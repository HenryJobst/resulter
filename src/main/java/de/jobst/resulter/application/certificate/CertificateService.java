package de.jobst.resulter.application.certificate;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TabAlignment;
import de.jobst.resulter.application.MediaFileService;
import de.jobst.resulter.domain.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
@Slf4j
public class CertificateService {

    public static final String SCHEMA_CERTIFICATE_SCHEMA_JSON = "schema/certificate_schema.json";

    @Getter
    private final String certificateSchema;

    private final JsonSchemaValidator jsonSchemaValidator;

    @Value("#{'${resulter.media-file-path}'}")
    private String mediaFilePath;

    public CertificateService() {
        TextFileLoader schemaLoader = new TextFileLoader();
        certificateSchema = schemaLoader.loadTextFile(SCHEMA_CERTIFICATE_SCHEMA_JSON);
        jsonSchemaValidator = new JsonSchemaValidator();
    }

    public CertificateService(String mediaFilePath) {
        this.mediaFilePath = mediaFilePath;
        TextFileLoader schemaLoader = new TextFileLoader();
        certificateSchema = schemaLoader.loadTextFile(SCHEMA_CERTIFICATE_SCHEMA_JSON);
        jsonSchemaValidator = new JsonSchemaValidator();
    }

    private static Text createTextBlock(
            TextBlock textBlock, PdfFont font, PdfFont boldFont, PdfFont italicFont, PdfFont boldItalicFont) {
        Text text = new Text(textBlock.text().content());
        setGlobalFonts(textBlock, boldFont, italicFont, boldItalicFont, text);
        setLocalFont(textBlock, text);
        setFontSize(textBlock, text);
        setColor(textBlock, text);
        return text;
    }

    private static void setFontSize(TextBlock textBlock, Text text) {
        if (textBlock.text().fontSize() != null) {
            text.setFontSize(textBlock.text().fontSize());
        }
    }

    private static void setColor(TextBlock textBlock, Text text) {
        if (textBlock.text().color() != null) {
            text.setFontColor(new DeviceRgb(
                    textBlock.text().color().r(),
                    textBlock.text().color().g(),
                    textBlock.text().color().b()));
        }
    }

    private static void setLocalFont(TextBlock textBlock, Text text) {
        // local font definition overrides global font definition
        if (textBlock.text().font() != null) {
            PdfFont textFont = CertificateService.getPdfFont(textBlock.text().font());
            if (textFont != null) {
                text.setFont(textFont);
            }
            if (textBlock.text().bold()) {
                text.setBold();
            }
            if (textBlock.text().italic()) {
                text.setItalic();
            }
        }
    }

    private static void setGlobalFonts(
            TextBlock textBlock, PdfFont boldFont, PdfFont italicFont, PdfFont boldItalicFont, Text text) {
        if (textBlock.text().bold()) {
            if (textBlock.text().italic()) {
                if (boldItalicFont != null) {
                    text.setFont(boldItalicFont);
                } else {
                    text.setBold();
                    text.setItalic();
                }
            } else {
                if (boldFont != null) {
                    text.setFont(boldFont);
                } else {
                    text.setBold();
                }
            }
        } else if (textBlock.text().italic()) {
            if (italicFont != null) {
                text.setFont(italicFont);
            } else {
                text.setItalic();
            }
        }
    }

