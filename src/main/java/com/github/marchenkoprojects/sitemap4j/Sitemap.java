package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class Sitemap {
    private static final int DEFAULT_MAX_URLS = 50_000;

    private final File file;
    private final Map<String, Url> urls;
    private int maxUrls;

    public Sitemap(File file) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        this.file = file;
        this.urls = new LinkedHashMap<>(2048);
        this.maxUrls = DEFAULT_MAX_URLS;
    }

    public void setMaxUrls(int maxUrls) {
        this.maxUrls = maxUrls;
    }

    public void load() {
        load(true);
    }

    public void load(boolean validate) {
        if (file.exists()) {
            if (validate) {
                new SitemapValidator().validate(file);
            }
            new SitemapLoader().load(file, urls);
        }
    }

    public boolean addUrl(String url) {
        if (isNull(url) || url.isEmpty()) {
            throw new NullPointerException("Parameter 'url' must not be null or empty");
        }
        return addUrl(new Url(url));
    }

    public boolean addUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }
        if (urls.size() < maxUrls) {
            String loc = url.getLoc();
            if (urls.containsKey(loc)) {
                throw new SitemapAlreadyContainsUrlException(loc);
            }

            urls.put(loc, url);
            return true;
        }
        return false;
    }

    public boolean modifyUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }

        Url prevUrl = urls.replace(url.getLoc(), url);
        return nonNull(prevUrl);
    }

    public boolean deleteUrl(String url) {
        if (isNull(url) || url.isEmpty()) {
            throw new NullPointerException("Parameter 'url' must not be null or empty");
        }
        return deleteUrl(new Url(url));
    }

    public boolean deleteUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }

        Url removedUrl = urls.remove(url.getLoc());
        return nonNull(removedUrl);
    }

    public void flush() {
        new SitemapFlusher().flush(urls.values(), file);
        urls.clear();
    }
}
