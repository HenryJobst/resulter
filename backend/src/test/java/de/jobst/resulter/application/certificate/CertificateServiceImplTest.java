package de.jobst.resulter.application.certificate;

import de.jobst.resulter.application.port.CertificateService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CertificateServiceImplTest {

    private static final String INVALID_LAYOUT = "{\"invalid\": true}";
    private static final String LAYOUT_WITH_DOCUMENT =
            "{\"document\":{\"font\":\"Courier\"},\"paragraphs\":[]}";

    private static PersonRaceResult samplePrr() {
        return PersonRaceResult.of("H21", 1L, null, null, 100.0, 1L, (byte) 1, ResultStatus.OK);
    }

    @Test
    void createCertificate_withDefaultLayout_returnsCertificate() {
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, null, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).startsWith("Certificate_");
        assertThat(result.size()).isPositive();
    }

    @Test
    void createCertificate_withInvalidLayout_returnsErrorCertificate() {
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, INVALID_LAYOUT, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).isEqualTo("LayoutDescriptionErrors.pdf");
        assertThat(result.size()).isPositive();
    }

    @Test
    void createCertificate_withDocumentFont_setsFont() {
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, LAYOUT_WITH_DOCUMENT, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).startsWith("Certificate_");
    }

    @Test
    void createCertificate_withTextBlockVariants_coversAllFontBranches() {
        // Covers setGlobalFonts, setLocalFont, setColor, setFontSize, applyTabStops, applyBlocks
        String layout = "{\"paragraphs\":[{" +
                "\"marginTop\":10,\"marginBottom\":5,\"marginLeft\":0,\"marginRight\":0," +
                "\"tabStops\":[{\"position\":261.5,\"alignment\":\"CENTER\"}]," +
                "\"blocks\":[" +
                "{\"block\":{\"text\":\"BoldText\",\"bold\":true,\"italic\":false,\"fontSize\":16," +
                "\"fontRGBColor\":{\"r\":100,\"g\":50,\"b\":200}},\"tabPosition\":1}," +
                "{\"block\":{\"text\":\"ItalicText\",\"bold\":false,\"italic\":true},\"tabPosition\":2}," +
                "{\"block\":{\"text\":\"BoldItalic\",\"bold\":true,\"italic\":true},\"tabPosition\":3}," +
                "{\"block\":{\"text\":\"FontText\",\"font\":\"Courier\"},\"tabPosition\":4}" +
                "]}]}";
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, layout, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).startsWith("Certificate_");
    }

    @Test
    void createCertificate_simpleOverload_returnsCertificate() {
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, null, null, false);

        CertificateService.Certificate result =
                service.createCertificate(event, cert, mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.size()).isPositive();
    }

    @Test
    void noArgsConstructor_loadsCertificateSchema() {
        CertificateServiceImpl service = new CertificateServiceImpl();
        assertThat(service.getCertificateSchema()).isNotBlank();
    }

    @Test
    void createCertificate_withDocumentMargins_appliesMargins() {
        String layout = "{\"document\":{\"margins\":{\"top\":40,\"bottom\":40,\"left\":20,\"right\":20}}," +
                "\"paragraphs\":[]}";
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, layout, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).startsWith("Certificate_");
    }

    @Test
    void createCertificate_withDocumentBoldItalicFonts_coversNonNullFontBranches() {
        // Covers setGlobalFonts with non-null boldFont/italicFont/boldItalicFont
        // Covers applyTabStops else-Branch (kein tabStops in Paragraph)
        String layout = "{" +
                "\"document\":{\"boldFont\":\"Helvetica-Bold\",\"italicFont\":\"Helvetica-Oblique\"," +
                "\"boldItalicFont\":\"Helvetica-BoldOblique\"}," +
                "\"paragraphs\":[{\"blocks\":[" +
                "{\"block\":{\"text\":\"Bold\",\"bold\":true,\"italic\":false},\"tabPosition\":1}," +
                "{\"block\":{\"text\":\"Italic\",\"bold\":false,\"italic\":true},\"tabPosition\":2}," +
                "{\"block\":{\"text\":\"BoldItalic\",\"bold\":true,\"italic\":true},\"tabPosition\":3}" +
                "]}]}";
        CertificateServiceImpl service = new CertificateServiceImpl("/tmp");
        Person person = Person.of("Doe", "Jane", null, Gender.F);
        Organisation org = Organisation.of("TestOrg", "TO");
        Event event = Event.of("TestEvent");
        EventCertificate cert = EventCertificate.of(1L, "Test", null, layout, null, false);

        CertificateService.Certificate result =
                service.createCertificate(person, org, event, cert, samplePrr(), mock(MediaFileService.class));

        assertThat(result).isNotNull();
        assertThat(result.filename()).startsWith("Certificate_");
    }
}
