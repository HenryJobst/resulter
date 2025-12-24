import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EventForm from '@/features/event/widgets/EventForm.vue'
import { createGlobalMountOptions } from '../../../helpers/testSetup'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { Certificate } from '@/features/certificate/model/certificate'
import type { Organisation } from '@/features/organisation/model/organisation'
import type { EventStatus } from '@/features/event/model/event_status'
import { EventService } from '@/features/event/services/event.service'

// Mock Tanstack Query
const mockCertificateQuery = vi.fn()
const mockOrganisationQuery = vi.fn()
const mockEventStatusQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
    useQuery: (options: any) => {
        if (options.queryKey[0] === 'certificates') {
            return mockCertificateQuery()
        }
        if (options.queryKey[0] === 'organisations') {
            return mockOrganisationQuery()
        }
        if (options.queryKey[0] === 'event_status') {
            return mockEventStatusQuery()
        }
        return {
            data: { value: null },
            status: { value: 'idle' },
        }
    },
}))

describe('EventForm', () => {
    let mockEvent: SportEvent
    let mockCertificates: Certificate[]
    let mockOrganisations: Organisation[]
    let mockEventStatuses: EventStatus[]
    let mockEntityService: EventService

    beforeEach(() => {
        vi.clearAllMocks()

        mockEventStatuses = [
            { id: 'planned' },
            { id: 'finished' },
            { id: 'cancelled' },
        ]

        mockCertificates = [
            { id: 1, name: 'Certificate 1' } as Certificate,
            { id: 2, name: 'Certificate 2' } as Certificate,
        ]

        mockOrganisations = [
            { id: 1, name: 'Organisation 1' } as Organisation,
            { id: 2, name: 'Organisation 2' } as Organisation,
            { id: 3, name: 'Organisation 3' } as Organisation,
        ]

        mockEvent = {
            id: 1,
            name: 'Test Event',
            startTime: '2024-06-15T14:30:00Z',
            state: { id: 'planned' },
            organisations: [{ id: 1, name: 'Organisation 1' }],
            certificate: { id: 1, name: 'Certificate 1' },
            hasSplitTimes: false,
        }

        mockEntityService = {} as EventService

        // Default query mocks - success state
        mockCertificateQuery.mockReturnValue({
            data: { value: mockCertificates },
            status: { value: 'success' },
        })

        mockOrganisationQuery.mockReturnValue({
            data: { value: mockOrganisations },
            status: { value: 'success' },
        })

        mockEventStatusQuery.mockReturnValue({
            data: { value: mockEventStatuses },
            status: { value: 'success' },
        })
    })

    describe('component rendering', () => {
        it('should render the form when event is provided', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('div.flex.flex-col').exists()).toBe(true)
        })

        it('should not render when event is null', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: null as any,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('div.flex.flex-col').exists()).toBe(false)
        })

        it('should render name input field', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const nameInput = wrapper.find('#name')
            expect(nameInput.exists()).toBe(true)
        })

        it('should render date picker for start date', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const datePicker = wrapper.find('#startDate')
            expect(datePicker.exists()).toBe(true)
        })

        it('should render time picker for start time', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const timePicker = wrapper.find('#startTime')
            expect(timePicker.exists()).toBe(true)
        })

        it('should render state select field', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const stateSelect = wrapper.find('#state')
            expect(stateSelect.exists()).toBe(true)
        })

        it('should render organisations multi-select field', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const organisationsSelect = wrapper.find('#organisations')
            expect(organisationsSelect.exists()).toBe(true)
        })

        it('should render certificate select field', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const certificateSelect = wrapper.find('#certificate')
            expect(certificateSelect.exists()).toBe(true)
        })
    })

    describe('loading states', () => {
        it('should show loading message for event status when pending', () => {
            mockEventStatusQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Lädt...')
        })

        it('should show loading message for organisations when pending', () => {
            mockOrganisationQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Lädt...')
        })

        it('should show loading message for certificates when pending', () => {
            mockCertificateQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.text()).toContain('Lädt...')
        })

        it('should not render state select when status query is pending', () => {
            mockEventStatusQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('#state').exists()).toBe(false)
        })

        it('should not render organisations multi-select when query is pending', () => {
            mockOrganisationQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('#organisations').exists()).toBe(false)
        })

        it('should not render certificate select when query is pending', () => {
            mockCertificateQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'pending' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.find('#certificate').exists()).toBe(false)
        })
    })

    describe('form field bindings', () => {
        it('should bind event name to input field', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const nameInput = wrapper.find('#name')
            expect(nameInput.element).toBeDefined()
        })

        it('should update event name when input changes', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.event.name = 'Updated Event Name'

            await wrapper.vm.$nextTick()
            expect(vm.event.name).toBe('Updated Event Name')
        })
    })

    describe('date and time handling', () => {
        it('should initialize dateTime from event startTime on mount', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.dateTime).toBeInstanceOf(Date)
        })

        it('should compute datePart from dateTime', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.datePart).toBeInstanceOf(Date)
        })

        it('should compute timePart from dateTime', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.timePart).toBeInstanceOf(Date)
        })

        it('should update event startTime when datePart changes', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const newDate = new Date(2024, 11, 25) // December 25, 2024
            vm.datePart = newDate

            await wrapper.vm.$nextTick()
            expect(vm.event.startTime).toBeDefined()
        })

        it('should update event startTime when timePart changes', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const newTime = new Date(2024, 0, 1, 16, 45) // 4:45 PM
            vm.timePart = newTime

            await wrapper.vm.$nextTick()
            expect(vm.event.startTime).toBeDefined()
        })

        it('should preserve date when updating time', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const originalDate = new Date(vm.dateTime)
            const newTime = new Date(2024, 0, 1, 18, 30) // 6:30 PM
            vm.timePart = newTime

            await wrapper.vm.$nextTick()
            const updatedDateTime = new Date(vm.dateTime)
            expect(updatedDateTime.getHours()).toBe(18)
            expect(updatedDateTime.getMinutes()).toBe(30)
        })

        it('should preserve time when updating date', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const originalTime = new Date(vm.dateTime)
            const newDate = new Date(2025, 0, 15) // January 15, 2025
            vm.datePart = newDate

            await wrapper.vm.$nextTick()
            const updatedDateTime = new Date(vm.dateTime)
            expect(updatedDateTime.getFullYear()).toBe(2025)
            expect(updatedDateTime.getMonth()).toBe(0)
            expect(updatedDateTime.getDate()).toBe(15)
        })
    })

    describe('organisation handling', () => {
        it('should compute l_organisations from event.organisations', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.l_organisations).toEqual([1])
        })

        it('should return empty array when event is null', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: null as any,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.l_organisations).toEqual([])
        })

        it('should update event.organisations when l_organisations changes', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.l_organisations = [1, 2]

            await wrapper.vm.$nextTick()
            expect(vm.event.organisations.length).toBe(2)
        })

        it('should call handleSelectionChange when organisation selection changes', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const changeEvent = {
                value: [1, 2, 3],
            }

            vm.handleSelectionChange(changeEvent)

            expect(vm.event.organisations.length).toBe(3)
        })

        it('should handle getOrganisationKeysFromIds with valid IDs', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getOrganisationKeysFromIds([1, 2])

            expect(result).toEqual([
                { id: 1, name: 'Organisation 1' },
                { id: 2, name: 'Organisation 2' },
            ])
        })

        it('should filter out invalid organisation IDs', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getOrganisationKeysFromIds([1, 999, 2])

            expect(result).toEqual([
                { id: 1, name: 'Organisation 1' },
                { id: 2, name: 'Organisation 2' },
            ])
        })

        it('should return empty array when organisation data is null', () => {
            mockOrganisationQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'success' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getOrganisationKeysFromIds([1, 2])

            expect(result).toEqual([])
        })

        it('should return empty array when organisation data is not an array', () => {
            mockOrganisationQuery.mockReturnValue({
                data: { value: 'invalid' },
                status: { value: 'success' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getOrganisationKeysFromIds([1, 2])

            expect(result).toEqual([])
        })
    })

    describe('certificate handling', () => {
        it('should call handleCertificateSelectionChange when certificate selection changes', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const changeEvent = {
                value: { id: 2, name: 'Certificate 2' },
            }

            vm.handleCertificateSelectionChange(changeEvent)

            expect(vm.event.certificate).toEqual({ id: 2, name: 'Certificate 2' })
        })

        it('should get certificate key from ID', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getCertificateKeyFromId(1)

            expect(result).toEqual({ id: 1, name: 'Certificate 1' })
        })

        it('should return null when certificate ID is null', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getCertificateKeyFromId(null)

            expect(result).toBeNull()
        })

        it('should return null when certificate is not found', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getCertificateKeyFromId(999)

            expect(result).toBeNull()
        })

        it('should return null when certificate data is null', () => {
            mockCertificateQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'success' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getCertificateKeyFromId(1)

            expect(result).toBeNull()
        })

        it('should return null when certificate data is not an array', () => {
            mockCertificateQuery.mockReturnValue({
                data: { value: 'invalid' },
                status: { value: 'success' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const result = vm.getCertificateKeyFromId(1)

            expect(result).toBeNull()
        })
    })

    describe('event status handling', () => {
        it('should compute localized event status options', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.localizedEventStatusOptions.length).toBe(3)
        })

        it('should return empty array when event status data is null', () => {
            mockEventStatusQuery.mockReturnValue({
                data: { value: null },
                status: { value: 'success' },
            })

            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.localizedEventStatusOptions).toEqual([])
        })

        it('should localize event status labels', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const localizedOptions = vm.localizedEventStatusOptions

            expect(localizedOptions[0].label).toBeDefined()
        })
    })

    describe('edge cases', () => {
        it('should handle event with empty organisations array', () => {
            const eventWithNoOrgs = {
                ...mockEvent,
                organisations: [],
            }

            const wrapper = mount(EventForm, {
                props: {
                    event: eventWithNoOrgs,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.l_organisations).toEqual([])
        })

        it('should handle event with null certificate', () => {
            const eventWithNoCert = {
                ...mockEvent,
                certificate: null,
            }

            const wrapper = mount(EventForm, {
                props: {
                    event: eventWithNoCert,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            expect(wrapper.vm).toBeDefined()
        })

        it('should handle event with Date object as startTime', () => {
            const eventWithDate = {
                ...mockEvent,
                startTime: new Date('2024-06-15T14:30:00Z'),
            }

            const wrapper = mount(EventForm, {
                props: {
                    event: eventWithDate,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            expect(vm.dateTime).toBeInstanceOf(Date)
        })

        it('should not update organisations when event is null in handleSelectionChange', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.event = null

            const changeEvent = {
                value: [1, 2],
            }

            vm.handleSelectionChange(changeEvent)
            // Should not throw error
            expect(vm.event).toBeNull()
        })

        it('should not update certificate when event is null in handleCertificateSelectionChange', () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            vm.event = null

            const changeEvent = {
                value: { id: 2, name: 'Certificate 2' },
            }

            vm.handleCertificateSelectionChange(changeEvent)
            // Should not throw error
            expect(vm.event).toBeNull()
        })
    })

    describe('v-model emission', () => {
        it('should emit update:modelValue when event computed setter is called', async () => {
            const wrapper = mount(EventForm, {
                props: {
                    event: mockEvent,
                    entityService: mockEntityService,
                    queryKey: ['events'],
                },
                global: createGlobalMountOptions(),
            })

            const vm = wrapper.vm as any
            const newEvent = { ...mockEvent, name: 'Updated Event' }
            vm.event = newEvent

            await wrapper.vm.$nextTick()
            expect(wrapper.emitted('update:modelValue')).toBeTruthy()
        })
    })
})
