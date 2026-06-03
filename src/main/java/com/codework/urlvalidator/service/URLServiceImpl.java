package com.codework.urlvalidator.service;

import com.codework.urlvalidator.model.URLClass;
import com.codework.urlvalidator.repository.URLRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class URLServiceImpl implements URLService {

    private final URLRepository urlRepository;
    private final HttpClient httpClient;

    private static final String SITE_IS_UP = "Site is up";
    private static final String SITE_IS_DOWN = "Site is down";
    private static final String INCORRECT_URL = "URL is incorrect";
    private static final int MAX_REDIRECT_HOPS = 5;

    public URLServiceImpl(URLRepository urlRepository) {
        this.urlRepository = urlRepository;
        // Instantiate a reusable modern HTTP Client configured with a 5-second timeout
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public URLClass findURL(String url) throws Exception {
        url = getFormattedURL(url);
        String status = urlInspector(url);
        int redirectCount = 0;

        // Loop to safely resolve multiple consecutive redirects (3xx status codes) up to a maximum threshold
        while (!status.equals(SITE_IS_UP) && !status.equals(SITE_IS_DOWN) && !status.equals(INCORRECT_URL)) {
            if (redirectCount >= MAX_REDIRECT_HOPS) {
                status = SITE_IS_DOWN;
                break;
            }
            url = status; // The status string contains the redirected destination URL
            status = urlInspector(url);
            redirectCount++;
        }

        if (status.equals(SITE_IS_UP)) {
            return getSavedURL(url, status);
        } else {
            URLClass temp = null;
            String parentURL = url;

            // Fallback traversal: Check parent directories if the deep-linked page is down
            while (!"/".equals(parentURL) && !status.equals(SITE_IS_UP)) {
                status = urlInspector(parentURL);
                if (status.equals(SITE_IS_UP)) {
                    return getSavedURL(parentURL, status);
                } else {
                    temp = getSavedURL(parentURL, status);
                }
                parentURL = getParentUrl(parentURL);
            }

            if ("/".equals(parentURL)) {
                return getUrlClassByURL(url);
            } else {
                return temp;
            }
        }
    }

    private URLClass getUrlClassByURL(String url) throws Exception {
        return urlRepository.findByCheckURL(url)
                .orElseThrow(() -> new IllegalArgumentException("URL Not Found in Database - " + url));
    }

    private String getFormattedURL(String url) {
        // Enforce trailing slash safely
        if (!url.endsWith("/")) {
            url = url.concat("/");
        }

        // Prepend protocol if missing
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            // Fix case-sensitivity issue: Only convert protocol and host domain to lowercase
            URI uri = URI.create(url);
            String scheme = uri.getScheme().toLowerCase();
            String host = uri.getHost().toLowerCase();
            String path = uri.getRawPath(); // Keeps the original exact case of subfolders/files
            
            return scheme + "://" + host + (path != null ? path : "/");
        } catch (Exception e) {
            return url; // Fallback to raw string if URI parsing fails
        }
    }

    private String urlInspector(String url) {
        try {
            // 1. Build a HEAD request with a realistic Browser User-Agent header
            HttpRequest headRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<Void> response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            // 2. Fallback: If HEAD fails with 403, 405 (Method Not Allowed), or 503, retry with a GET request
            if (statusCode == 403 || statusCode == 405 || statusCode == 503) {
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(Duration.ofSeconds(5))
                        .build();
                
                response = httpClient.send(getRequest, HttpResponse.BodyHandlers.discarding());
                statusCode = response.statusCode();
            }

            int familyCode = statusCode / 100;

            if (familyCode == 2) {
                return SITE_IS_UP;
            } else if (familyCode == 3) {
                return response.headers().firstValue("Location").orElse(SITE_IS_DOWN);
            } else {
                return SITE_IS_DOWN;
            }

        } catch (IllegalArgumentException e) {
            return INCORRECT_URL;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SITE_IS_DOWN;
        }
    }

    private String getParentUrl(String childUrl) {
        if (childUrl.endsWith("/")) {
            childUrl = childUrl.substring(0, childUrl.length() - 1);
        }
        
        int index = childUrl.lastIndexOf("/");
        String tempChild = null;
        
        if (index > 0) {
            tempChild = childUrl.substring(0, index + 1);
        }

        // Prevent NullPointerException by avoiding calling methods directly on non-initialized objects
        if (tempChild == null || "https://".equals(tempChild) || "http://".equals(tempChild)) {
            return "/";
        } else {
            return tempChild;
        }
    }

    private URLClass getSavedURL(String url, String status) {
        log.debug("Processing persistence layer logic in Service");
        Optional<URLClass> urlClassOptional = urlRepository.findByCheckURL(url);

        if (urlClassOptional.isEmpty()) {
            URLClass newURL = new URLClass();
            newURL.setCheckURL(url);
            newURL.setCount(1L);
            newURL.setStatus(status);

            URLClass savedURL = urlRepository.save(newURL);
            log.debug("Saved new URL entry with Id - " + savedURL.getId());
            return savedURL;
        } else {
            URLClass updateURL = urlClassOptional.get();
            updateURL.setCount(updateURL.getCount() + 1);
            updateURL.setStatus(status); // Keeps database status current if it switched states

            URLClass savedURL = urlRepository.save(updateURL);
            log.debug("Incremented access count for URL Id - " + savedURL.getId());
            return savedURL;
        }
    }

    @Override
    public URLClass findById(Long id) throws Exception {
        return urlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Value not found for ID value " + id));
    }
}