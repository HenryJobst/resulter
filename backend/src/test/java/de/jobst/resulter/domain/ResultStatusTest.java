package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ResultStatusTest {

    @ParameterizedTest
    @CsvSource({
        "OK, OK",
        "Finished, FINISHED",
        "MissingPunch, MISSING_PUNCH",
        "Disqualified, DISQUALIFIED",
        "DidNotFinish, DID_NOT_FINISH",
        "Active, ACTIVE",
        "Inactive, INACTIVE",
        "OverTime, OVER_TIME",
        "SportingWithdrawal, SPORTING_WITHDRAWAL",
        "NotCompeting, NOT_COMPETING",
        "Moved, MOVED",
        "MovedUp, MOVED_UP",
        "DidNotStart, DID_NOT_START",
        "DidNotEnter, DID_NOT_ENTER",
        "Cancelled, CANCELLED"
    })
    void fromValue_returnsCorrectEnum(String xmlValue, String enumName) {
        ResultStatus status = ResultStatus.fromValue(xmlValue);
        assertThat(status.name()).isEqualTo(enumName);
        assertThat(status.value()).isEqualTo(xmlValue);
    }

    @Test
    void fromValue_throwsForUnknownValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> ResultStatus.fromValue("unknown"));
        assertThatIllegalArgumentException().isThrownBy(() -> ResultStatus.fromValue("ok"));
        assertThatIllegalArgumentException().isThrownBy(() -> ResultStatus.fromValue(""));
    }
}