    private static void applyTabStops(ParagraphDefinition paragraphDefinition, Paragraph paragraph) {
        if (paragraphDefinition.tabStops() != null) {
            paragraphDefinition.tabStops().stream()
                    .map(tabStopDefinition -> new TabStop(
                            tabStopDefinition.position(), TabAlignment.valueOf(tabStopDefinition.alignment())))
                    .forEach(paragraph::addTabStops);
        } else {
            paragraph.addTabStops(new TabStop(261.5f, TabAlignment.CENTER));
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

    public static FontProgram loadFont(String fontNameOrPath) {
        InputStream fontStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/" + fontNameOrPath);
        if (fontStream == null) {
            throw new RuntimeException("Font file not found: " + fontNameOrPath);
        }

        FontProgram fontProgram;
        try {
            fontProgram = FontProgramFactory.createFont(fontStream.readAllBytes(), true);
        } catch (IOException e) {
            throw new RuntimeException("Error loading font: " + fontNameOrPath, e);
        } finally {
            try {
                fontStream.close();
            } catch (IOException e) {
                log.error("Error closing InputStream", e);
            }
        }
        return fontProgram;
    }

    @Nullable
    private static PdfFont getPdfFont(String fontNameOrPath) {
        PdfFont font = null;
        if (fontNameOrPath != null) {
            try {
                if (StandardFonts.isStandardFont(fontNameOrPath)) {
                    font = PdfFontFactory.createFont(fontNameOrPath);
                } else {
                    FontProgram fontProgram = loadFont(fontNameOrPath);
                    font = PdfFontFactory.createFont(
                            fontProgram, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
                }
            } catch (IOException e) {
                log.error(MessageFormat.format("Error loading font: {0}", fontNameOrPath), e);
            }
        }
        return font;
    }

    private Image createImageBlock(MediaBlock mediaParagraph) throws MalformedURLException {
        Path basePath = Paths.get(mediaFilePath);
        ImageData imageData =
                ImageDataFactory.create(basePath.resolve(mediaParagraph.media()).toString());
        Image image = new Image(imageData);
        image.setWidth(mediaParagraph.width());
        return image;
    }

    private Paragraph createParagraph(
            ParagraphDefinition paragraphDefinition,
            PdfFont font,
            PdfFont boldFont,
            PdfFont italicFont,
            PdfFont boldItalicFont) {
        Paragraph paragraph = getParagraph(paragraphDefinition);
        applyTabStops(paragraphDefinition, paragraph);
        applyBlocks(paragraphDefinition, paragraph, font, boldFont, italicFont, boldItalicFont);
        return paragraph;
    }

    private void applyBlocks(
            ParagraphDefinition paragraphDefinition,
            Paragraph paragraph,
            PdfFont font,
            PdfFont boldFont,
            PdfFont italicFont,
            PdfFont boldItalicFont) {
        if (paragraphDefinition.blocks() != null) {
            paragraphDefinition.blocks().stream()
                    .sorted(Comparator.comparingInt(ParagraphDefinition.ParagraphDefinitionBlock::getTabPosition))
                    .map(block -> {
                        if (block.block() instanceof TextBlock textBlock) {
                            return createTextBlock(textBlock, font, boldFont, italicFont, boldItalicFont);
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

    public Certificate createCertificate(Event event, @NonNull EventCertificate eventCertificate, @NonNull MediaFileService mediaFileService) {
        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        PersonRaceResult prr = PersonRaceResult.of(
                "H35-",
                p.getId().value(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                Double.valueOf("1934"),
                1L,
                (byte) 1,
                ResultStatus.OK);
        return createCertificate(p, Optional.of(organisation), event, eventCertificate, prr, mediaFileService);
    }

    public Certificate createCertificate(
            @NonNull Person person,
            Optional<Organisation> organisation,
            @NonNull Event event,
            @NonNull EventCertificate eventCertificate,
            @NonNull PersonRaceResult personResult,
            @NonNull
            MediaFileService mediaFileService) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdfDocument, pageSize);

        String filename = MessageFormat.format(
                "Certificate_{0}_{1}_{2}.pdf",
                event.getName().value().replace("\n", ""),
                person.getPersonName().familyName().value(),
                person.getPersonName().givenName().value());

        ProcessingReport report = jsonSchemaValidator.validateJsonAgainstSchema(
                Objects.requireNonNull(eventCertificate.getLayoutDescription()).value(), certificateSchema);

        if (!report.isSuccess()) {
            filename = "LayoutDescriptionErrors.pdf";
            log.debug("Error validating layout description: " + report);
            document.add(new Paragraph("Error validating layout description: " + report));
        } else {
            PdfCanvas canvas = new PdfCanvas(pdfDocument.addNewPage());
            Path basePath = Paths.get(mediaFilePath);
            try {
                if (eventCertificate.getBlankCertificate() != null) {
                    MediaFileId blankCertificateId =
                            Objects.requireNonNull(eventCertificate).getBlankCertificate();
                    MediaFile blankCertificate = mediaFileService.getById(blankCertificateId);
                    ImageData image = ImageDataFactory.create(basePath.resolve(Objects.requireNonNull(blankCertificate)
                                    .getMediaFileName()
                                    .value())
                            .toString());
                    canvas.addImageFittedIntoRectangle(image, pageSize, false);
                }
            } catch (MalformedURLException e) {
                log.error("Error loading blank certificate image", e);
            }

            var documentAndParagraphDefinitionsWithPlaceholders = JsonToTextParagraph.loadDefinitions(
                    Objects.requireNonNull(eventCertificate.getLayoutDescription())
                            .value(),
                    false);
            DocumentDefinition documentDefinition = documentAndParagraphDefinitionsWithPlaceholders.getLeft();

            MarginsDefinition margins = new MarginsDefinition(30.0f, 30.0f, 20.0f, 20.0f);
            PdfFont font = null;
            PdfFont boldFont = null;
            PdfFont italicFont = null;
            PdfFont boldItalicFont = null;
            if (documentDefinition != null) {
                if (Objects.nonNull(documentDefinition.margins())) {
                    margins = documentDefinition.margins();
                }
                document.setMargins(margins.top(), margins.right(), margins.bottom(), margins.left());
                font = getPdfFont(documentDefinition.font());
                if (font != null) {
                    document.setFont(font);
                }
                boldFont = getPdfFont(documentDefinition.boldFont());
                italicFont = getPdfFont(documentDefinition.italicFont());
                boldItalicFont = getPdfFont(documentDefinition.boldItalicFont());
            }

            float center = (pageSize.getWidth() - document.getLeftMargin() - document.getRightMargin()) / 2;
            System.out.println("Center: " + center);

            List<ParagraphDefinition> paragraphDefinitionsWithPlaceholders =
                    documentAndParagraphDefinitionsWithPlaceholders.getRight();

            List<ParagraphDefinition> paragraphDefinitions = TextBlockProcessor.processPlaceholders(
                    paragraphDefinitionsWithPlaceholders, person, organisation.orElse(null), event, personResult);

            PdfFont finalFont = font;
            PdfFont finalBoldFont = boldFont;
            PdfFont finalItalicFont = italicFont;
            PdfFont finalBoldItalicFont = boldItalicFont;
            paragraphDefinitions.stream()
                    .map(paragraphDefinition -> createParagraph(
                            paragraphDefinition, finalFont, finalBoldFont, finalItalicFont, finalBoldItalicFont))
                    .forEach(document::add);
        }

        document.close();

        byte[] pdfContents = byteArrayOutputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfContents);

        return new Certificate(filename, resource, pdfContents.length);
    }

    public record Certificate(String filename, ByteArrayResource resource, int size) {}
}
