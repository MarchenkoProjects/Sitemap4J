package com.github.marchenkoprojects.sitemap4j;

/**
 * @author Oleg Marchenko
 */
class SitemapIndexFlusher extends SitemapFlusher {

    @Override
    protected String getStartRootTag() {
        return "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
    }

    @Override
    protected String getEndRootTag() {
        return "</sitemapindex>";
    }
}
