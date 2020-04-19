package com.github.marchenkoprojects.sitemap4j;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class SitemapLoader {

    public void load(File file, Collection<Url> urls) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }
        if (isNull(urls) || urls.isEmpty()) return;

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
            xmlStreamReader.close();
        }
        catch (XMLStreamException e) {
            throw new SitemapNotLoadedException(e);
        }
    }
}
