import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it } from 'vitest'
import ResultListHeader from '@/features/event/widgets/ResultListHeader.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'
import type { ResultList } from '@/features/event/model/result_list'
import type { Race } from '@/features/race/model/race'

describe('ResultListHeader', () => {
    let mockResultList: ResultList
    let mockRace: Race

    beforeEach(() => {
        mockRace = {
            id: 1,
            name: 'Test Race',
            startTime: new Date('2024-01-15T10:00:00'),
        } as Race

        mockResultList = {
            id: 1,
            createTime: new Date('2024-01-15T12:00:00'),
            status: 'PUBLISHED',
            fileName: 'results.xml',
        } as ResultList
    })

    describe('component rendering', () => {
        it('should render race name', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Test Race')
        })

        it('should render created label', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Erstellt')
        })

        it('should render result list create time', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            const expectedTime = mockResultList.createTime.toLocaleString()
            expect(wrapper.text()).toContain(expectedTime)
        })

        it('should render result list status', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('PUBLISHED')
        })
    })

    describe('different statuses', () => {
        it('should render DRAFT status', () => {
            const draftResultList = {
                ...mockResultList,
                status: 'DRAFT',
            }

            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: draftResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('DRAFT')
        })

        it('should render ARCHIVED status', () => {
            const archivedResultList = {
                ...mockResultList,
                status: 'ARCHIVED',
            }

            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: archivedResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('ARCHIVED')
        })
    })

    describe('edge cases', () => {
        it('should handle different race names', () => {
            const longNameRace = {
                ...mockRace,
                name: 'Very Long Race Name With Many Words',
            }

            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: longNameRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Very Long Race Name With Many Words')
        })

        it('should format create time correctly', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            // Verify that createTime.toLocaleString() is called and rendered
            const displayedTime = mockResultList.createTime.toLocaleString()
            expect(wrapper.text()).toContain(displayedTime)
        })

        it('should render with different time zones', () => {
            const utcTime = new Date('2024-06-15T14:30:00Z')
            const resultListWithUtc = {
                ...mockResultList,
                createTime: utcTime,
            }

            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: resultListWithUtc,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain(utcTime.toLocaleString())
        })
    })

    describe('component structure', () => {
        it('should have multiple div elements', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            const divs = wrapper.findAll('div')
            expect(divs.length).toBeGreaterThanOrEqual(4)
        })

        it('should apply ml-2 class to appropriate divs', () => {
            const wrapper = mount(ResultListHeader, {
                props: {
                    resultList: mockResultList,
                    race: mockRace,
                },
                global: createGlobalMountOptions(),
            })

            const divsWithMargin = wrapper.findAll('.ml-2')
            expect(divsWithMargin.length).toBe(2)
        })
    })
})
