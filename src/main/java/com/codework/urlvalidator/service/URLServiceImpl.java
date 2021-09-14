package com.codework.urlvalidator.service;

import com.codework.urlvalidator.model.URLClass;
import com.codework.urlvalidator.repository.URLRepository;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Service
public class URLServiceImpl implements URLService {

    private final URLRepository urlRepository;
    private final String SITE_IS_UP = "Site is up";
    private final String SITE_IS_DOWN = "Site is down";
    private final String INCORRECT_URL = "URL is incorrect";

    public URLServiceImpl(URLRepository urlRepository) {
        this.urlRepository = urlRepository;
    }


    @Override
    public URLClass findURL(String url) throws Exception {

        url = getFormattedURL(url);

        String status = urlInspector(url);

        if (!status.equals(SITE_IS_UP) && !status.equals(SITE_IS_DOWN) && !status.equals(INCORRECT_URL)) {
            url = status;
            status = urlInspector(url);
        }

        if (status.equals(SITE_IS_UP)) {
            return getSavedURL(url, status);
        } else {
            URLClass temp=null;
            String parentURL=url;

            while (!parentURL.equals("/") && !status.equals(SITE_IS_UP)) {

                status = urlInspector(parentURL);
                if (status.equals(SITE_IS_UP)){
                    return getSavedURL(parentURL,status);
                }else {
                    temp = getSavedURL(parentURL,status);
                }
                parentURL = getParentUrl(parentURL);
            }

            if (parentURL.equals("/")) {
                return getUrlClassByURL(url);
            }else {
                return temp;
            }

        }

    }

    private URLClass getUrlClassByURL(String url) throws Exception {
        Optional<URLClass> urlClassOptional = urlRepository.findByCheckURL(url);

        if (urlClassOptional.isEmpty()) {
            throw new NotFoundException("URL Not Found - " + url);
        }

        return urlClassOptional.get();
    }

    private String getFormattedURL(String url) {
        String headerUrl = "https://";

        if (!url.endsWith("/")){
            url = url.concat("/");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = headerUrl.concat(url);
        }
        return url;
    }

    private String urlInspector(String url){

        try {
            URL inspectURL = new URL(url);
//        HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = (HttpURLConnection) inspectURL.openConnection();
            connection.setInstanceFollowRedirects(false);

            int responseCode = (connection.getResponseCode())/100;

            if (responseCode == 2){
                return SITE_IS_UP;
            } else if (responseCode == 3) {
                return connection.getHeaderField("Location");
            }else{
                return SITE_IS_DOWN;
            }

        } catch (MalformedURLException e) {
            return INCORRECT_URL;
        } catch (IOException e) {
            return SITE_IS_DOWN;
        }



    }

    private String getParentUrl(String childUrl){
        String tempChild = null;
        if (childUrl.endsWith("/")){
            childUrl = childUrl.substring(0,childUrl.length()-1);
        }
        int index = childUrl.lastIndexOf("/");
        if (index > 0) {
            tempChild = childUrl.substring(0, index+1);
        }

        if (tempChild.equals("https://")  || tempChild.equals("http://"))
            return "/";
        else 
            return tempChild;
    }

    private URLClass getSavedURL(String url, String status) {
        URLClass savedURL;
        log.debug("I am in Service");
        Optional<URLClass> urlClassOptional = urlRepository.findByCheckURL(url);

        if (urlClassOptional.isEmpty()){

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
        if (urlClassOptional.isEmpty()){
            throw new NotFoundException("Not Found"+id.toString());
        }

        return urlClassOptional.get();
    }
}
