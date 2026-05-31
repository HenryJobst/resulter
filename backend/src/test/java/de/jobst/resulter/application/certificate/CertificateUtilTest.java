package de.jobst.resulter.application.certificate;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CertificateUtilTest {

    // -------------------------------------------------------------------------
    // TextParagraphsConfig
    // -------------------------------------------------------------------------

    @Test
    void textParagraphsConfig_getterSetterWork() {
        TextParagraphsConfig config = new TextParagraphsConfig();
        assertThat(config.getParagraphs()).isNull();

        TextBlock block = new TextBlock(new TextBlock.Text("hello", null, false, false, 12f, null));
        config.setParagraphs(List.of(block));
        assertThat(config.getParagraphs()).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // ParagraphDefinition.ParagraphDefinitionBlock.getTabPosition()
    // -------------------------------------------------------------------------

    @Test
    void paragraphDefinitionBlock_getTabPosition_withNull_returnsDefault1() {
        Block block = new TextBlock(new TextBlock.Text("x", null, false, false, null, null));
        ParagraphDefinition.ParagraphDefinitionBlock pdb = new ParagraphDefinition.ParagraphDefinitionBlock(block, null);
        assertThat(pdb.getTabPosition()).isEqualTo(1);
    }

    @Test
    void paragraphDefinitionBlock_getTabPosition_withValue_returnsValue() {
        Block block = new TextBlock(new TextBlock.Text("x", null, false, false, null, null));
        ParagraphDefinition.ParagraphDefinitionBlock pdb = new ParagraphDefinition.ParagraphDefinitionBlock(block, 3);
        assertThat(pdb.getTabPosition()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // TextFileLoader
    // -------------------------------------------------------------------------

    @Test
    void textFileLoader_loadExistingFile_returnsContent() {
        TextFileLoader loader = new TextFileLoader();
        // schema-Datei ist auf dem Classpath
        String content = loader.loadTextFile("certificate/test_layout_description_1.json");
        assertThat(content).contains("paragraphs");
    }

    @Test
    void textFileLoader_fileNotFound_throwsIllegalArgumentException() {
        TextFileLoader loader = new TextFileLoader();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadTextFile("not/existing/file.json"))
                .withMessageContaining("File not found");
    }

    // -------------------------------------------------------------------------
    // JsonToTextParagraph — Fehlerpfade
    // -------------------------------------------------------------------------

    @Test
    void jsonToTextParagraph_invalidJson_throwsIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JsonToTextParagraph.loadDefinitions("this is not json", false))
                .withMessageContaining("Certificate layout description couldn't be loaded.");
    }

    @Test
    void jsonToTextParagraph_missingParagraphsKey_throwsIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JsonToTextParagraph.loadDefinitions("{\"document\": {}}", false))
                .withMessageContaining("paragraphs");
    }

    @Test
    void jsonToTextParagraph_paragraphsNotArray_throwsIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JsonToTextParagraph.loadDefinitions("{\"paragraphs\": \"not-an-array\"}", false))
                .withMessageContaining("paragraphs");
    }

    @Test
    void jsonToTextParagraph_withoutDocumentNode_returnsNullDocumentDefinition() {
        var result = JsonToTextParagraph.loadDefinitions("{\"paragraphs\": []}", false);
        assertThat(result.getLeft()).isNull();
        assertThat(result.getRight()).isEmpty();
    }

    @Test
    void jsonToTextParagraph_withFilePath_loadsCorrectly(@TempDir Path tempDir) throws IOException {
        String json = "{\"paragraphs\": [{\"blocks\": [{\"block\": {\"text\": \"Hello\", \"fontSize\": 12.0}}]}]}";
        Path file = tempDir.resolve("layout.json");
        Files.write(file, json.getBytes(StandardCharsets.UTF_8));

        var result = JsonToTextParagraph.loadDefinitions(file.toString(), true);

        assertThat(result.getLeft()).isNull();
        assertThat(result.getRight()).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // BlockDeserializer — unbekannter Block-Typ
    // -------------------------------------------------------------------------

    @Test
    void jsonToTextParagraph_unknownBlockType_throwsException() {
        String json = "{\"paragraphs\": [{\"blocks\": [{\"block\": {\"unknown\": true}}]}]}";
        assertThatThrownBy(() -> JsonToTextParagraph.loadDefinitions(json, false))
                .isInstanceOf(Exception.class);
    }

    // -------------------------------------------------------------------------
    // TextBlockDeserializer — fehlende optionale Felder
    // -------------------------------------------------------------------------

    @Test
    void jsonToTextParagraph_textBlockWithMinimalFields_usesNullDefaults() {
        // Nur text — alle anderen Felder fehlen → null/false defaults
        String json = "{\"paragraphs\": [{\"blocks\": [{\"block\": {\"text\": \"Hello\"}}]}]}";
        var result = JsonToTextParagraph.loadDefinitions(json, false);
        assertThat(result.getRight()).hasSize(1);
        ParagraphDefinition.ParagraphDefinitionBlock pdb = result.getRight().get(0).blocks().get(0);
        TextBlock.Text text = ((TextBlock) pdb.block()).text();
        assertThat(text.content()).isEqualTo("Hello");
        assertThat(text.font()).isNull();
        assertThat(text.bold()).isFalse();
        assertThat(text.italic()).isFalse();
        assertThat(text.fontSize()).isNull();
        assertThat(text.color()).isNull();
    }

    @Test
    void jsonToTextParagraph_textBlockWithFontAndBoldFalse_coversBooleanBranches() {
        // bold=false → bold != null → true, asBoolean → false
        // italic=false → italic != null → true, asBoolean → false
        // font → font != null → true
        String json = "{\"paragraphs\": [{\"blocks\": [{\"block\": {\"text\": \"Hi\", \"font\": \"Arial\", \"bold\": false, \"italic\": false, \"fontSize\": 10}}]}]}";
        var result = JsonToTextParagraph.loadDefinitions(json, false);
        ParagraphDefinition.ParagraphDefinitionBlock pdb = result.getRight().get(0).blocks().get(0);
        TextBlock.Text text = ((TextBlock) pdb.block()).text();
        assertThat(text.font()).isEqualTo("Arial");
        assertThat(text.bold()).isFalse();
        assertThat(text.italic()).isFalse();
    }

    @Test
    void jsonToTextParagraph_textBlockWithItalicTrue_coversItalicTrueBranch() {
        // italic=true → italic != null → true, asBoolean → true
        String json = "{\"paragraphs\": [{\"blocks\": [{\"block\": {\"text\": \"Hi\", \"italic\": true}}]}]}";
        var result = JsonToTextParagraph.loadDefinitions(json, false);
        ParagraphDefinition.ParagraphDefinitionBlock pdb = result.getRight().get(0).blocks().get(0);
        assertThat(((TextBlock) pdb.block()).text().italic()).isTrue();
    }

    @Test
    void jsonToTextParagraph_canBeInstantiated() {
        assertThat(new JsonToTextParagraph()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // JsonToTextParagraph — documentNode is present but not an object
    // -------------------------------------------------------------------------

    @Test
    void jsonToTextParagraph_documentIsArray_returnsNullDocumentDefinition() {
        // document ist Array, nicht Object → documentNode.isObject() → false → null
        String json = "{\"document\": [1, 2, 3], \"paragraphs\": []}";
        var result = JsonToTextParagraph.loadDefinitions(json, false);
        assertThat(result.getLeft()).isNull();
        assertThat(result.getRight()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // TextBlockProcessor — Organisation null und Runtime null
    // -------------------------------------------------------------------------

    @Test
    void textBlockProcessor_withNullOrganisation_replacesOtherPlaceholders() {
        List<ParagraphDefinition.ParagraphDefinitionBlock> blocks = new ArrayList<>();
        blocks.add(new ParagraphDefinition.ParagraphDefinitionBlock(
                new TextBlock(new TextBlock.Text("{{GIVEN_NAME}} {{FAMILY_NAME}} Platz {{RESULT_POSITION}} {{CATEGORY}}",
                        null, false, false, 12f, null)), null));
        List<ParagraphDefinition> paragraphs = new ArrayList<>();
        paragraphs.add(new ParagraphDefinition(List.of(), blocks, null, null, null, null));

        Person person = Person.of("Müller", "Max", LocalDate.of(1990, 1, 1), Gender.M);
        Event event = Event.of("Test Event");
        PersonRaceResult result = PersonRaceResult.of("H21", 1L, null, null, 600.0, 1L, (byte) 1, ResultStatus.OK);

        List<ParagraphDefinition> processed = TextBlockProcessor.processPlaceholders(
                paragraphs, person, null, event, result);

        String text = ((TextBlock) processed.get(0).blocks().get(0).block()).text().content();
        assertThat(text).contains("Max").contains("Müller");
        assertThat(text).doesNotContain("{{GIVEN_NAME}}");
    }

    @Test
    void textBlockProcessor_withNullRuntimeAndTimePlaceholder_skipsTimeReplacement() {
        List<ParagraphDefinition.ParagraphDefinitionBlock> blocks = new ArrayList<>();
        blocks.add(new ParagraphDefinition.ParagraphDefinitionBlock(
                new TextBlock(new TextBlock.Text("{{RESULT_TIME}} {{GIVEN_NAME}}",
                        null, false, false, 12f, null)), null));
        List<ParagraphDefinition> paragraphs = new ArrayList<>();
        paragraphs.add(new ParagraphDefinition(List.of(), blocks, null, null, null, null));

        Person person = Person.of("Müller", "Max", null, Gender.M);
        Event event = Event.of("Test Event");
        // runtime = null → getRuntime().value() == null → Zeit-Ersatz übersprungen
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, null, 1L, (byte) 1, ResultStatus.OK);

        List<ParagraphDefinition> processed = TextBlockProcessor.processPlaceholders(
                paragraphs, person, null, event, prr);

        String text = ((TextBlock) processed.get(0).blocks().get(0).block()).text().content();
        assertThat(text).contains("{{RESULT_TIME}}"); // nicht ersetzt, weil runtime null
        assertThat(text).contains("Max");
    }

    @Test
    void textBlockProcessor_withRuntimeOver60Minutes_usesHhMmSsFormat() {
        // runtime = 3661 Sekunden → 1:01:01 → isAfter(0:59:59) → true → "HH:mm:ss"
        List<ParagraphDefinition.ParagraphDefinitionBlock> blocks = new ArrayList<>();
        blocks.add(new ParagraphDefinition.ParagraphDefinitionBlock(
                new TextBlock(new TextBlock.Text("{{RESULT_TIME}}",
                        null, false, false, 12f, null)), null));
        List<ParagraphDefinition> paragraphs = new ArrayList<>();
        paragraphs.add(new ParagraphDefinition(List.of(), blocks, null, null, null, null));

        Person person = Person.of("Test", "User", null, Gender.M);
        Event event = Event.of("Event");
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 3661.0, 1L, (byte) 1, ResultStatus.OK);

        List<ParagraphDefinition> processed = TextBlockProcessor.processPlaceholders(
                paragraphs, person, null, event, prr);

        String text = ((TextBlock) processed.get(0).blocks().get(0).block()).text().content();
        assertThat(text).isEqualTo("01:01:01");
    }
}
