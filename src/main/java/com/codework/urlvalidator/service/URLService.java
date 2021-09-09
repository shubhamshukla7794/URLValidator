package com.codework.urlvalidator.service;

import com.codework.urlvalidator.model.URLClass;

import java.io.IOException;
import java.net.MalformedURLException;

public interface URLService {
    URLClass findURL(String url) throws Exception;
    URLClass findById(Long id) throws Exception;
}
