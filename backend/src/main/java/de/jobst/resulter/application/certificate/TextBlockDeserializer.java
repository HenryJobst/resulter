package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class TextBlockDeserializer extends JsonDeserializer<TextBlock> {

    @Override
    public TextBlock deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode fontSize = node.get("fontSize");
        JsonNode color = node.get("fontRGBColor");
        JsonNode text = node.get("text");
        JsonNode font = node.get("font");
        JsonNode bold = node.get("bold");
        JsonNode italic = node.get("italic");
        return new TextBlock(new TextBlock.Text(text != null ? text.asText() : null,
            font != null ? font.asText() : null,
            bold != null && bold.asBoolean(false),
            italic != null && italic.asBoolean(false),
            fontSize != null ? fontSize.floatValue() : null,
            color != null ?
            new TextBlock.RGBColor(color.get("r").intValue(), color.get("g").intValue(), color.get("b").intValue()) :
            null));
    }
}
