package de.jobst.resulter.domain;

import lombok.Setter;
import org.springframework.lang.NonNull;

import java.io.BufferedInputStream;

public class BlankCertificate {

    @NonNull
    @Setter
    private BlankCertificateId id;

    @NonNull
    private BufferedInputStream image;
}
