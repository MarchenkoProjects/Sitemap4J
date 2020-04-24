package com.github.marchenkoprojects.sitemap4j;

/**
 * This type is used to indicate how frequently the page is likely to change.
 *
 * @author Oleg Marchenko
 * @see <a href="https://www.sitemaps.org/protocol.html#changefreqdef">changefreq</a>
 */
public enum ChangeFreq {
    ALWAYS,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    NEVER
}
