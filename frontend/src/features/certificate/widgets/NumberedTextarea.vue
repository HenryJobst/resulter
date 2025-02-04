<script setup lang="ts">
import { defineModel, onMounted, ref, watch } from 'vue'
import Textarea from 'primevue/textarea'

const modelValue = defineModel({
    type: String,
    default: '',
})

const lineNumbers = ref<number[]>([1])
const lineNumbersRef = ref<HTMLDivElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

function updateLineNumbers() {
    const lines = (modelValue.value || '').split('\n').length
    lineNumbers.value = Array.from({ length: lines }, (_, i) => i + 1)
    console.log('updateLineNumbers')
}

// Scroll-Synchronisation
function syncScroll() {
    if (lineNumbersRef.value && textareaRef.value) {
        lineNumbersRef.value.scrollTop = textareaRef.value.scrollTop
    }
}

// Watcher für Änderungen von innen und außen
watch(() => modelValue, updateLineNumbers, { immediate: true })

watch(modelValue, () => {
    updateLineNumbers()
})

onMounted(updateLineNumbers)
</script>

<template>
    <div class="container">
        <div ref="lineNumbersRef" class="container__lines">
            <div v-for="n in lineNumbers" :key="n">
                {{ n }}
            </div>
        </div>
        <Textarea
            ref="textareaRef"
            v-model="modelValue"
            class="container__textarea"
            rows="20"
            cols="60"
            style="overflow: auto; min-height: 300px;"
            @scroll="syncScroll"
        />
    </div>
</template>

<style scoped>
.container {
    display: flex;
}

.container__lines {
    padding: 8px;
    text-align: right;
    user-select: none;
    background-color: var(--p-textarea-disabled-background);
}

.container__textarea {
    border: none;
    flex: 1;
    padding: 8px;
    font-family: monospace;
    line-height: 1.5;
    resize: none;
    overflow: auto;
}
</style>
