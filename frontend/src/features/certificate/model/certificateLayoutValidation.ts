import type { ValidateFunction } from 'ajv'
import Ajv from 'ajv'

const ajv = new Ajv({ allErrors: true, strict: false })
const compiled = new WeakMap<object, ValidateFunction>()

/** Validates a layout against the backend schema and returns readable errors, empty when valid. */
export function validateLayout(schema: object, layout: unknown): string[] {
    let validate = compiled.get(schema)
    if (!validate) {
        validate = ajv.compile(schema)
        compiled.set(schema, validate)
    }
    if (validate(layout)) {
        return []
    }
    return (validate.errors ?? []).map(e => `${e.instancePath || '/'} ${e.message}`)
}
