package de.jobst.resulter.application.certificate;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonRaceResult;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TextBlockProcessor {

    public static List<ParagraphDefinition> processPlaceholders(@NonNull List<ParagraphDefinition> paragraphDefinitions,
                                                                @NonNull Person person,
                                                                @Nullable Organisation organisation,
                                                                @NonNull Event event,
                                                                @NonNull PersonRaceResult personResult) {
        for (ParagraphDefinition paragraphDefinition : paragraphDefinitions) {
            for (ParagraphDefinition.ParagraphDefinitionBlock block : paragraphDefinition.blocks()) {
                if (!(block.block() instanceof TextBlock textBlock)) {
                    continue;
                }
                String text = textBlock.text().content();

                // Ersetzen der Platzhalter
                text = text.replace("{{GIVEN_NAME}}", person.personName().givenName().value());
                text = text.replace("{{FAMILY_NAME}}", person.personName().familyName().value());
                text = text.replace("{{EVENT_NAME}}", event.getName().value());
                text = text.replace("{{RESULT_POSITION}}", String.valueOf(personResult.getPosition().value()));
                text = text.replace("{{CATEGORY}}", personResult.getClassResultShortName().value());

                if (text.contains("{{RESULT_TIME}}")) {
                    LocalTime punchTime = LocalTime.ofSecondOfDay(personResult.getRuntime().value().longValue());
                    String timeFormat = punchTime.isAfter(LocalTime.of(0, 59, 59)) ? "HH:mm:ss" : "mm:ss";
                    text = text.replace("{{RESULT_TIME}}", punchTime.format(DateTimeFormatter.ofPattern(timeFormat)));
                }

                if (organisation != null) {
                    text = text.replace("{{ORGANISATION}}", organisation.getName().value());
                }

                int index = paragraphDefinition.blocks().indexOf(block);
                paragraphDefinition.blocks()
                    .set(index,
                        new ParagraphDefinition.ParagraphDefinitionBlock(new TextBlock(new TextBlock.Text(text,
                            textBlock.text().font(),
                            textBlock.text().bold(),
                            textBlock.text().italic(),
                            textBlock.text().fontSize(),
                            textBlock.text().color())), block.tabPosition()));
            }
        }
        return paragraphDefinitions;
    }
}
