package de.jobst.resulter.application.certificate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonToTextParagraph {

    public static Pair<DocumentDefinition, List<ParagraphDefinition>> loadDefinitions(String jsonSource,
                                                                                      boolean isFilePath)
        throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Block.class, new BlockDeserializer());
        module.addDeserializer(TextBlock.class, new TextBlockDeserializer());
        mapper.registerModule(module);
        JsonNode root;
        if (isFilePath) {
            root = mapper.readTree(new File(jsonSource));
        } else {
            root = mapper.readTree(jsonSource);
        }

        TypeFactory typeFactory = mapper.getTypeFactory();

        JsonNode documentNode = root.get("document");
        DocumentDefinition documentDefinition = null;
        if (documentNode != null && documentNode.isObject()) {
            documentDefinition = mapper.convertValue(documentNode, DocumentDefinition.class);
        }
        JsonNode paragraphsNode = root.get("paragraphs");
        if (paragraphsNode == null || !paragraphsNode.isArray()) {
            throw new IllegalArgumentException("Die JSON-Datei muss ein 'paragraphs'-Array enthalten.");
        }

        List<ParagraphDefinition> paragraphDefinitions = mapper.convertValue(paragraphsNode,
            typeFactory.constructCollectionType(List.class, ParagraphDefinition.class));

        return Pair.of(documentDefinition, paragraphDefinitions);
    }
}
