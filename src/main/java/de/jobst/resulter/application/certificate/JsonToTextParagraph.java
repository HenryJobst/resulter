package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonToTextParagraph {

    public static List<TextParagraph> loadTextParagraphs(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));
        JsonNode paragraphsNode = root.get("paragraphs");
        if (paragraphsNode == null || !paragraphsNode.isArray()) {
            throw new IllegalArgumentException("Die JSON-Datei muss ein 'paragraphs'-Array enthalten.");
        }
        TypeFactory typeFactory = mapper.getTypeFactory();
        return mapper.convertValue(paragraphsNode,
            typeFactory.constructCollectionType(List.class, TextParagraph.class));
    }
}
