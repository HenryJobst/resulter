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

public class TextParagraphProcessor {

    public static List<Paragraph> processPlaceholders(@NonNull List<Paragraph> paragraphs,
                                                      @NonNull Person person,
                                                      @Nullable Organisation organisation,
                                                      @NonNull Event event,
                                                      @NonNull PersonRaceResult personResult) {
        for (Paragraph paragraph : paragraphs) {
            if (!(paragraph instanceof TextParagraph textParagraph)) {
                continue;
            }
            String text = textParagraph.text();

            // Ersetzen der Platzhalter
            text = text.replace("{{GIVEN_NAME}}", person.getPersonName().givenName().value());
            text = text.replace("{{FAMILY_NAME}}", person.getPersonName().familyName().value());
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

            int index = paragraphs.indexOf(textParagraph);
            paragraphs.set(index,
                new TextParagraph(textParagraph.marginTop(),
                    textParagraph.marginLeft(),
                    textParagraph.fontSize(),
                    textParagraph.bold(),
                    text));
        }

        return paragraphs;
    }
}

