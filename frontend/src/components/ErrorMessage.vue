<script setup lang="ts">
import InlineMessage from 'primevue/inlinemessage'

const props = defineProps<{ message: string | null }>()
</script>

<template>
    <InlineMessage v-if="props.message" severity="error" class="my-3" style="width: 100%">
        {{ props.message }}
    </InlineMessage>
</template>

<style scoped></style>
