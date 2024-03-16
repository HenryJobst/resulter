package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonToTextParagraph {

    public static List<Paragraph> loadParagraphs(String jsonSource, boolean isFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Paragraph.class, new ParagraphDeserializer());
        mapper.registerModule(module);
        JsonNode root;
        if (isFilePath) {
            root = mapper.readTree(new File(jsonSource));
        } else {
            root = mapper.readTree(jsonSource);
        }
        JsonNode paragraphsNode = root.get("paragraphs");
        if (paragraphsNode == null || !paragraphsNode.isArray()) {
            throw new IllegalArgumentException("Die JSON-Datei muss ein 'paragraphs'-Array enthalten.");
        }
        TypeFactory typeFactory = mapper.getTypeFactory();
        return mapper.convertValue(paragraphsNode, typeFactory.constructCollectionType(List.class, Paragraph.class));
    }
}
