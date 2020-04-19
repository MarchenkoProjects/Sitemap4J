package com.github.marchenkoprojects.sitemap4j;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            Url.Builder urlBuilder = null;
            BiConsumer<String, Url.Builder> tagValueConsumer = null;

            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(file));
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    String tagName = xmlStreamReader.getLocalName();
                    switch (tagName) {
                        case "url":
                            urlBuilder = Url.builder();
                            break;
                        case "loc":
                            tagValueConsumer = (tagValue, builder) -> builder.setLoc(tagValue);
                            break;
                        case "lastmod":
                            tagValueConsumer = (tagValue, builder) -> builder.setLastmod(ZonedDateTime.parse(tagValue));
                            break;
                        case "changefreq":
                            tagValueConsumer = (tagValue, builder) -> builder.setChangefreq(ChangeFreqType.valueOf(tagValue.toUpperCase()));
                            break;
                        case "priority":
                            tagValueConsumer = (tagValue, builder) -> builder.setPriority(Float.parseFloat(tagValue));
                            break;
                    }
                }
                if (event == XMLEvent.CHARACTERS) {
                    String text = xmlStreamReader.getText().trim();
                    if (nonNull(tagValueConsumer) && !text.isEmpty()) {
                        tagValueConsumer.accept(text, urlBuilder);
                    }
                }
                if (event == XMLEvent.END_ELEMENT) {
                    String tagName = xmlStreamReader.getLocalName();
                    switch (tagName) {
                        case "url":
                            urls.add(urlBuilder.build());
                            urlBuilder = null;
                            break;
                        case "loc":
                        case "lastmod":
                        case "changefreq":
                        case "priority":
                            tagValueConsumer = null;
                            break;
                    }
                }
            }
        }
        catch (XMLStreamException e) {
            throw new SitemapNotLoadedException(e);
        }
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
