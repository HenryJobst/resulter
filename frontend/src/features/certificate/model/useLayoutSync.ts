import type { CertificateLayout } from '@/features/certificate/model/certificateLayout'
import { ref, watch } from 'vue'
import { parseLayout, serializeLayout } from '@/features/certificate/model/certificateLayout'

/**
 * Keeps a parsed, editable layout in sync with the layout JSON string of the parent.
 * Loading a value never emits it again (that would reformat the JSON without any edit),
 * and our own update does not reload the layout.
 */
export function useLayoutSync(getValue: () => string, emit: (value: string) => void) {
    const layout = ref<CertificateLayout | null>(null)
    const error = ref<string | null>(null)
    // the serialized layout the editor and the parent value agree on
    let lastSerialized: string | null = null

    function load(json: string) {
        const result = parseLayout(json)
        if ('error' in result) {
            error.value = result.error
            layout.value = null
        }
        else {
            error.value = null
            lastSerialized = serializeLayout(result.layout)
            layout.value = result.layout
        }
    }

    watch(
        getValue,
        (value) => {
            if (value !== lastSerialized) {
                load(value)
            }
        },
        { immediate: true },
    )

    watch(
        layout,
        (value) => {
            if (!value) {
                return
            }
            const serialized = serializeLayout(value)
            if (serialized !== lastSerialized) {
                lastSerialized = serialized
                emit(serialized)
            }
        },
        { deep: true },
    )

    return { layout, error }
}
