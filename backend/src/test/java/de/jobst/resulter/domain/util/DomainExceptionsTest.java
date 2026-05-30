package de.jobst.resulter.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionsTest {

    @Test
    void resourceNotFoundException_withMessage_setsMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void resourceNotFoundException_noArg_hasNoMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException();
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void dataLoadException_withMessageAndCause_setsBoth() {
        Throwable cause = new RuntimeException("root");
        DataLoadException ex = new DataLoadException("load failed", cause);
        assertThat(ex.getMessage()).isEqualTo("load failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void dataNotLoadedException_withMessage_setsMessage() {
        DataNotLoadedException ex = new DataNotLoadedException("data not loaded");
        assertThat(ex.getMessage()).isEqualTo("data not loaded");
    }

    @Test
    void optimisticEntityLockException_withMessage_setsMessage() {
        OptimisticEntityLockException ex = new OptimisticEntityLockException("lock conflict");
        assertThat(ex.getMessage()).isEqualTo("lock conflict");
    }

    @Test
    void responseNotFoundException_withMessage_setsMessage() {
        ResponseNotFoundException ex = new ResponseNotFoundException("response missing");
        assertThat(ex.getMessage()).isEqualTo("response missing");
    }

    @Test
    void exceptions_areThrowable() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("x"); })
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> { throw new DataNotLoadedException("x"); })
                .isInstanceOf(DataNotLoadedException.class);
        assertThatThrownBy(() -> { throw new OptimisticEntityLockException("x"); })
                .isInstanceOf(OptimisticEntityLockException.class);
        assertThatThrownBy(() -> { throw new ResponseNotFoundException("x"); })
                .isInstanceOf(ResponseNotFoundException.class);
    }
}
