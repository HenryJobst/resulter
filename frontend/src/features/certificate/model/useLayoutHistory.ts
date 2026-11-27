import { computed, ref, watch } from 'vue'

// edits closer together than this (a drag, typing) count as one step
const MERGE_MS = 600

/**
 * Undo / redo for the layout JSON string. `key` identifies the certificate: when it changes the
 * value was loaded, not edited, so the history starts over.
 */
export function useLayoutHistory(
    getValue: () => string,
    setValue: (value: string) => void,
    getKey: () => unknown,
    now: () => number = Date.now,
) {
    const past = ref<string[]>([])
    const future = ref<string[]>([])
    let current = getValue()
    let key = getKey()
    let lastChange = 0

    watch([getValue, getKey], ([value, newKey]) => {
        if (newKey !== key) {
            key = newKey
            current = value
            past.value = []
            future.value = []
            return
        }
        if (value === current) {
            return
        }
        const time = now()
        if (past.value.length === 0 || time - lastChange > MERGE_MS) {
            past.value.push(current)
        }
        lastChange = time
        future.value = []
        current = value
    })

    function apply(value: string) {
        // the watcher then sees nothing new, so the step is not recorded as an edit
        current = value
        setValue(value)
    }

    function undo() {
        const previous = past.value.pop()
        if (previous !== undefined) {
            future.value.push(current)
            apply(previous)
        }
    }

    function redo() {
        const next = future.value.pop()
        if (next !== undefined) {
            past.value.push(current)
            apply(next)
        }
    }

    return { undo, redo, canUndo: computed(() => past.value.length > 0), canRedo: computed(() => future.value.length > 0) }
}
