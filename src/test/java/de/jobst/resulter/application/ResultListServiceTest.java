package de.jobst.resulter.application;

import de.jobst.resulter.application.certificate.CertificateService;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Properties;

class ResultListServiceTest {

    private String mediaFilePath;

    public ResultListServiceTest() throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        mediaFilePath = properties.getProperty("resulter.media-file-path");
    }

    @Test
    void createCertificate() throws IOException {

        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        Event e = Event.of("BBM Mittel-OL 2024");
        String layoutDescription =
            Files.readString(Paths.get("src/test/resources/certificate" + "/test_layout_description_3" + ".json"));

        MediaFile mediaFile =
            MediaFile.of("Urkunde_BTFB_2023.jpg", "thumbnails/Urkunde_BTFB_2023.thumbnail.jpg", "image/jpeg", 100000L);

        EventCertificate eventCertificate = EventCertificate.of(EventCertificateId.empty().value(),
            "Test-Zertifikat",
            e,
            layoutDescription,
            mediaFile,
            true);

        PersonRaceResult prr = PersonRaceResult.of("H35-",
            p.getId().value(),
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            Double.valueOf("1934"),
            1L,
            ResultStatus.OK);
        CertificateService.Certificate certificate = new CertificateService(this.mediaFilePath).createCertificate(p,
            Optional.of(organisation),
            e,
            eventCertificate,
            prr);

        File file = new File(certificate.filename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(certificate.resource().getByteArray());
            fileOutputStream.flush();
        }
    }
}
