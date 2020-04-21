package com.github.marchenkoprojects.sitemap4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class Url {
    private final String loc;
    private Temporal lastmod;
    private ChangeFreq changefreq;
    private Float priority;

    public Url(String loc) {
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }

    public Temporal getLastmod() {
        return lastmod;
    }

    void setLastmod(Temporal lastmod) {
        this.lastmod = lastmod;
    }

    public void setLastmod(LocalDate lastmod) {
        this.lastmod = lastmod;
    }

    public void setLastmod(LocalDateTime lastmod) {
        this.lastmod = lastmod.atOffset(UTC).withNano(0);
    }

    public void setLastmod(OffsetDateTime lastmod) {
        this.lastmod = lastmod;
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
        StringBuilder builder = new StringBuilder();
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
