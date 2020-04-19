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
            throw new IllegalArgumentException("Parameter 'loc' must not be null or empty");
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String loc;
        private ZonedDateTime lastmod;
        private ChangeFreqType changefreq;
        private Float priority;

        public Builder setLoc(String loc) {
            if (isNull(loc) || loc.isEmpty()) {
                throw new IllegalArgumentException("Parameter 'loc' must not be null or empty");
            }
            this.loc = loc;
            return this;
        }

        public Builder setLastmod(ZonedDateTime lastmod) {
            if (isNull(lastmod)) {
                throw new NullPointerException("Parameter 'lastmod' must not be null");
            }
            this.lastmod = lastmod;
            return this;
        }

        public Builder setChangefreq(ChangeFreqType changefreq) {
            if (isNull(changefreq)) {
                throw new NullPointerException("Parameter 'changefreq' must not be null");
            }
            this.changefreq = changefreq;
            return this;
        }

        public Builder setPriority(Float priority) {
            if (isNull(priority)) {
                throw new NullPointerException("Parameter 'priority' must not be null");
            }
            if (priority < 0 || priority > 1) {
                throw new IllegalArgumentException("Parameter 'priority' must be between 0 and 1");
            }
            this.priority = priority;
            return this;
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
