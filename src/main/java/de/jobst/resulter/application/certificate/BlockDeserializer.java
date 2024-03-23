package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class BlockDeserializer extends JsonDeserializer<Block> {

    @Override
    public Block deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.has("text")) {
            return jp.getCodec().treeToValue(node, TextBlock.class);
        } else if (node.has("media")) {
            return jp.getCodec().treeToValue(node, MediaBlock.class);
        }
        throw new RuntimeException("Unknown block type");
    }
}
