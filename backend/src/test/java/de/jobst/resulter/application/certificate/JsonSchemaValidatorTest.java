package de.jobst.resulter.application.certificate;

import com.networknt.schema.Error;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        List<Error> errors = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        errors.forEach(System.out::println);
        assertTrue(errors.isEmpty(), "Validation should succeed with no errors");
    }

    @Test
    void validateJsonAgainstSchema_test_layout_2() {
        String json = textFileLoader.loadTextFile("certificate/test_layout_description_2.json");

        List<Error> errors = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        errors.forEach(System.out::println);
        assertTrue(errors.isEmpty(), "Validation should succeed with no errors");
    }

    @Test
    void validateJsonAgainstSchema_test_layout_3() {
        String json = textFileLoader.loadTextFile("certificate/test_layout_description_3.json");

        List<Error> errors = jsonSchemaValidator.validateJsonAgainstSchema(json, certificateSchema);

        errors.forEach(System.out::println);
        assertTrue(errors.isEmpty(), "Validation should succeed with no errors");
    }
}
