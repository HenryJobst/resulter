import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import process from 'node:process'
import { describe, expect, it } from 'vitest'

// read the raw files: imported JSON is precompiled by the i18n plugin and no longer plain text
const load = (file: string): Tree => JSON.parse(readFileSync(resolve(process.cwd(), 'src/locales', file), 'utf8'))

interface Tree { [key: string]: Tree | string | number | boolean | null | Tree[] | string[] }

function flatten(tree: Tree, prefix = ''): Record<string, unknown> {
    const result: Record<string, unknown> = {}
    for (const [key, value] of Object.entries(tree)) {
        if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
            Object.assign(result, flatten(value as Tree, `${prefix}${key}.`))
        }
        else {
            result[`${prefix}${key}`] = value
        }
    }
    return result
}

const placeholders = (text: unknown) => (typeof text === 'string' ? [...text.matchAll(/\{(\w+)\}/g)].map(m => m[1]).sort() : [])
const choices = (text: unknown) => (typeof text === 'string' ? text.split('|').length : 0)

// Both languages must offer the same texts (docs/adr/0021). Keys whose named parameters differ
// on purpose are listed here with the reason.
const PARAMETER_EXCEPTIONS: Record<string, string> = {
    'errors.internalServerError': 'German text omits {name}; the key is not used with parameters in the code',
    'errors.notFound': 'German text omits {name}; the key is not used with parameters in the code',
}

describe.each([
    ['application texts', load('de.json'), load('en.json')],
    ['component library texts', load('primevue/de.json').de as Tree, load('primevue/en.json').en as Tree],
])('%s', (_name, german, english) => {
    const a = flatten(german)
    const b = flatten(english)

    it('has the same keys in German and English', () => {
        const onlyGerman = Object.keys(a).filter(k => !(k in b))
        const onlyEnglish = Object.keys(b).filter(k => !(k in a))
        expect({ onlyGerman, onlyEnglish }).toEqual({ onlyGerman: [], onlyEnglish: [] })
    })

    it('uses the same named parameters in both texts', () => {
        const different = Object.keys(a)
            .filter(k => k in b && !(k in PARAMETER_EXCEPTIONS))
            .filter(k => placeholders(a[k]).join() !== placeholders(b[k]).join())
        expect(different).toEqual([])
    })

    it('has the same number of plural choices in both texts', () => {
        const different = Object.keys(a)
            .filter(k => k in b && choices(a[k]) !== choices(b[k]))
        expect(different).toEqual([])
    })
})
