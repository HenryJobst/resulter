package de.jobst.resulter.domain.analysis;

/**
 * Classification of hanging behavior based on the Hanging Index (HI).
 * Hanging occurs when a runner (passenger) follows a faster runner (bus driver)
 * and achieves better performance than their normal baseline.
 */
public enum HangingClassification {
    /**
     * No hanging behavior detected - runner performed within normal range
     */
    NO_HANGING,

    /**
     * Moderate hanging detected - 1-2 hanging segments or less than 30% of segments
     */
    MODERATE_HANGING,

    /**
     * High hanging detected - 3 or more hanging segments or 30% or more of segments
     */
    HIGH_HANGING,

    /**
     * Insufficient data to determine hanging behavior (e.g., no Normal PI available)
     */
    INSUFFICIENT_DATA
}
