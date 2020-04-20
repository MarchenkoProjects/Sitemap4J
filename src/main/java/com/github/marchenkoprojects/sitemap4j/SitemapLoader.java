package com.github.marchenkoprojects.sitemap4j;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
class SitemapLoader {

    public void load(File file, Collection<Url> urls) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = null;
        try {
            UrlBuilder urlBuilder = null;
            BiConsumer<String, UrlBuilder> tagValueConsumer = null;

            xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(file));
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    String tagName = xmlStreamReader.getLocalName();
                    switch (tagName) {
                        case "url":
                            urlBuilder = new UrlBuilder();
                            break;
                        case "loc":
                            tagValueConsumer = (value, builder) -> builder.setLoc(value);
                            break;
                        case "lastmod":
                            tagValueConsumer = (value, builder) -> builder.setLastmod(value);
                            break;
                        case "changefreq":
                            tagValueConsumer = (value, builder) -> builder.setChangefreq(value);
                            break;
                        case "priority":
                            tagValueConsumer = (value, builder) -> builder.setPriority(value);
                            break;
                    }
                }
                else if (event == XMLEvent.CHARACTERS) {
                    String text = xmlStreamReader.getText().trim();
                    if (nonNull(tagValueConsumer) && !text.isEmpty()) {
                        tagValueConsumer.accept(text, urlBuilder);
                    }
                }
                else if (event == XMLEvent.END_ELEMENT) {
                    String tagName = xmlStreamReader.getLocalName();
                    switch (tagName) {
                        case "url":
                            if (nonNull(urlBuilder)) {
                                urls.add(urlBuilder.build());
                                urlBuilder = null;
                            }
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
        finally {
            if (nonNull(xmlStreamReader)) {
                try {
                    xmlStreamReader.close();
                }
                catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static class UrlBuilder {
        private static final Pattern TZD_PATTERN = Pattern.compile("(\\+\\d{2}:\\d{2}|-\\d{2}:\\d{2}|Z)");

        private String loc;
        private Temporal lastmod;
        private ChangeFreq changefreq;
        private Float priority;

        public void setLoc(String loc) {
            this.loc = loc;
        }

        public void setLastmod(String value) {
            if (TZD_PATTERN.matcher(value).find()) {
                this.lastmod = OffsetDateTime.parse(value);
            }
            else {
                this.lastmod = LocalDate.parse(value);
            }
        }

        public void setChangefreq(String value) {
            this.changefreq = ChangeFreq.valueOf(value.toUpperCase());
        }

        public void setPriority(String value) {
            this.priority = Float.parseFloat(value);
        }

        public Url build() {
            Url url = new Url(loc);
            url.setLastmod(lastmod);
            url.setChangefreq(changefreq);
            url.setPriority(priority);
            return url;
        }
    }
}
