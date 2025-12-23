import { beforeEach, describe, expect, it, vi } from 'vitest'
import { EventService } from '@/features/event/services/event.service'
import axiosInstance from '@/features/keycloak/services/api'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { EventResults } from '@/features/event/model/event_results'
import type { EventStatus } from '@/features/event/model/event_status'

// Mock axios instance
vi.mock('@/features/keycloak/services/api', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}))

describe('EventService', () => {
    let eventService: EventService
    const mockT = (key: string) => key

    beforeEach(() => {
        vi.clearAllMocks()
        eventService = new EventService()
    })

    describe('getAll', () => {
        it('should fetch all events and transform startTime to Date', async () => {
            const mockEvents: SportEvent[] = [
                {
                    id: 1,
                    name: 'Event 1',
                    startTime: '2025-01-15T10:00:00' as any,
                    state: { id: 'Planned' },
                },
                {
                    id: 2,
                    name: 'Event 2',
                    startTime: '2025-01-20T14:00:00' as any,
                    state: { id: 'Running' },
                },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({
                data: {
                    content: mockEvents,
                    totalElements: 2,
                    totalPages: 1,
                },
            })

            const result = await eventService.getAll(mockT)

            expect(result).not.toBeNull()
            expect(result?.content).toHaveLength(2)
            expect(result?.content[0].startTime).toBeInstanceOf(Date)
            expect(result?.content[1].startTime).toBeInstanceOf(Date)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await eventService.getAll(mockT)

            expect(result).toBeNull()
        })

        it('should handle events without startTime', async () => {
            const mockEvents: SportEvent[] = [
                { id: 1, name: 'Event 1', state: { id: 'Planned' } },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({
                data: {
                    content: mockEvents,
                    totalElements: 1,
                },
            })

            const result = await eventService.getAll(mockT)

            expect(result).not.toBeNull()
            expect(result?.content[0]).toBeDefined()
        })
    })

    describe('getAllUnpaged', () => {
        it('should fetch all events unpaged and transform dates', async () => {
            const mockEvents: SportEvent[] = [
                {
                    id: 1,
                    name: 'Event 1',
                    startTime: '2025-01-15T10:00:00' as any,
                    state: { id: 'Planned' },
                },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({
                data: mockEvents,
            })

            const result = await eventService.getAllUnpaged(mockT)

            expect(result).toHaveLength(1)
            expect(result?.[0].startTime).toBeInstanceOf(Date)
        })

        it('should return null when response is null', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: null })

            const result = await eventService.getAllUnpaged(mockT)

            expect(result).toBeNull()
        })
    })

    describe('calculate', () => {
        it('should call calculate endpoint for result list', async () => {
            const mockData = { success: true }
            vi.mocked(axiosInstance.put).mockResolvedValue({ data: mockData })

            const result = await EventService.calculate(123, mockT)

            expect(axiosInstance.put).toHaveBeenCalledWith('/result_list/123/calculate')
            expect(result).toEqual(mockData)
        })
    })

    describe('getEventStatus', () => {
        it('should fetch all event statuses', async () => {
            const mockStatuses: EventStatus[] = [
                { id: 'Planned', name: 'Geplant' },
                { id: 'Running', name: 'Läuft' },
                { id: 'Finished', name: 'Beendet' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockStatuses })

            const result = await EventService.getEventStatus(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/event_status')
            expect(result).toEqual(mockStatuses)
        })
    })

    describe('getResultsById', () => {
        it('should fetch event results and transform createTime', async () => {
            const mockResults: EventResults = {
                id: 1,
                name: 'Event 1',
                resultLists: [
                    {
                        id: 1,
                        name: 'Results 1',
                        createTime: '2025-01-15T10:00:00[Europe/Berlin]' as any,
                    },
                    {
                        id: 2,
                        name: 'Results 2',
                        createTime: '2025-01-16T10:00:00[Europe/Berlin]' as any,
                    },
                ],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockResults })

            const result = await EventService.getResultsById('1', mockT)

            expect(result).not.toBeNull()
            expect(result?.resultLists[0].createTime).toBeInstanceOf(Date)
            expect(result?.resultLists[1].createTime).toBeInstanceOf(Date)
        })

        it('should handle results without timezone identifier', async () => {
            const mockResults: EventResults = {
                id: 1,
                name: 'Event 1',
                resultLists: [
                    {
                        id: 1,
                        name: 'Results 1',
                        createTime: '2025-01-15T10:00:00' as any,
                    },
                ],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockResults })

            const result = await EventService.getResultsById('1', mockT)

            expect(result?.resultLists[0].createTime).toBeInstanceOf(Date)
        })
    })

    describe('upload', () => {
        it('should upload form data with correct headers', async () => {
            const formData = new FormData()
            formData.append('file', new Blob(['test']), 'test.xml')
            const mockResponse = { success: true }

            vi.mocked(axiosInstance.post).mockResolvedValue({ data: mockResponse })

            const result = await EventService.upload(formData, mockT)

            expect(axiosInstance.post).toHaveBeenCalledWith(
                '/upload',
                formData,
                { headers: { 'Content-Type': 'multipart/form-data' } },
            )
            expect(result).toEqual(mockResponse)
        })
    })

    describe('getCertificateStats', () => {
        it('should fetch certificate stats for event', async () => {
            const mockStats = {
                totalCertificates: 10,
                generatedCertificates: 7,
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockStats })

            const result = await EventService.getCertificateStats(123, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/event/123/certificate_stats')
            expect(result).toEqual(mockStats)
        })

        it('should return null when id is undefined', async () => {
            const result = await EventService.getCertificateStats(undefined, mockT)

            expect(result).toBeNull()
            expect(axiosInstance.get).not.toHaveBeenCalled()
        })
    })

    describe('removeEventCertificateStat', () => {
        it('should delete event certificate stat', async () => {
            const mockResponse = { success: true }
            vi.mocked(axiosInstance.delete).mockResolvedValue({ data: mockResponse })

            const result = await EventService.removeEventCertificateStat(456, mockT)

            expect(axiosInstance.delete).toHaveBeenCalledWith('/event_certificate_stat/456')
            expect(result).toEqual(mockResponse)
        })

        it('should return null when id is falsy', async () => {
            const result = await EventService.removeEventCertificateStat(0, mockT)

            expect(result).toBeNull()
            expect(axiosInstance.delete).not.toHaveBeenCalled()
        })
    })

    describe('getCertificateSchema', () => {
        it('should fetch certificate schema', async () => {
            const mockSchema = { fields: ['name', 'time', 'rank'] }
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockSchema })

            const result = await EventService.getCertificateSchema(mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/certificate_schema')
            expect(result).toEqual(mockSchema)
        })
    })

    describe('getCupScores', () => {
        it('should fetch cup scores for result list', async () => {
            const mockScores = [
                { cupId: 1, scores: [{ personId: 1, points: 100 }] },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockScores })

            const result = await EventService.getCupScores(123, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith('/result_list/123/cup_score_lists')
            expect(result).toEqual(mockScores)
        })

        it('should return empty array when id is undefined', async () => {
            const result = await EventService.getCupScores(undefined, mockT)

            expect(result).toEqual([])
            expect(axiosInstance.get).not.toHaveBeenCalled()
        })
    })

    describe('getSplitTimeAnalysisRanking', () => {
        it('should fetch split time analysis without filters', async () => {
            const mockAnalysis = [{ personId: 1, splits: [] }]
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            const result = await EventService.getSplitTimeAnalysisRanking(123, false, [], false, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/ranking',
            )
            expect(result).toEqual(mockAnalysis)
        })

        it('should fetch split time analysis with mergeBidirectional', async () => {
            const mockAnalysis = [{ personId: 1, splits: [] }]
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            await EventService.getSplitTimeAnalysisRanking(123, true, [], false, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/ranking?mergeBidirectional=true',
            )
        })

        it('should fetch split time analysis with person filters', async () => {
            const mockAnalysis = [{ personId: 1, splits: [] }]
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            await EventService.getSplitTimeAnalysisRanking(123, false, [1, 2, 3], false, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/ranking?filterPersonIds=1&filterPersonIds=2&filterPersonIds=3',
            )
        })

        it('should fetch split time analysis with all parameters', async () => {
            const mockAnalysis = [{ personId: 1, splits: [] }]
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            await EventService.getSplitTimeAnalysisRanking(123, true, [1, 2], true, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                expect.stringContaining('mergeBidirectional=true'),
            )
            expect(axiosInstance.get).toHaveBeenCalledWith(
                expect.stringContaining('filterIntersection=true'),
            )
            expect(axiosInstance.get).toHaveBeenCalledWith(
                expect.stringContaining('filterPersonIds=1'),
            )
        })
    })

    describe('getPersonsForResultList', () => {
        it('should fetch persons for result list', async () => {
            const mockPersons = [
                { id: 1, firstName: 'John', lastName: 'Doe' },
                { id: 2, firstName: 'Jane', lastName: 'Smith' },
            ]

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockPersons })

            const result = await EventService.getPersonsForResultList(123, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/persons',
            )
            expect(result).toEqual(mockPersons)
        })
    })

    describe('getMentalResilienceAnalysis', () => {
        it('should fetch mental resilience analysis without filters', async () => {
            const mockAnalysis = {
                averageRecoveryTime: 45,
                resilenceScore: 0.85,
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            const result = await EventService.getMentalResilienceAnalysis(123, [], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/mental_resilience',
            )
            expect(result).toEqual(mockAnalysis)
        })

        it('should fetch mental resilience analysis with person filters', async () => {
            const mockAnalysis = { resilenceScore: 0.85 }
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            await EventService.getMentalResilienceAnalysis(123, [1, 2, 3], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/mental_resilience?filterPersonIds=1&filterPersonIds=2&filterPersonIds=3',
            )
        })
    })

    describe('getAnomalyDetectionAnalysis', () => {
        it('should fetch anomaly detection analysis', async () => {
            const mockAnalysis = { anomalies: [] }
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            const result = await EventService.getAnomalyDetectionAnalysis(123, [], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/anomaly_detection',
            )
            expect(result).toEqual(mockAnalysis)
        })

        it('should include person filters in request', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: {} })

            await EventService.getAnomalyDetectionAnalysis(123, [5, 10], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/anomaly_detection?filterPersonIds=5&filterPersonIds=10',
            )
        })
    })

    describe('getHangingDetectionAnalysis', () => {
        it('should fetch hanging detection analysis', async () => {
            const mockAnalysis = { hangingSegments: [] }
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockAnalysis })

            const result = await EventService.getHangingDetectionAnalysis(123, [], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/hanging_detection',
            )
            expect(result).toEqual(mockAnalysis)
        })

        it('should include person filters in request', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: {} })

            await EventService.getHangingDetectionAnalysis(123, [7, 8, 9], mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/hanging_detection?filterPersonIds=7&filterPersonIds=8&filterPersonIds=9',
            )
        })
    })

    describe('getSplitTimeTable', () => {
        it('should fetch split time table by class', async () => {
            const mockTable = {
                groupByType: 'class',
                rows: [],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockTable })

            const result = await EventService.getSplitTimeTable(123, 'class', 'H21', mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/split_table?groupBy=class&groupId=H21',
            )
            expect(result).toEqual(mockTable)
        })

        it('should URL encode groupId parameter', async () => {
            vi.mocked(axiosInstance.get).mockResolvedValue({ data: {} })

            await EventService.getSplitTimeTable(123, 'class', 'D 18/20', mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                expect.stringContaining('groupId=D%2018%2F20'),
            )
        })
    })

    describe('getSplitTableOptions', () => {
        it('should fetch available split table options', async () => {
            const mockOptions = {
                classes: [{ id: 'H21', name: 'Herren 21' }],
                courses: [{ id: 1, name: 'Course A' }],
            }

            vi.mocked(axiosInstance.get).mockResolvedValue({ data: mockOptions })

            const result = await EventService.getSplitTableOptions(123, mockT)

            expect(axiosInstance.get).toHaveBeenCalledWith(
                '/split_time_analysis/result_list/123/split_table/options',
            )
            expect(result).toEqual(mockOptions)
        })
    })
})
