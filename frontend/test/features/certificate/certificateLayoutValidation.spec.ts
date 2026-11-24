import { readdirSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import process from 'node:process'
import { describe, expect, it } from 'vitest'
import { validateLayout } from '@/features/certificate/model/certificateLayoutValidation'

// vitest runs in the frontend folder
const backend = resolve(process.cwd(), '../backend/src')
const schema = JSON.parse(readFileSync(`${backend}/main/resources/schema/certificate_schema.json`, 'utf-8'))
const fixtureDir = `${backend}/test/resources/certificate/`
const legacyFixtures = ['bbm-mittel-2024.json']
const fixtures = readdirSync(fixtureDir).filter(f => f.endsWith('.json') && !legacyFixtures.includes(f))

const block = (b: object) => ({ blocks: [{ block: b }] })

describe('validateLayout', () => {
    it.each(fixtures)('accepts the backend fixture %s', (name) => {
        const layout = JSON.parse(readFileSync(`${fixtureDir}${name}`, 'utf-8'))
        expect(validateLayout(schema, layout)).toEqual([])
    })

    it('rejects the legacy flat format', () => {
        const layout = JSON.parse(readFileSync(`${fixtureDir}${legacyFixtures[0]}`, 'utf-8'))
        expect(validateLayout(schema, layout).length).toBeGreaterThan(0)
    })

    it('accepts an empty layout', () => {
        expect(validateLayout(schema, { paragraphs: [] })).toEqual([])
    })

    it('reports the path of a wrong value', () => {
        const errors = validateLayout(schema, { paragraphs: [block({ text: 'x', fontSize: 'big' })] })
        expect(errors).toHaveLength(1)
        expect(errors[0]).toContain('/paragraphs/0/blocks/0/block/fontSize')
    })

    it('reports an unknown font', () => {
        const errors = validateLayout(schema, { paragraphs: [block({ text: 'x', font: 'Comic-Sans' })] })
        expect(errors[0]).toContain('/paragraphs/0/blocks/0/block/font')
    })

    it('reports an unknown tab stop alignment', () => {
        const errors = validateLayout(schema, {
            paragraphs: [{ tabStops: [{ position: 1, alignment: 'MIDDLE' }], blocks: [{ block: { text: 'x' } }] }],
        })
        expect(errors[0]).toContain('alignment')
    })

    it('validates the document margins', () => {
        expect(validateLayout(schema, { document: { margins: { top: 10, left: 12.5 } }, paragraphs: [] })).toEqual([])
        expect(validateLayout(schema, { document: { margins: 'wide' }, paragraphs: [] }).length).toBeGreaterThan(0)
        expect(validateLayout(schema, { document: { margins: { top: 'x' } }, paragraphs: [] }).length).toBeGreaterThan(0)
    })
})
