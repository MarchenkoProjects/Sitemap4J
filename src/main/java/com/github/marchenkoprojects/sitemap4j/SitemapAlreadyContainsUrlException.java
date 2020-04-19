package com.github.marchenkoprojects.sitemap4j;

/**
 * @author Oleg Marchenko
 */
public class SitemapAlreadyContainsUrlException extends RuntimeException {
    public SitemapAlreadyContainsUrlException(String message) {
        super(message);
    }
}
