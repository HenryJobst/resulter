<script setup lang="ts">
import Message from 'primevue/message'

const props = defineProps<{ message: string | null }>()
</script>

<template>
    <Message v-if="props.message" severity="error" class="my-3" style="width: 100%">
        {{ props.message }}
    </Message>
</template>

<style scoped></style>
