package com.github.marchenkoprojects.sitemap4j;

import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

import static java.util.Objects.isNull;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * @author Oleg Marchenko
 */
public class SitemapValidator {

    public void validate(File file) {
        if (isNull(file)) {
            throw new NullPointerException("Parameter 'file' must not be null");
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(new File("src/main/resources/sitemap.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(file));
        }
        catch (SAXException | IOException e) {
            throw new SitemapNotValidException(e);
        }
    }
}
