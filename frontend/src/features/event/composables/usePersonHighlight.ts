import { ref } from 'vue'

const highlightedPersonId = ref<number | null>(null)
const highlightMode = ref<'bright' | 'light' | null>(null)

export function usePersonHighlight() {
    function highlightPerson(personId: number, duration: number = 5000) {
        highlightedPersonId.value = personId
        highlightMode.value = 'bright'

        // Fade zu light nach 100ms
        setTimeout(() => {
            if (highlightedPersonId.value === personId) {
                highlightMode.value = 'light'
            }
        }, 100)

        // Entferne Highlight nach duration
        setTimeout(() => {
            if (highlightedPersonId.value === personId) {
                highlightedPersonId.value = null
                highlightMode.value = null
            }
        }, duration)
    }

    function clearHighlight() {
        highlightedPersonId.value = null
        highlightMode.value = null
    }

    function isHighlighted(personId: number): 'bright' | 'light' | null {
        if (highlightedPersonId.value === personId) {
            return highlightMode.value
        }
        return null
    }

    return {
        highlightedPersonId,
        highlightMode,
        highlightPerson,
        clearHighlight,
        isHighlighted,
    }
}
