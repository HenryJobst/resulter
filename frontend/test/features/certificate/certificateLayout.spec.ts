import { readdirSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import process from 'node:process'
import { describe, expect, it } from 'vitest'
import {
    hexToRgb,
    isMediaBlock,
    moveItem,
    newMediaBlockEntry,
    newParagraph,
    parseLayout,
    rgbToHex,
    serializeLayout,
    setOptional,
} from '@/features/certificate/model/certificateLayout'

// vitest runs in the frontend folder
const fixtureDir = `${resolve(process.cwd(), '../backend/src/test/resources/certificate')}/`
// old flat format without "blocks"; no longer valid against the schema
const legacyFixtures = ['bbm-mittel-2024.json']
const fixtures = readdirSync(fixtureDir).filter(f => f.endsWith('.json') && !legacyFixtures.includes(f))

describe('certificateLayout', () => {
    describe('parseLayout', () => {
        it('treats an empty string as an empty layout', () => {
            expect(parseLayout('')).toEqual({ layout: { paragraphs: [] } })
            expect(parseLayout('   \n')).toEqual({ layout: { paragraphs: [] } })
        })

        it('reports invalid JSON', () => {
            expect(parseLayout('{ nope')).toHaveProperty('error')
        })

        it('requires a paragraphs array', () => {
            expect(parseLayout('{}')).toHaveProperty('error')
            expect(parseLayout('{"paragraphs": 1}')).toHaveProperty('error')
            expect(parseLayout('[]')).toHaveProperty('error')
        })

        it('reports a paragraph without blocks array', () => {
            expect(parseLayout('{"paragraphs":[{}]}')).toHaveProperty('error')
        })
    })

    describe('round trip with the backend fixtures', () => {
        it('finds fixtures', () => {
            expect(fixtures.length).toBeGreaterThan(0)
        })

        it('rejects the legacy flat format', () => {
            const raw = readFileSync(`${fixtureDir}${legacyFixtures[0]}`, 'utf-8')
            expect(parseLayout(raw)).toHaveProperty('error')
        })

        it.each(fixtures)('keeps %s unchanged', (name) => {
            const raw = readFileSync(`${fixtureDir}${name}`, 'utf-8')
            const parsed = parseLayout(raw)
            expect(parsed).toHaveProperty('layout')
            if ('layout' in parsed) {
                expect(JSON.parse(serializeLayout(parsed.layout))).toEqual(JSON.parse(raw))
            }
        })
    })

    describe('helpers', () => {
        it('creates a paragraph with one text block', () => {
            const p = newParagraph()
            expect(p.blocks).toHaveLength(1)
            expect(isMediaBlock(p.blocks[0]!.block)).toBe(false)
        })

        it('creates a media block entry', () => {
            expect(isMediaBlock(newMediaBlockEntry().block)).toBe(true)
        })

        it('moves items and ignores moves out of range', () => {
            const a = [1, 2, 3]
            moveItem(a, 1, -1)
            expect(a).toEqual([2, 1, 3])
            moveItem(a, 0, -1)
            moveItem(a, 2, 1)
            expect(a).toEqual([2, 1, 3])
            moveItem(a, 1, 1)
            expect(a).toEqual([2, 3, 1])
        })

        it('removes optional keys that are cleared', () => {
            const o: Record<string, unknown> = { marginTop: 10 }
            setOptional(o, 'marginTop', null)
            expect(o).toEqual({})
            setOptional(o, 'marginTop', 0)
            expect(o).toEqual({ marginTop: 0 })
            setOptional(o, 'font', '')
            expect(o).toEqual({ marginTop: 0 })
        })

        it('converts colors between rgb and hex', () => {
            expect(rgbToHex({ r: 20, g: 0, b: 255 })).toBe('1400ff')
            expect(hexToRgb('1400ff')).toEqual({ r: 20, g: 0, b: 255 })
            expect(hexToRgb('#1400FF')).toEqual({ r: 20, g: 0, b: 255 })
        })
    })
})
