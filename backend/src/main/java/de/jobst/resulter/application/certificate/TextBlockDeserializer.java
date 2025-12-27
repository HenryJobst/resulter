package de.jobst.resulter.application.certificate;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class TextBlockDeserializer extends ValueDeserializer<TextBlock> {

    @Override
    public TextBlock deserialize(JsonParser jp, DeserializationContext deserializationContext) throws JacksonException {
        JsonNode node = jp.objectReadContext().readTree(jp);
        JsonNode fontSize = node.get("fontSize");
        JsonNode color = node.get("fontRGBColor");
        JsonNode text = node.get("text");
        JsonNode font = node.get("font");
        JsonNode bold = node.get("bold");
        JsonNode italic = node.get("italic");
        return new TextBlock(new TextBlock.Text(text != null ? text.asString() : null,
            font != null ? font.asString() : null,
            bold != null && bold.asBoolean(false),
            italic != null && italic.asBoolean(false),
            fontSize != null ? fontSize.floatValue() : null,
            color != null ?
            new TextBlock.RGBColor(color.get("r").intValue(), color.get("g").intValue(), color.get("b").intValue()) :
            null));
    }
}
