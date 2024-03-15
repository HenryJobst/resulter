package de.jobst.resulter.application.certificate;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import de.jobst.resulter.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CertificateService {

    @Value("#{'${resulter.media-file-path}'}")
    private String mediaFilePath;

    private static Paragraph createParagraph(TextParagraph textParagraph) {
        Paragraph paragraph = new Paragraph(textParagraph.text());
        paragraph.setMarginTop(textParagraph.spacingBefore());
        paragraph.setFontSize(textParagraph.fontSize());
        if (textParagraph.bold()) {
            paragraph.setBold();
        }
        return paragraph;
    }

    public Certificate createCertificate(Event event, EventCertificate eventCertificate) throws IOException {
        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        PersonRaceResult prr = PersonRaceResult.of("H35-",
            p.getId().value(),
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            Double.valueOf("1934"),
            1L,
            ResultStatus.OK);
        return createCertificate(p, Optional.of(organisation), event, eventCertificate, prr);
    }

    public Certificate createCertificate(@NonNull Person person,
                                         Optional<Organisation> organisation,
                                         @NonNull Event event,
                                         @NonNull EventCertificate eventCertificate,
                                         @NonNull PersonRaceResult personResult) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdfDocument, pageSize);
        document.setTextAlignment(TextAlignment.CENTER);

        MediaFile blankCertificate = Objects.requireNonNull(eventCertificate).getBlankCertificate();
        PdfCanvas canvas = new PdfCanvas(pdfDocument.addNewPage());
        Path basePath = Paths.get(mediaFilePath);
        ImageData image = ImageDataFactory.create(basePath.resolve(Objects.requireNonNull(blankCertificate)
            .getMediaFileName()
            .value()).toString());
        canvas.addImageFittedIntoRectangle(image, pageSize, false);

        List<TextParagraph> paragraphsWithPlaceholders =
            JsonToTextParagraph.loadTextParagraphs(Objects.requireNonNull(eventCertificate.getLayoutDescription())
                .value(), false);

        List<TextParagraph> paragraphs = TextParagraphProcessor.processPlaceholders(paragraphsWithPlaceholders,
            person,
            organisation.orElse(null),
            event,
            personResult);

        for (TextParagraph tp : paragraphs) {
            document.add(createParagraph(tp));
        }

        document.close();

        byte[] pdfContents = byteArrayOutputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfContents);
        return new Certificate(MessageFormat.format("Certificate_{0}_{1}_{2}.pdf",
            event.getName().value().replace("\n", ""),
            person.getPersonName().familyName().value(),
            person.getPersonName().givenName().value()), resource, pdfContents.length);
    }

    public record Certificate(String filename, ByteArrayResource resource, int size) {}
}
