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
 * An abstract representation of a sitemap index protocol.
 * This class represents a real sitemap index in memory.
 * Provides multiple Sitemaps as list in a Sitemap index file.
 *
 * <p> Inside the sitemap there is a life cycle for the correct work with it.
 * <ol>
 *     <li>The first step is to specify a file in the file system;</li>
 *     <li>The next step is loading the sitemap from a file into memory for further work;</li>
 *     <li>The last step is to flush the sitemap back to the file.</li>
 * </ol>
 *
 * <p> Example:
 * <code>
 *     Sitemap sitemap = new SitemapIndex(new File("sitemap-index.xml"), "http://example.com");
 *     sitemap.load();
 *     sitemap.addUrl("/page1.html");
 *     sitemap.addUrl("/page2.html");
 *     sitemap.addUrl("/page3.html");
 *     sitemap.flush();
 * </code>
 *
 * @author Oleg Marchenko
 * @see <a href="https://www.sitemaps.org/protocol.html#index">Sitemap Index protocol</a>
 */
public class SitemapIndex extends Sitemap {
    /**
     * Default sitemap filename prefix used to create a new sitemap.
     */
    private static final String DEFAULT_FILENAME_PREFIX = "sitemap";
    /**
     * Basic sitemap file extension.
     */
    private static final String SITEMAP_FILE_EXT = ".xml";
    /**
     * Basic sitemap GZip file extension.
     */
    private static final String GZIP_FILE_EXT = ".gz";

    /**
     * Internal registry of all sitemaps in the sitemap index.
     * Represents the current state of a sitemap index with all sitemaps in memory.
     */
    private final Map<String, Sitemap> sitemaps;

    /**
     * Indicates the current sitemap filename prefix.
     * It used to create a new sitemap.
     */
    private String sitemapFilenamePrefix;

    /**
     * Creates a new sitemap index instance with an abstract or real file and base site URL.
     *
     * @param file abstract or real sitemap index file in filesystem;
     *             this file may also have a <tt>.gz</tt> extension
     * @param baseUrl basic site URL
     * @throws NullPointerException if the file is <code>null</code>
     */
    public SitemapIndex(File file, String baseUrl) {
        super(file, baseUrl);
        this.sitemaps = new LinkedHashMap<>(32);
        this.sitemapFilenamePrefix = DEFAULT_FILENAME_PREFIX;
    }

    /**
     * Sets the sitemap filename prefix.
     *
     * @param sitemapFilenamePrefix the sitemap filename prefix
     */
    public void setSitemapFilenamePrefix(String sitemapFilenamePrefix) {
        this.sitemapFilenamePrefix = sitemapFilenamePrefix;
    }

    /**
     * Performs to load the current state of the sitemap index and all registered sitemaps in it.
     * Method will load if the file really exists in the file system.
     * This method is part of the life cycle of working with a sitemap index or sitemap.
     *
     * @param validate indicates whether to validate the sitemap index and all registered sitemaps in it
     * @throws SitemapNotValidException if the sitemap index or any sitemaps in it did not passed the validation stage
     * @throws SitemapNotLoadedException if errors occurred while loading the sitemap index or any sitemaps in it
     */
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

    /**
     * Adds a new URL to the first available sitemap registered in sitemap index.
     * If there are no available sitemaps than new sitemap will be created and added to sitemap index.
     * If base URL are present that this method can accept part of URL without part of base URL.
     *
     * @param url new URL to add to the first available sitemap
     * @return <code>true</code> if the URL is successfully added to the first available sitemap
     * @throws NullPointerException if URL is <code>null</code>
     * @throws SitemapAlreadyContainsUrlException if this URL already exists in the any sitemap registered in sitemap index
     */
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

    /**
     * Modifies the URL in the first available sitemap registered in sitemap index.
     * URL be to modify only if existing in any sitemaps registered in sitemap index..
     *
     * @param url URL to modify
     * @return <code>true</code> if the URL is modified successfully
     * @throws NullPointerException if URL is <code>null</code>
     */
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

    /**
     * Performs to flush the current state of the sitemap index and all registered sitemaps in it.
     * This method is part of the life cycle of working with a sitemap.
     */
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

    /**
     * Represents the parent tag for any sitemap index URL.
     *
     * @see <a href="https://www.sitemaps.org/protocol.html#sitemapIndex_sitemapindex">Sitemap index tag definitions</a>
     */
    static class Url {
        /**
         * <p> For Sitemap: <br>
         * URL of the page. This URL must begin with the protocol (such as http) and end with a trailing slash,
         * if your web server requires it.
         *
         * <p> For Sitemap Index: <br>
         * Identifies the location of the sitemap.
         *
         * @see <a href="https://www.sitemaps.org/protocol.html#locdef">'loc' tag in sitemap</a>
         * @see <a href="https://www.sitemaps.org/protocol.html#sitemapIndex_loc">'loc' tag in sitemap index</a>
         */
        protected final String loc;

        /**
         * <p> For Sitemap: <br>
         * The date of last modification of the page.
         * This date should be in <a href="https://www.w3.org/TR/NOTE-datetime">W3C Datetime</a> format.
         *
         * <p> For Sitemap Index: <br>
         * Identifies the time that the corresponding Sitemap file was modified.
         *
         * @see <a href="https://www.sitemaps.org/protocol.html#lastmoddef">'lastmod' tag in sitemap</a>
         * @see <a href="https://www.sitemaps.org/protocol.html#sitemapIndex_lastmod">'lastmod' tag in sitemap index</a>
         */
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
