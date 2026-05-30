package de.jobst.resulter.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConverterUtilsTest {

    @Test
    void converterUtils_canBeInstantiated() {
        assertThat(new ConverterUtils()).isNotNull();
    }

    @Test
    void encodeFileToBase64Binary_encodesFileContentCorrectly(@TempDir Path tempDir) throws IOException {
        byte[] content = "Hello Base64!".getBytes();
        Path file = tempDir.resolve("test.txt");
        Files.write(file, content);

        String encoded = ConverterUtils.encodeFileToBase64Binary(file);

        assertThat(encoded).isEqualTo(Base64.getEncoder().encodeToString(content));
    }

    @Test
    void encodeFileToBase64Binary_nonExistentFile_throwsRuntimeException(@TempDir Path tempDir) {
        Path nonExistent = tempDir.resolve("does-not-exist.txt");

        assertThatThrownBy(() -> ConverterUtils.encodeFileToBase64Binary(nonExistent))
                .isInstanceOf(RuntimeException.class);
    }
}
