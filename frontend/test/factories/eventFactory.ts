import {SportEvent} from "../../src/features/event/model/sportEvent";

const defaultEvent: SportEvent = {
    id: 1,
    name: 'Test-Event',
    startTime: new Date(2025, 12, 20, 10, 12, 0, 0),
    state: { id: 'Planned' },
    organisations: [],
    certificate: null,
    hasSplitTimes: false
}

// noinspection JSUnusedGlobalSymbols
export function createEvent(overrides: Partial<SportEvent> = {}): SportEvent {
    return {
        ...defaultEvent, // start with default values
        ...overrides // apply specific changes
    }
}
