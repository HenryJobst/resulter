package de.jobst.resulter.application.certificate;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TabAlignment;
import de.jobst.resulter.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CertificateService {

    @Value("#{'${resulter.media-file-path}'}")
    private String mediaFilePath;

    public CertificateService() {
    }

    public CertificateService(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
    }

    private static Text createTextBlock(TextBlock textBlock) {
        Text text = new Text(textBlock.text().content());
        if (textBlock.text().fontSize() != null) {
            text.setFontSize(textBlock.text().fontSize());
        }
        if (textBlock.text().color() != null) {
            text.setFontColor(new DeviceRgb(textBlock.text().color().r(),
                textBlock.text().color().g(),
                textBlock.text().color().b()));
        }
        if (textBlock.text().bold()) {
            text.setBold();
        }
        return text;
    }

    private Image createImageBlock(MediaBlock mediaParagraph) throws MalformedURLException {
        Path basePath = Paths.get(mediaFilePath);
        ImageData imageData = ImageDataFactory.create(basePath.resolve(mediaParagraph.media()).toString());
        return new Image(imageData, 0, 0, mediaParagraph.width());
    }

    private Paragraph createParagraph(ParagraphDefinition paragraphDefinition) {
        Paragraph paragraph = getParagraph(paragraphDefinition);
        applyTabStops(paragraphDefinition, paragraph);
        applyBlocks(paragraphDefinition, paragraph);
        return paragraph;
    }

    private void applyBlocks(ParagraphDefinition paragraphDefinition, Paragraph paragraph) {
        if (paragraphDefinition.blocks() != null) {
            paragraphDefinition.blocks()
                .stream()
                .sorted(Comparator.comparingInt(ParagraphDefinition.ParagraphDefinitionBlock::getTabPosition))
                .map(block -> {
                    if (block.block() instanceof TextBlock textBlock) {
                        return createTextBlock(textBlock);
                    } else if (block.block() instanceof MediaBlock mediaBlock) {
                        try {
                            return createImageBlock(mediaBlock);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                })
                .forEach(element -> {
                    paragraph.add(new Tab());
                    paragraph.add(element);
                });
        }
    }

    private static void applyTabStops(ParagraphDefinition paragraphDefinition, Paragraph paragraph) {
        if (paragraphDefinition.tabStops() != null) {
            paragraphDefinition.tabStops()
                .stream()
                .map(tabStopDefinition -> new TabStop(tabStopDefinition.position(),
                    TabAlignment.valueOf(tabStopDefinition.alignment())))
                .forEach(paragraph::addTabStops);
        } else {
            paragraph.addTabStops(new TabStop(297.5f, TabAlignment.CENTER));
        }
    }

    @NonNull
    private static Paragraph getParagraph(ParagraphDefinition paragraphDefinition) {
        Paragraph paragraph = new Paragraph();
        if (paragraphDefinition.marginTop() != null) {
            paragraph.setMarginTop(paragraphDefinition.marginTop());
        }
        if (paragraphDefinition.marginBottom() != null) {
            paragraph.setMarginBottom(paragraphDefinition.marginBottom());
        }
        if (paragraphDefinition.marginLeft() != null) {
            paragraph.setMarginLeft(paragraphDefinition.marginLeft());
        }
        if (paragraphDefinition.marginRight() != null) {
            paragraph.setMarginRight(paragraphDefinition.marginRight());
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

        MediaFile blankCertificate = Objects.requireNonNull(eventCertificate).getBlankCertificate();
        PdfCanvas canvas = new PdfCanvas(pdfDocument.addNewPage());
        Path basePath = Paths.get(mediaFilePath);
        ImageData image = ImageDataFactory.create(basePath.resolve(Objects.requireNonNull(blankCertificate)
            .getMediaFileName()
            .value()).toString());
        canvas.addImageFittedIntoRectangle(image, pageSize, false);

        List<ParagraphDefinition> paragraphDefinitionsWithPlaceholders =
            JsonToTextParagraph.loadParagraphDefinitions(Objects.requireNonNull(eventCertificate.getLayoutDescription())
                .value(), false);

        List<ParagraphDefinition> paragraphDefinitionsp = TextBlockProcessor.processPlaceholders(
            paragraphDefinitionsWithPlaceholders,
            person,
            organisation.orElse(null),
            event,
            personResult);

        paragraphDefinitionsp.stream().map(this::createParagraph).forEach(document::add);

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
