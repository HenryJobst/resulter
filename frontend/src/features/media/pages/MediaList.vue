<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { mediaService } from '@/features/media/services/media.service'

const authStore = useAuthStore()
const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const queryKey: string = 'media'
const entityLabel: string = 'media'
const settingStoreSuffix: string = 'media'
const listLabel = computed(() => t('labels.media', 2))
const columns: GenericListColumn[] = [
    {
        label: 'labels.preview',
        field: 'thumbnailContent',
        type: 'image',
        sortable: false,
        filterable: false,
    },
    {
        label: 'labels.fileName',
        field: 'fileName',
        sortable: true,
        filterable: true,
        filterType: 'input',
    },
    {
        label: 'labels.contentType',
        field: 'contentType',
        sortable: true,
        filterable: true,
        filterType: 'input',
    },
    { label: 'labels.fileSize', field: 'fileSize', sortable: true },
    {
        label: 'labels.description',
        field: 'description',
        sortable: true,
        filterable: true,
        filterType: 'input',
    },
]
</script>

<template>
    <GenericList
        :entity-service="mediaService"
        :query-key="queryKey"
        :list-label="listLabel"
        :entity-label="entityLabel"
        router-prefix="media"
        :settings-store-suffix="settingStoreSuffix"
        :columns="columns"
        :changeable="authStore.isAdmin"
        filter-display="row"
        :visible="authStore.isAuthenticated"
        :edit-enabled="true"
        :delete-enabled="true"
    />
</template>

<style scoped></style>
