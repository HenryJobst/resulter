package de.jobst.resulter.application.certificate;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSchemaValidatorTest {

    private final String certificateSchema;
    private final TextFileLoader textFileLoader;
    private final JsonSchemaValidator jsonSchemaValidator;

    JsonSchemaValidatorTest() {
        TextFileLoader schemaLoader = new TextFileLoader();
        certificateSchema = schemaLoader.loadTextFile("schema/certificate_schema.json");
        textFileLoader = new TextFileLoader();
        jsonSchemaValidator = new JsonSchemaValidator();
    }

    @Test
    void validateJsonAgainstSchema_test_layout_1() {
        String json = textFileLoader.loadTextFile("certificate/test_layout_description_1.json");

        ProcessingReport report = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        report.forEach(System.out::println);
        assertTrue(report.isSuccess());
    }

    @Test
    void validateJsonAgainstSchema_test_layout_2() {
        String json = textFileLoader.loadTextFile("certificate/test_layout_description_2.json");

        ProcessingReport report = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        report.forEach(System.out::println);
        assertTrue(report.isSuccess());
    }

    @Test
    void validateJsonAgainstSchema_test_layout_3() {
        String json = textFileLoader.loadTextFile("certificate/test_layout_description_3.json");

        ProcessingReport report = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        report.forEach(System.out::println);
        assertTrue(report.isSuccess());
    }
}
