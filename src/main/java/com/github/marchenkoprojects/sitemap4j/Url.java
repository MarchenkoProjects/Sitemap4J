package com.github.marchenkoprojects.sitemap4j;

import java.time.ZonedDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class Url {
    private final String loc;
    private ZonedDateTime lastmod;
    private ChangeFreqType changefreq;
    private Float priority;

    public Url(String loc) {
        if (isNull(loc) || loc.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.loc = loc;
    }

    public String getLoc() {
        return loc;
    }

    public ZonedDateTime getLastmod() {
        return lastmod;
    }

    public void setLastmod(ZonedDateTime lastmod) {
        this.lastmod = lastmod;
    }

    public ChangeFreqType getChangefreq() {
        return changefreq;
    }

    public void setChangefreq(ChangeFreqType changefreq) {
        this.changefreq = changefreq;
    }

    public Float getPriority() {
        return priority;
    }

    public void setPriority(Float priority) {
        this.priority = priority;
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
