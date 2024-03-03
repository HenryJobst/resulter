package de.jobst.resulter.application.certificate;

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
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {

    private static Paragraph createParagraph(TextParagraph textParagraph) {
        Paragraph paragraph = new Paragraph(textParagraph.text());
        paragraph.setMarginTop(textParagraph.spacingBefore());
        paragraph.setFontSize(textParagraph.fontSize());
        if (textParagraph.bold()) {
            paragraph.setBold();
        }
        return paragraph;
    }

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

        List<TextParagraph> paragraphsWithPlaceholders =
            JsonToTextParagraph.loadTextParagraphs("src/main/resources/certificate/bbm-mittel-2024.json");

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
