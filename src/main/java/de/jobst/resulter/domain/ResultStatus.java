package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum ResultStatus {
    OK("OK"),
    FINISHED("Finished"),
    MISSING_PUNCH("MissingPunch"),
    DISQUALIFIED("Disqualified"),
    DID_NOT_FINISH("DidNotFinish"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    OVER_TIME("OverTime"),
    SPORTING_WITHDRAWAL("SportingWithdrawal"),
    NOT_COMPETING("NotCompeting"),
    MOVED("Moved"),
    MOVED_UP("MovedUp"),
    DID_NOT_START("DidNotStart"),
    DID_NOT_ENTER("DidNotEnter"),
    CANCELLED("Cancelled");
    private final String value;

    ResultStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResultStatus fromValue(String v) {
        for (ResultStatus c : ResultStatus.values()) {
            if (Objects.equals(c.value, v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
