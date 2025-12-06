export interface MistakeReactionPair {
    mistakeLegNumber: number
    mistakeSegmentLabel: string
    mistakePI: number
    mistakeSeverity: 'moderate' | 'major' | 'severe'
    reactionLegNumber: number
    reactionSegmentLabel: string
    reactionPI: number
    mri: number
    classification: 'panic' | 'ice_man' | 'resigner' | 'chain_error'
}
