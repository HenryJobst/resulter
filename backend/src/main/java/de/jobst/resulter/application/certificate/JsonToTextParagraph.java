package de.jobst.resulter.application.certificate;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.List;

public class JsonToTextParagraph {

    public static Pair<DocumentDefinition, List<ParagraphDefinition>> loadDefinitions(String jsonSource,
                                                                                      boolean isFilePath) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Block.class, new BlockDeserializer());
        module.addDeserializer(TextBlock.class, new TextBlockDeserializer());

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(module)
                .build();
        JsonNode root;

        try {
            if (isFilePath) {
                root = mapper.readTree(new File(jsonSource));
            } else {
                root = mapper.readTree(jsonSource);
            }
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Certificate layout description couldn't be loaded.", e);
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
