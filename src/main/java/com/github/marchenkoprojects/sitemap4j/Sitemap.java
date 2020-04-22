package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class Sitemap {
    private static final int DEFAULT_MAX_URLS = 50_000;

    private final File file;
    private String baseUrl;

    private final Map<String, Url> urls;
    private int maxUrls;

    public Sitemap(File file) {
        this(file, null);
    }

    public Sitemap(File file, String baseUrl) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        this.file = file;
        this.baseUrl = baseUrl;
        this.urls = new LinkedHashMap<>(2048);
        this.maxUrls = DEFAULT_MAX_URLS;
    }

    public void setMaxUrls(int maxUrls) {
        this.maxUrls = maxUrls;
    }

    public Url createUrl(String url) {
        if (isNull(baseUrl) || baseUrl.isEmpty()) {
            return new Url(url);
        }
        return new Url(url.startsWith(baseUrl) ? url : baseUrl + url);
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

    public static class Url {
        private final String loc;
        private Temporal lastmod;
        private ChangeFreq changefreq;
        private Float priority;

        Url(String loc) {
            if (isNull(loc) || loc.isEmpty()) {
                throw new IllegalArgumentException("Parameter 'loc' must not be null or empty");
            }
            this.loc = loc;
        }

        public String getLoc() {
            return loc;
        }

        public Temporal getLastmod() {
            return lastmod;
        }

        void setLastmod(Temporal lastmod) {
            this.lastmod = lastmod;
        }

        public void setLastmod(LocalDate lastmod) {
            this.lastmod = lastmod;
        }

        public void setLastmod(LocalDateTime lastmod) {
            this.lastmod = lastmod.atOffset(UTC).withNano(0);
        }

        public void setLastmod(OffsetDateTime lastmod) {
            this.lastmod = lastmod;
        }

        public ChangeFreq getChangefreq() {
            return changefreq;
        }

        public void setChangefreq(ChangeFreq changefreq) {
            this.changefreq = changefreq;
        }

        public Float getPriority() {
            return priority;
        }

        public void setPriority(Float priority) {
            if (nonNull(priority)) {
                if (priority < 0 || priority > 1) {
                    throw new IllegalArgumentException("Parameter 'priority' must be between 0 and 1");
                }
            }
            this.priority = priority;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("\t<url>\n");
            builder.append("\t\t<loc>").append(loc).append("</loc>\n");
            if (nonNull(lastmod)) {
                builder.append("\t\t<lastmod>").append(lastmod).append("</lastmod>\n");
            }
            if (nonNull(changefreq)) {
                builder.append("\t\t<changefreq>").append(changefreq.name().toLowerCase()).append("</changefreq>\n");
            }
            if (nonNull(priority)) {
                builder.append("\t\t<priority>").append(priority).append("</priority>\n");
            }
            builder.append("\t</url>\n");
            return builder.toString();
        }
    }
}
