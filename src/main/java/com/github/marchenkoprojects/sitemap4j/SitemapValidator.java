package com.github.marchenkoprojects.sitemap4j;

import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * @author Oleg Marchenko
 */
class SitemapValidator {

    void validate(File file) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        try {
            InputStream is = new FileInputStream(file);
            if (file.getName().endsWith(".gz")) {
                is = new GZIPInputStream(is);
            }

            Schema schema = schemaFactory.newSchema(new File("src/main/resources/sitemap.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(is));
        }
        catch (SAXException | IOException e) {
            throw new SitemapNotValidException(e);
        }
    }
}
