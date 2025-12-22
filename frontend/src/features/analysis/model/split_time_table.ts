/**
 * TypeScript models for Winsplits-style split-time table
 */

/**
 * Main split-time table structure
 */
export interface SplitTimeTable {
    groupByType: string // "CLASS" or "COURSE"
    groupId: string // class name or course ID as string
    groupNames: string[] // class names included in this table
    controlCodes: string[] // ["S", "101", "102", ..., "F"]
    rows: SplitTimeTableRow[]
    metadata: SplitTimeTableMetadata
}

/**
 * Single row in the split-time table representing one runner
 */
export interface SplitTimeTableRow {
    personId: number
    personName: string // "Lastname, Firstname"
    className: string
    cells: SplitTimeTableCell[]
    hasIncompleteSplits: boolean // true if runner has missing split data
}

/**
 * Single cell in the split-time table for a runner at a specific control
 */
export interface SplitTimeTableCell {
    controlCode: string

    // Cumulative data (total time from start to this control)
    cumulativeTime: number | null // null if missing split
    cumulativePosition: number | null // rank at this control

    // Segment data (time from previous control to this control)
    segmentTime: number | null // null if missing or for start control
    segmentPosition: number | null // rank for this segment

    // Error detection (individual PI-based)
    isError: boolean // true if segment-PI > Normal-PI + threshold
    errorSeverity: ErrorSeverity // severity level for visual highlighting
    errorMagnitude: number | null // how much worse than normal (segment-PI - Normal-PI)

    // Best time indicators
    isBestCumulative: boolean // true if this is the best cumulative time
    isBestSegment: boolean // true if this is the best segment time
}

/**
 * Metadata about the split-time table for data quality assessment
 */
export interface SplitTimeTableMetadata {
    totalRunners: number
    runnersWithCompleteSplits: number
    totalControls: number
    reliableData: boolean // true if >= 5 runners (statistical validity threshold)
}

/**
 * Error severity levels for gradient coloring of mistake cells
 * Based on how much the segment-PI exceeds the runner's Normal-PI
 */
export enum ErrorSeverity {
    NONE = 'NONE', // No error detected
    LOW = 'LOW', // 5-15% worse than normal
    MEDIUM = 'MEDIUM', // 15-30% worse than normal
    HIGH = 'HIGH', // 30-50% worse than normal
    SEVERE = 'SEVERE', // 50%+ worse than normal
}

/**
 * Available options for split-time table grouping
 */
export interface SplitTimeTableOptions {
    classes: ClassGroupOption[]
    courses: CourseGroupOption[]
}

/**
 * Class grouping option for selection
 */
export interface ClassGroupOption {
    className: string
    runnerCount: number
}

/**
 * Course grouping option for selection
 */
export interface CourseGroupOption {
    courseId: number
    courseName: string
    classNames: string[]
    runnerCount: number
}
