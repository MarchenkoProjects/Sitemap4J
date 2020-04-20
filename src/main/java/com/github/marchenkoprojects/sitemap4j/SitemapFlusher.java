package com.github.marchenkoprojects.sitemap4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Oleg Marchenko
 */
class SitemapFlusher {

    public void flush(Collection<Url> urls, File file) {
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
    }
}
