package de.jobst.resulter.application.certificate;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class BlockDeserializer extends ValueDeserializer<Block> {

    @Override
    public Block deserialize(JsonParser jp, DeserializationContext deserializationContext) throws JacksonException {
        JsonNode node = jp.objectReadContext().readTree(jp);
        if (node.has("text")) {
            return deserializationContext.readTreeAsValue(node, TextBlock.class);
        } else if (node.has("media")) {
            return deserializationContext.readTreeAsValue(node, MediaBlock.class);
        }
        throw new RuntimeException("Unknown block type");
    }
}
