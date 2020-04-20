package com.github.marchenkoprojects.sitemap4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
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
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (isNull(that) || getClass() != that.getClass()) {
            return false;
        }

        Url url = (Url) that;
        return loc.equals(url.loc);
    }

    @Override
    public int hashCode() {
        return loc.hashCode();
    }

    @Override
    public String toString() {
        return
                "<url>" +
                    "<loc>" + loc + "</loc>" +
                    (nonNull(lastmod) ? "<lastmod>" + lastmod + "</lastmod>" : "") +
                    (nonNull(changefreq) ? "<changefreq>" + changefreq.name().toLowerCase() + "</changefreq>" : "") +
                    (nonNull(priority) ? "<priority>" + priority + "</priority>" : "") +
                "</url>";
    }
}
