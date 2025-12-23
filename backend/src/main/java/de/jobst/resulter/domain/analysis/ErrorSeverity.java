package de.jobst.resulter.domain.analysis;

/**
 * Error severity levels for gradient coloring of mistake cells.
 * Based on how much the segment-PI exceeds the runner's Normal-PI.
 */
public enum ErrorSeverity {
    NONE,     // No error detected
    LOW,      // 5-15% worse than normal
    MEDIUM,   // 15-30% worse than normal
    HIGH,     // 30-50% worse than normal
    SEVERE    // 50%+ worse than normal
}
