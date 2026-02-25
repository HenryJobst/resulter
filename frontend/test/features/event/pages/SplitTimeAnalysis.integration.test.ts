import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import SplitTimeAnalysis from '../../../../src/features/event/pages/SplitTimeAnalysis.vue'
import { cleanupTest, createGlobalMountOptions, mockUseQuery } from '../../../helpers/testSetup'

vi.mock('vue-router', () => ({
    useRouter: vi.fn(() => ({
        back: vi.fn(),
    })),
}))

vi.mock('@tanstack/vue-query', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@tanstack/vue-query')>()
    return {
        ...actual,
        useQuery: vi.fn(),
    }
})

describe('splitTimeAnalysis Integration', () => {
    it('renders sequence section when sequence segments are present', async () => {
        const { useQuery } = await import('@tanstack/vue-query')

        vi.mocked(useQuery)
            .mockReturnValueOnce(mockUseQuery([
                { id: 1, familyName: 'Meyer', givenName: 'Anna' },
                { id: 2, familyName: 'Schulz', givenName: 'Ben' },
            ]))
            .mockReturnValueOnce(mockUseQuery([
                {
                    resultListId: 123,
                    eventId: 1,
                    classResultShortName: 'Alle Klassen',
                    controlSegments: [
                        {
                            fromControl: '31',
                            toControl: '32',
                            segmentLabel: '31 → 32',
                            runnerSplits: [
                                {
                                    personId: 1,
                                    classResultShortName: 'H21',
                                    position: 1,
                                    splitTime: '1:20',
                                    timeBehind: '',
                                    splitTimeSeconds: 80,
                                    reversed: false,
                                },
                            ],
                            classes: ['H21'],
                            bidirectional: false,
                        },
                    ],
                    sequenceSegments: [
                        {
                            controls: ['S', '31', '32'],
                            segmentLabel: 'S → 31 → 32',
                            runnerSplits: [
                                {
                                    personId: 1,
                                    classResultShortName: 'H21',
                                    position: 1,
                                    splitTime: '2:50',
                                    timeBehind: '',
                                    splitTimeSeconds: 170,
                                    legSplitTimes: ['1:30', '1:20'],
                                    legSplitTimesSeconds: [90, 80],
                                },
                                {
                                    personId: 2,
                                    classResultShortName: 'H21',
                                    position: 2,
                                    splitTime: '3:05',
                                    timeBehind: '+0:15',
                                    splitTimeSeconds: 185,
                                    legSplitTimes: ['1:35', '1:30'],
                                    legSplitTimesSeconds: [95, 90],
                                },
                            ],
                            classes: ['H21'],
                        },
                    ],
                },
            ]))

        const wrapper = mount(SplitTimeAnalysis, {
            props: {
                id: '1',
                resultListId: '123',
                eventName: 'Test Event',
                resultListLabel: 'Result List A',
            },
            global: {
                ...createGlobalMountOptions(),
                stubs: {
                    ...createGlobalMountOptions().stubs,
                    Accordion: true,
                    AccordionPanel: true,
                    AccordionHeader: true,
                    AccordionContent: true,
                    DataTable: true,
                    Column: true,
                    Button: true,
                    Checkbox: true,
                    MultiSelect: true,
                    Select: true,
                },
            },
        })

        await nextTick()

        expect(wrapper.exists()).toBe(true)
        expect(wrapper.text()).toContain('labels.sequence_segments')

        cleanupTest(wrapper)
    })
})
