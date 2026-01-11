<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import Chip from 'primevue/chip'
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
        style: 'width: 8rem; max-width: 8rem;',
    },
    {
        label: 'labels.fileName',
        field: 'fileName',
        sortable: true,
        filterable: true,
        filterType: 'input',
        style: 'min-width: 15rem;',
    },
    {
        label: 'labels.contentType',
        field: 'contentType',
        type: 'custom',
        sortable: true,
        filterable: true,
        filterType: 'input',
        style: 'width: 12rem; max-width: 12rem;',
    },
    { label: 'labels.fileSize', field: 'fileSize', sortable: true, style: 'width: 8rem; max-width: 8rem;' },
    {
        label: 'labels.description',
        field: 'description',
        sortable: true,
        filterable: true,
        filterType: 'input',
        style: 'min-width: 15rem;',
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
    >
        <template #contentType="{ value }">
            <Chip class="contenttype-chip">
                <span class="contenttype-name">{{ value }}</span>
            </Chip>
        </template>
    </GenericList>
</template>

<style scoped>
/* ContentType Chip Styling (Teal) */
.contenttype-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(20, 184, 166, 0.08) 0%, rgba(20, 184, 166, 0.02) 100%);
    border: 1px solid rgba(20, 184, 166, 0.2);
    font-size: 0.8125rem;
}

.contenttype-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Force column widths */
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
    white-space: nowrap;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .contenttype-chip {
        background: linear-gradient(135deg, rgba(20, 184, 166, 0.12) 0%, rgba(20, 184, 166, 0.04) 100%);
        border-color: rgba(20, 184, 166, 0.25);
    }
}
</style>
