package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;

import java.io.IOException;

public class JsonSchemaValidator {

    public ProcessingReport validateJsonAgainstSchema(String json, String schema) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode schemaNode = objectMapper.readTree(schema);
            JsonNode jsonNode = objectMapper.readTree(json);

            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonValidator validator = factory.getValidator();

            return validator.validate(schemaNode, jsonNode, true);
        } catch (JsonParseException e) {
            ProcessingReport report = new ListProcessingReport();
            ProcessingMessage message = new ProcessingMessage();
            message.setMessage("Failed to parse json: " + e.getMessage());
            try {
                report.error(message);
            } catch (ProcessingException ex) {
                throw new RuntimeException(ex);
            }
            return report;
        } catch (IOException | ProcessingException e) {
            throw new RuntimeException("Failed to validate json against schema", e);
        }
    }
}
