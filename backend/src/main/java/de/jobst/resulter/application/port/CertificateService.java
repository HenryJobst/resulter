package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;

@PrimaryPort
public interface CertificateService {

    Certificate createCertificate(
            Event event, @NonNull EventCertificate eventCertificate, @NonNull MediaFileService mediaFileService);

    Certificate createCertificate(
        @NonNull Person person,
        @Nullable Organisation organisation,
        @NonNull Event event,
        @NonNull EventCertificate eventCertificate,
        @NonNull PersonRaceResult personResult,
        @NonNull MediaFileService mediaFileService);

    String getCertificateSchema();

    record Certificate(String filename, ByteArrayResource resource, int size) {}
}
