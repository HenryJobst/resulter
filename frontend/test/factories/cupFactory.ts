import type { Cup } from '../../src/features/cup/model/cup'

const defaultCup: Cup = {
    id: 1,
    name: 'Test-Cup 2025',
    type: { id: 'KRISTALL', name: 'Kristall-Cup' },
    year: 2025,
    events: [],
}

// noinspection JSUnusedGlobalSymbols
export function createCup(overrides: Partial<Cup> = {}): Cup {
    return {
        ...defaultCup, // start with default values
        ...overrides, // apply specific changes
    }
}
