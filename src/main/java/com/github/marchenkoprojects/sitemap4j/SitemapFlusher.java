package com.github.marchenkoprojects.sitemap4j;

import com.github.marchenkoprojects.sitemap4j.SitemapIndex.Url;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

/**
 * @author Oleg Marchenko
 */
class SitemapFlusher {

    void flush(Collection<Url> urls, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            if (file.getName().endsWith(".gz")) {
                os = new GZIPOutputStream(os);
            }

            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
            os.write(getStartRootTag().getBytes());
            for (Url url: urls) {
                os.write(url.toString().getBytes());
            }
            os.write(getEndRootTag().getBytes());
            os.flush();
            os.close();
        }
        catch (IOException e) {
            throw new SitemapNotFlushedException(e);
        }
    }

    protected String getStartRootTag() {
        return "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
    }

    protected String getEndRootTag() {
        return "</urlset>";
    }
}
