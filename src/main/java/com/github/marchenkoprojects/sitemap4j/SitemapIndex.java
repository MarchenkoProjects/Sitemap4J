package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class SitemapIndex extends Sitemap {
    private static final String DEFAULT_FILENAME_PREFIX = "sitemap";
    private static final String SITEMAP_FILE_EXT = ".xml";
    private static final String GZIP_FILE_EXT = ".gz";

    private final Map<String, Sitemap> sitemaps;
    private String sitemapFilenamePrefix;

    public SitemapIndex(File file, String baseUrl) {
        super(file, baseUrl);
        this.sitemaps = new LinkedHashMap<>(32);
        this.sitemapFilenamePrefix = DEFAULT_FILENAME_PREFIX;
    }

    public void setSitemapFilenamePrefix(String sitemapFilenamePrefix) {
        this.sitemapFilenamePrefix = sitemapFilenamePrefix;
    }

    @Override
    public void load(boolean validate) {
        if (file.exists()) {
            if (validate) {
                new SitemapIndexValidator().validate(file);
            }
            urls.clear();

            new SitemapIndexLoader().load(file, urls);

            for (String url: urls.keySet()) {
                String filename = url.substring(url.lastIndexOf('/') + 1);

                Sitemap sitemap = new Sitemap(new File(getBaseDir() + filename), baseUrl);
                sitemap.setMaxUrls(maxUrls);
                sitemap.load(validate);
                sitemaps.put(url, sitemap);
            }
        }
    }

    @Override
    public boolean addUrl(Sitemap.Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }

        for (Sitemap sitemap: sitemaps.values()) {
            if (sitemap.containsUrl(url)) {
                throw new SitemapAlreadyContainsUrlException(sitemap.file.getName() + " [" + url.getLoc() + "]");
            }
        }

        boolean urlAdded = false;
        for (Entry<String, Sitemap> urlToSitemap: sitemaps.entrySet()) {
            Sitemap sitemap = urlToSitemap.getValue();
            if (sitemap.addUrl(url)) {
                Url currentUrl = urls.get(urlToSitemap.getKey());
                currentUrl.setLastmod(LocalDateTime.now());

                urlAdded = true;
                break;
            }
        }
        if (!urlAdded) {
            String sitemapFilename = generateSitemapFilename();

            Url newSitemapUrl = new Url(getBaseUrl() + sitemapFilename);
            newSitemapUrl.setLastmod(LocalDateTime.now());
            urls.put(newSitemapUrl.getLoc(), newSitemapUrl);

            Sitemap sitemap = new Sitemap(new File(getBaseDir() + sitemapFilename), baseUrl);
            sitemap.setMaxUrls(maxUrls);
            sitemap.addUrl(url);
            sitemaps.put(newSitemapUrl.getLoc(), sitemap);
        }
        return true;
    }

    private String generateSitemapFilename() {
        String filename = sitemapFilenamePrefix + (sitemaps.size() + 1) + SITEMAP_FILE_EXT;
        if (file.getName().endsWith(GZIP_FILE_EXT)) {
            filename += GZIP_FILE_EXT;
        }
        return filename;
    }

    @Override
    public boolean modifyUrl(Sitemap.Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }
        for (Entry<String, Sitemap> urlToSitemap: sitemaps.entrySet()) {
            Sitemap sitemap = urlToSitemap.getValue();
            if (sitemap.modifyUrl(url)) {
                Url currentUrl = urls.get(urlToSitemap.getKey());
                currentUrl.setLastmod(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    @Override
    public void flush() {
        new SitemapIndexFlusher().flush(urls.values(), file);

        sitemaps.values().forEach(Sitemap::flush);
        urls.clear();
    }

    private String getBaseDir() {
        return file.getParent() + File.separator;
    }

    private String getBaseUrl() {
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/");
    }

    static class Url {
        protected final String loc;
        protected Temporal lastmod;

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

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(64);
            builder.append("\t<sitemap>\n");
            builder.append("\t\t<loc>").append(loc).append("</loc>\n");
            if (nonNull(lastmod)) {
                builder.append("\t\t<lastmod>").append(lastmod).append("</lastmod>\n");
            }
            builder.append("\t</sitemap>\n");
            return builder.toString();
        }
    }
}
