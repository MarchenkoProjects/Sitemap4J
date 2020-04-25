package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * An abstract representation of a sitemap protocol.
 * This class represents a real sitemap in memory.
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
 *     Sitemap sitemap = new Sitemap(new File("sitemap.xml"));
 *     sitemap.load();
 *     sitemap.addUrl("http://example.com/page1.html");
 *     sitemap.addUrl("http://example.com/page2.html");
 *     sitemap.addUrl("http://example.com/page3.html");
 *     sitemap.flush();
 * </code>
 *
 * @author Oleg Marchenko
 * @see <a href="https://www.sitemaps.org/protocol.html">Sitemap protocol</a>
 */
public class Sitemap {
    /**
     * The maximum number of URLs that a sitemap can store from the specification.
     */
    private static final int DEFAULT_MAX_URLS = 50_000;

    /**
     * This file is the main sitemap file.
     * It can be a real file in the file system or abstract non-existent file.
     * This file will be used to load the current state of the sitemap and flush a modified copy in memory.
     */
    protected final File file;

    /**
     * The base site URL is used to shorten the link when it is added or modified.
     */
    protected String baseUrl;

    /**
     * Internal registry of all URLs in the sitemap.
     * Represents the current state of a sitemap in memory.
     */
    protected final Map<String, SitemapIndex.Url> urls;

    /**
     * Indicates the maximum number of URls that a sitemap can store.
     */
    protected int maxUrls;

    /**
     * Creates a new sitemap instance with an abstract or real file in filesystem.
     *
     * @param file abstract or real file in filesystem;
     *             this file may also have a <tt>.gz</tt> extension
     * @throws NullPointerException if the file is <code>null</code>
     */
    public Sitemap(File file) {
        this(file, null);
    }

    /**
     * Creates a new sitemap instance with an abstract or real file and base site URL.
     *
     * @param file abstract or real sitemap file in filesystem;
     *             this file may also have a <tt>.gz</tt> extension
     * @param baseUrl basic site URL
     * @throws NullPointerException if the file is <code>null</code>
     */
    public Sitemap(File file, String baseUrl) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        this.file = file;
        this.baseUrl = baseUrl;
        this.urls = new LinkedHashMap<>(2048);
        this.maxUrls = DEFAULT_MAX_URLS;
    }

    /**
     * Sets the maximum number of URLs that a sitemap can store.
     *
     * @param maxUrls the maximum number of URLs
     * @throws IllegalArgumentException if the maximum number of URLs is greater
     *                                  than the possible value from the specification
     */
    public void setMaxUrls(int maxUrls) {
        if (maxUrls > DEFAULT_MAX_URLS) {
            throw new IllegalArgumentException("Parameter 'maxUrls' cannot exceed 50,000 URLs");
        }
        this.maxUrls = maxUrls;
    }

    /**
     * Represents the main factory method for creating URL.
     * If base url is specified then it will be added as a prefix
     * if the current <code>url</code> does not contain it.
     *
     * @param url URL for adding to the sitemap
     * @throws NullPointerException if URL is <code>null</code> or empty
     */
    public Url createUrl(String url) {
        if (isNull(url) || url.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'url' must not be null or empty");
        }
        if (isNull(baseUrl) || baseUrl.isEmpty()) {
            return new Url(url);
        }
        return new Url(url.startsWith(baseUrl) ? url : baseUrl + url);
    }

    /**
     * Performs to load the current state of the sitemap from a file.
     * Method will load if the file really exists in the file system.
     * This method is part of the life cycle of working with a sitemap.
     * Calling this method will always validate the sitemap.
     *
     * @throws SitemapNotValidException if the sitemap has not passed the validation stage
     * @throws SitemapNotLoadedException if errors occurred while loading the sitemap
     */
    public void load() {
        load(true);
    }

    /**
     * Performs to load the current state of the sitemap from a file.
     * Method will load if the file really exists in the file system.
     * This method is part of the life cycle of working with a sitemap.
     *
     * @param validate indicates whether to validate the sitemap
     * @throws SitemapNotValidException if the sitemap has not passed the validation stage
     * @throws SitemapNotLoadedException if errors occurred while loading the sitemap
     */
    public void load(boolean validate) {
        if (file.exists()) {
            if (validate) {
                new SitemapValidator().validate(file);
            }
            urls.clear();

            new SitemapLoader().load(file, urls);
        }
    }

    /**
     * Adds a new URL to the sitemap.
     * If base URL are present that this method can accept part of url without part of base URL.
     *
     * @param url new URL to add
     * @return <code>true</code> if the URL is added successfully;
     *         URL will not be added to the sitemap if such URL already exists
     *         or the maximum number of URls has been reached
     * @throws IllegalArgumentException if URL is <code>null</code> or empty
     * @throws SitemapAlreadyContainsUrlException if this URL already exists in the sitemap
     */
    public boolean addUrl(String url) {
        if (isNull(url) || url.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'url' must not be null or empty");
        }
        return addUrl(createUrl(url));
    }

    /**
     * Adds a new URL to the sitemap.
     * If base URL are present that this method can accept part of url without part of base URL.
     *
     * @param url new URL to add
     * @return <code>true</code> if the URL is added successfully;
     *         URL will not be added to the sitemap if such URL already exists
     *         or the maximum number of URls has been reached
     * @throws NullPointerException if URL is <code>null</code>
     * @throws SitemapAlreadyContainsUrlException if this URL already exists in the sitemap
     */
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

    /**
     * Modifies the URL in the sitemap. URL be to modify only if existing in current sitemap.
     *
     * @param url URL to modify
     * @return <code>true</code> if the URL is modified successfully
     * @throws NullPointerException if URL is <code>null</code>
     */
    public boolean modifyUrl(Url url) {
        if (isNull(url)) {
            throw new NullPointerException("Parameter 'url' must not be null");
        }

        SitemapIndex.Url prevUrl = urls.replace(url.getLoc(), url);
        return nonNull(prevUrl);
    }

    /**
     * Performs to flush the current state of the sitemap to a file in the file system.
     * This method is part of the life cycle of working with a sitemap.
     *
     * @throws SitemapNotFlushedException if errors occurred while flushing the sitemap
     */
    public void flush() {
        new SitemapFlusher().flush(urls.values(), file);
        urls.clear();
    }

    /**
     * Returns <code>true</code> if this sitemap contains the current URL.
     *
     * @param url URL to be tested
     * @return <code>true</code> if sitemap contain the URL
     */
    protected boolean containsUrl(Url url) {
        return urls.containsKey(url.getLoc());
    }

    /**
     * Represents the parent tag for any sitemap URL.
     *
     * @see <a href="https://www.sitemaps.org/protocol.html#urldef">Sitemap tag definitions</a>
     */
    public static class Url extends SitemapIndex.Url {
        /**
         * How frequently the page is likely to change.
         * This value provides general information to search engines and
         * may not correlate exactly to how often they crawl the page.
         *
         * @see <a href="https://www.sitemaps.org/protocol.html#changefreqdef">'changefreq' tag</a>
         */
        private ChangeFreq changefreq;
        /**
         * The priority of this URL relative to other URLs on your site.
         * Valid values range from 0.0 to 1.0.
         *
         * @see <a href="https://www.sitemaps.org/protocol.html#prioritydef">'priority' tag</a>
         */
        private Float priority;

        Url(String loc) {
            super(loc);
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
            StringBuilder builder = new StringBuilder(128);
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
