package de.jobst.resulter.application.certificate;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import java.util.List;

public class JsonSchemaValidator {

    public List<Error> validateJsonAgainstSchema(String json, String schemaString) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
                SpecificationVersion.DRAFT_7
        );

        Schema schema = registry.getSchema(schemaString);
        return schema.validate(json, InputFormat.JSON);
    }
}
