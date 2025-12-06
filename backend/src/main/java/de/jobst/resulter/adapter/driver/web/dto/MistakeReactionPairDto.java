package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.MistakeReactionPair;

/**
 * DTO for a mistake-reaction pair in Mental Resilience Index analysis.
 */
public record MistakeReactionPairDto(
        Integer mistakeLegNumber,
        String mistakeSegmentLabel,
        Double mistakePI,
        String mistakeSeverity,
        Integer reactionLegNumber,
        String reactionSegmentLabel,
        Double reactionPI,
        Double mri,
        String classification
) {
    /**
     * Converts domain mistake-reaction pair to DTO.
     *
     * @param pair domain mistake-reaction pair
     * @return DTO
     */
    public static MistakeReactionPairDto from(MistakeReactionPair pair) {
        return new MistakeReactionPairDto(
                pair.mistakeLegNumber(),
                formatSegmentLabel(pair.mistakeFromControl().value(), pair.mistakeToControl().value()),
                pair.mistakePI().value(),
                pair.getMistakeSeverity(),
                pair.reactionLegNumber(),
                formatSegmentLabel(pair.reactionFromControl().value(), pair.reactionToControl().value()),
                pair.reactionPI().value(),
                pair.mri().value(),
                pair.classification().getKey()
        );
    }

    /**
     * Formats segment label as "from → to".
     *
     * @param fromControl from control code
     * @param toControl   to control code
     * @return formatted label
     */
    private static String formatSegmentLabel(String fromControl, String toControl) {
        return fromControl + " → " + toControl;
    }
}
