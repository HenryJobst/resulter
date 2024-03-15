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

class ResultListServiceTest {

    @Test
    void createCertificate() throws IOException {
        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        Event e = Event.of("Berlin-Brandenburg-Meisterschaft\nim Mittel-OL 2024");
        String layoutDescription = Files.readString(Paths.get("src/test/resources/certificate/bbm-mittel-2024.json"));

        MediaFile mediaFile = MediaFile.of("src/test/resources/certificate/Urkunde_BTFB_2023.jpg",
            "src/test/resources/certificate/Urkunde_BTFB_2023.jpg",
            "image/jpeg",
            100000L);

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
        CertificateService.Certificate certificate =
            new CertificateService().createCertificate(p, Optional.of(organisation), e, eventCertificate, prr);

        File file = new File(certificate.filename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(certificate.resource().getByteArray());
            fileOutputStream.flush();
        }
    }
}
