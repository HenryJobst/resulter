package de.jobst.resulter.application;

import de.jobst.resulter.application.certificate.CertificateServiceImpl;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Properties;

class ResultListServiceTest {

    private final String mediaFilePath;

    private final MediaFileService mediaFileService;

    public ResultListServiceTest() throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        String rawValue = properties.getProperty("resulter.media-file-path");
        Dotenv dotenv = Dotenv.load();
        String resulterMediaFilePath = dotenv.get("RESULTER_MEDIA_FILE_PATH");
        mediaFilePath = rawValue.replace("${RESULTER_MEDIA_FILE_PATH}", resulterMediaFilePath);
        mediaFileService = Mockito.mock(MediaFileService.class);
    }

    @Test
    void createCertificate() throws IOException {

        Person p = Person.of("Mustermann", "Max", null, Gender.M);
        Organisation organisation = Organisation.of("Kaulsdorfer OLV Berlin", "KOLV");
        Event e = Event.of("BBM Mittel-OL 2024");
        String layoutDescription =
                Files.readString(Path.of("src/test/resources/certificate" + "/test_layout_description_3" + ".json"));

        MediaFile mediaFile = MediaFile.of(1L,
                "Urkunde_BTFB_2023.jpg", "thumbnails/Urkunde_BTFB_2023.thumbnail.jpg", "image/jpeg", 100000L);

        Mockito.when(mediaFileService.getById(mediaFile.getId())).thenReturn(mediaFile);

        EventCertificate eventCertificate = EventCertificate.of(
                EventCertificateId.empty().value(),
                "Test-Zertifikat",
                e.getId(),
                layoutDescription,
                mediaFile.getId(),
                true);

        PersonRaceResult prr = PersonRaceResult.of(
                "H35-",
                p.id().value(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                Double.valueOf("1934"),
                1L,
                (byte) 1,
                ResultStatus.OK);
        CertificateServiceImpl.Certificate certificate = new CertificateServiceImpl(this.mediaFilePath)
                .createCertificate(p, organisation, e, eventCertificate, prr, mediaFileService);

        File file = new File(certificate.filename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(certificate.resource().getByteArray());
            fileOutputStream.flush();
        }
    }
}
