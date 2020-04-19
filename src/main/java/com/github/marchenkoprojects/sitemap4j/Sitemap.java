package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public class Sitemap {
    private static final int DEFAULT_MAX_URLS = 50_000;

    private final Set<Url> urls;
    private int maxUrls;

    public Sitemap() {
        this.urls = new LinkedHashSet<>(1024);
        this.maxUrls = DEFAULT_MAX_URLS;
    }

    public void setMaxUrls(int maxUrls) {
        this.maxUrls = maxUrls;
    }

    public void load(File file) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File [" + file + "] not found");
        }

        new SitemapValidator().validate(file);
        new SitemapLoader().load(file, urls);
    }

    public boolean addUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }
        if (urls.size() < maxUrls) {
            if (!urls.add(url)) {
                throw new SitemapAlreadyContainsUrlException(url.getLoc());
            }
            return true;
        }
        return false;
    }

    public boolean modifyUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }
        return urls.remove(url) && urls.add(url);
    }

    public boolean deleteUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }
        return urls.remove(url);
    }

    public void flush(File file) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }

        try(OutputStream os = new FileOutputStream(file)) {
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
            os.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">".getBytes());
            for (Url url: urls) {
                os.write(url.toString().getBytes());
            }
            os.write("</urlset>".getBytes());
            os.flush();
        }
        catch (IOException e) {
            throw new SitemapNotFlushedException(e);
        }

        urls.clear();
    }
}
