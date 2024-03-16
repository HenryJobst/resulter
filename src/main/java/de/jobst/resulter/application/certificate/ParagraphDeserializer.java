package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class ParagraphDeserializer extends JsonDeserializer<Paragraph> {

    @Override
    public Paragraph deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.has("text")) {
            return jp.getCodec().treeToValue(node, TextParagraph.class);
        } else if (node.has("media")) {
            return jp.getCodec().treeToValue(node, MediaParagraph.class);
        }
        throw new RuntimeException("Unknown paragraph type");
    }
}
