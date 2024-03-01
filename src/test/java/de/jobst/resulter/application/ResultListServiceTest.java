package de.jobst.resulter.application;

import com.itextpdf.text.DocumentException;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

class ResultListServiceTest {

    private final CertificateService certificateService;

    ResultListServiceTest() {
        this.certificateService = new CertificateService();
    }

    @Test
    void createCertificate() throws DocumentException, IOException {
        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        Event e = Event.of("Berlin-Brandenburg-Meisterschaft\nim Mittel-OL 2024");
        PersonRaceResult prr = PersonRaceResult.of("H35-",
            p.getId().value(),
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            Double.valueOf("1934"),
            1L,
            ResultStatus.OK);
        CertificateService.Certificate certificate =
            certificateService.createCertificate(p, Optional.of(organisation), e, prr);

        File file = new File(certificate.filename());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(certificate.resource().getByteArray());
        fileOutputStream.flush();
    }
}
