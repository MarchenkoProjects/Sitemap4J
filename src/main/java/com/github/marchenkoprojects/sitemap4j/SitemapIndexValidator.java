package com.github.marchenkoprojects.sitemap4j;

import java.io.File;

/**
 * @author Oleg Marchenko
 */
class SitemapIndexValidator extends SitemapValidator {

    @Override
    void validate(File file) {
        validate(file, new File("src/main/resources/sitemap-index.xsd"));
    }
}
