package com.codework.urlvalidator.controller;

import com.codework.urlvalidator.model.URLClass;
import com.codework.urlvalidator.service.URLService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class IndexController {

    private final URLService urlService;

    public IndexController(URLService urlService) {
        this.urlService = urlService;
    }

    @GetMapping({"","/","/index","/home"})
    public String getIndexPage(Model model){
        model.addAttribute("urlclass", new URLClass());
        return "index";
    }

    @GetMapping("/result/{id}")
    public String showResult(@PathVariable String id, Model model) throws Exception {
        model.addAttribute("urlclass", urlService.findById(Long.valueOf(id)));
        return "result";
    }

    @PostMapping("validateURL")
    public String validatingURL(@Valid @ModelAttribute("urlclass") URLClass urlClass) throws Exception {
        URLClass savedURL = urlService.findURL(urlClass.getCheckURL());
        return "redirect:/result/"+ savedURL.getId();
    }
}
