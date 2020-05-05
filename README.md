# Sitemap4J
Sitemap4J is a Java library that implements the [**Sitemap XML protocol**](https://www.sitemaps.org/protocol.html).
## Getting started ##
### Installation ###
**Maven:**
```xml
<dependency>
  <groupId>com.github.marchenkoprojects</groupId>
  <artifactId>sitemap4j</artifactId>
  <version>0.1</version>
</dependency>
```
**Gradle:**
```groovy
compile("com.github.marchenkoprojects:sitemap4j:0.1")
```
### Using ###
**Sitemap:**
```java
Sitemap sitemap = new Sitemap(new File("sitemap.xml"));
sitemap.load();
sitemap.addUrl("http://example.com/page1.html");
sitemap.addUrl("http://example.com/page2.html");
sitemap.addUrl("http://example.com/page3.html");
sitemap.flush();
```
**SitemapIndex:**
```java
Sitemap sitemap = new SitemapIndex(new File("sitemap-index.xml"), "http://example.com");
sitemap.load();
sitemap.addUrl("/page1.html");
sitemap.addUrl("/page2.html");
sitemap.addUrl("/page3.html");
sitemap.flush();
```