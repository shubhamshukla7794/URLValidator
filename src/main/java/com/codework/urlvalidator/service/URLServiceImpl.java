package com.codework.urlvalidator.service;

import com.codework.urlvalidator.model.URLClass;
import com.codework.urlvalidator.repository.URLRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
public class URLServiceImpl implements URLService {

    private final URLRepository urlRepository;

    public URLServiceImpl(URLRepository urlRepository) {
        this.urlRepository = urlRepository;
    }


    @Override
    public URLClass findURL(String url) throws Exception {

//        URLClass savedURL;
        URL inspectURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) inspectURL.openConnection();
        connection.setInstanceFollowRedirects(false);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK){
            return getUrlClass(url,"OK");
        } else{
            return getUrlClass(url,"404");
        }

    }

    private URLClass getUrlClass(String url, String status) {
        URLClass savedURL;
        log.debug("I am in Service");
        Optional<URLClass> urlClassOptional = urlRepository.findByCheckURL(url);

        if (!urlClassOptional.isPresent()){

            URLClass newURL = new URLClass();
            newURL.setCheckURL(url);
            newURL.setCount(1L);
            newURL.setStatus(status);

            savedURL = urlRepository.save(newURL);
            log.debug("Saved URL Id - " + savedURL.getId());
        }else {

            URLClass updateURL = urlClassOptional.get();
            updateURL.setCount(updateURL.getCount()+1);

            savedURL = urlRepository.save(updateURL);
            log.debug("Saved URL Id - " + savedURL.getId());
        }
        return savedURL;
    }

    @Override
    public URLClass findById(Long id) throws Exception {

        Optional<URLClass> urlClassOptional = urlRepository.findById(id);
        if (!urlClassOptional.isPresent()){
            throw new NotFoundException("Not Found"+id.toString());
        }

        URLClass urlClass = urlClassOptional.get();

        return urlClass;
    }
}
