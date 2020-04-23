package com.github.marchenkoprojects.sitemap4j;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
class SitemapIndexLoader extends SitemapLoader {

    @Override
    void load(File file, Map<String, SitemapIndex.Url> urls) {
        XMLStreamReader xmlStreamReader = null;
        try {
            UrlBuilder urlBuilder = null;
            BiConsumer<String, UrlBuilder> tagValueConsumer = null;

            xmlStreamReader = createXMLStreamReader(file);
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    String tagName = xmlStreamReader.getLocalName();
                    switch (tagName) {
                        case "sitemap":
                            urlBuilder = new UrlBuilder();
                            break;
                        case "loc":
                            tagValueConsumer = (value, builder) -> builder.setLoc(value);
                            break;
                        case "lastmod":
                            tagValueConsumer = (value, builder) -> builder.setLastmod(value);
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
                        case "sitemap":
                            if (nonNull(urlBuilder)) {
                                SitemapIndex.Url url = urlBuilder.build();
                                urls.put(url.getLoc(), url);

                                urlBuilder = null;
                            }
                            break;
                        case "loc":
                        case "lastmod":
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

    protected static class UrlBuilder {
        private static final Pattern TZD_PATTERN = Pattern.compile("(\\+\\d{2}:\\d{2}|-\\d{2}:\\d{2}|Z)");

        protected String loc;
        protected Temporal lastmod;

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

        public SitemapIndex.Url build() {
            SitemapIndex.Url url = new SitemapIndex.Url(loc);
            url.setLastmod(lastmod);
            return url;
        }
    }
}
