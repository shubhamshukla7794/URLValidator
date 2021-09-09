package com.codework.urlvalidator.service;

import com.codework.urlvalidator.model.URLClass;
import com.codework.urlvalidator.repository.URLRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        if (urlInspector(url) == "OK") {
            return getUrlClass(url, "OK");
        } else {
            String parentUrl = getParentUrl(url);
            if (urlInspector(parentUrl) == "OK") {
                return getUrlClass(parentUrl, "OK");
            } else {
                return getUrlClass(url,"404");
            }
        }

    }

    private String urlInspector(String url) throws Exception{
        URL inspectURL = new URL(url);
//        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection connection = (HttpURLConnection) inspectURL.openConnection();
        connection.setInstanceFollowRedirects(false);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK){
            return "OK";
        } else{
            return "404";
        }
    }

    private String getParentUrl(String childUrl){
        if (childUrl.endsWith("/")){
            childUrl = childUrl.substring(0,childUrl.length()-1);
        }
        int index = childUrl.lastIndexOf("/");
        if (index > 0) {
            return childUrl.substring(0, index);
        }
        return "/";
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
