<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`
</script>

<template>
    <h2>{{ t('pages.start') }}</h2>

    <img alt="Logo" class="my-4" src="@/assets/Logo_Resulter.png" width="400" height="400">
</template>

<style scoped></style>
