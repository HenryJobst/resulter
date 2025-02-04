package de.jobst.resulter.domain.util;

public class OptimisticEntityLockException extends RuntimeException {
    public OptimisticEntityLockException(String message) {
        super(message);
    }
}
