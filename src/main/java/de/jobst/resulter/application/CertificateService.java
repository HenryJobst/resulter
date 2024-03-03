package de.jobst.resulter.application;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonRaceResult;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CertificateService {

    public record Certificate(String filename, ByteArrayResource resource, int size) {}

    public Certificate createCertificate(Person person,
                                         Optional<Organisation> organisation,
                                         Event event,
                                         PersonRaceResult personResult) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdfDocument, pageSize);
        document.setTextAlignment(TextAlignment.CENTER);

        String BG_IMAGE = "src/main/resources/certificate/Urkunde_BTFB_2023.jpg";

        PdfCanvas canvas = new PdfCanvas(pdfDocument.addNewPage());
        canvas.addImageFittedIntoRectangle(ImageDataFactory.create(BG_IMAGE), pageSize, false);

        Paragraph paragraph = new Paragraph(MessageFormat.format("{0} {1}",
            person.getPersonName().givenName().value(),
            person.getPersonName().familyName().value()));
        formatParagraph(paragraph, 155, 28, true);
        document.add(paragraph);

        if (organisation.isPresent()) {
            paragraph = new Paragraph(MessageFormat.format("{0}", organisation.get().getName().value()));
            formatParagraph(paragraph, 0, 24, false);
            document.add(paragraph);
        }

        paragraph = new Paragraph("belegte bei der");
        formatParagraph(paragraph, 10, 24, false);
        document.add(paragraph);
        paragraph = new Paragraph(MessageFormat.format("{0}", event.getName().value()));
        formatParagraph(paragraph, 20, 24, true);
        document.add(paragraph);

        paragraph =
            new Paragraph(MessageFormat.format("in der Kategorie {0}", personResult.getClassResultShortName().value()));
        formatParagraph(paragraph, 30, 24, false);
        document.add(paragraph);

        LocalTime punchTime = LocalTime.ofSecondOfDay(personResult.getRuntime().value().longValue());
        paragraph = new Paragraph(MessageFormat.format("mit der Zeit {0}",
            punchTime.format(DateTimeFormatter.ofPattern(punchTime.isAfter(LocalTime.of(0, 59, 59)) ?
                                                         "HH:mm:ss" :
                                                         "mm:ss"))));
        formatParagraph(paragraph, 0, 24, false);
        document.add(paragraph);

        paragraph = new Paragraph(MessageFormat.format("den {0}. Platz", personResult.getPosition().value()));
        formatParagraph(paragraph, 30, 24, true);
        document.add(paragraph);

        document.close();

        byte[] pdfContents = byteArrayOutputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfContents);
        return new Certificate(MessageFormat.format("Certificate_{0}_{1}_{2}.pdf",
            event.getName().value().replace("\n", ""),
            person.getPersonName().familyName().value(),
            person.getPersonName().givenName().value()), resource, pdfContents.length);
    }

    private static void formatParagraph(Paragraph paragraph, int spacingBefore, int fontSize, boolean bold) {
        paragraph.setMarginTop(spacingBefore);
        paragraph.setFontSize(fontSize);
        if (bold) {
            paragraph.setBold();
        }
    }
}
