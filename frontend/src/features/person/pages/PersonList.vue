<script setup lang="ts">
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { TableSettings } from '@/features/generic/models/table_settings'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Chip from 'primevue/chip'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/store/auth.store'
import GenericList from '@/features/generic/pages/GenericList.vue'
import { duplicatePersonService, personService } from '@/features/person/services/person.service'

const authStore = useAuthStore()
const { t } = useI18n()

const entityLabel: string = 'person'
const settingStoreSuffix: string = 'person'
const listLabel = computed(() => t('labels.person', 2))
const columns: GenericListColumn[] = [
    { label: 'labels.no', field: 'id', sortable: true, filterable: true, filterType: 'input', style: 'width: 8rem; max-width: 8rem;' },
    { label: 'labels.family_name', field: 'familyName', sortable: true, filterable: true, filterType: 'input', style: 'min-width: 12rem;' },
    { label: 'labels.given_name', field: 'givenName', sortable: true, filterable: true, filterType: 'input', style: 'min-width: 12rem;' },
    { label: 'labels.gender', field: 'gender', type: 'custom', sortable: true, style: 'width: 10rem; max-width: 10rem;' },
    { label: 'labels.birth_year', field: 'birthDate', type: 'year', sortable: true, style: 'width: 10rem; max-width: 10rem;' },
]

const initialTableSettings: TableSettings = {
    first: 0,
    rows: 10,
    page: 0,
    paginator: true,
    paginatorPosition: 'both',
    rowsPerPageOptions: [5, 10, 20, 50, 100, 200, 500],
    sortMode: 'multiple',
    multiSortMeta: undefined,
    sortField: 'familyName',
    sortOrder: 1,
    nullSortOrder: 1,
    defaultSortOrder: 1,
    filters: undefined,
    removableSort: true,
    rowHover: true,
    stateStorage: 'session',
    stateKey: 'PersonList',
    scrollable: true,
    stripedRows: true,
}

const duplicatesOnly = ref(false)
const currentService = computed(() => (duplicatesOnly.value ? duplicatePersonService : personService))
const queryKey = computed(() => (duplicatesOnly.value ? 'persons-duplicates' : 'persons'))
</script>

<template>
    <div v-if="authStore.isAdmin">
        <div class="mb-3 flex align-items-center">
            <Checkbox v-model="duplicatesOnly" :binary="true" input-id="duplicatesOnly" class="mr-2" />
            <label for="duplicatesOnly">{{ t('labels.possible_duplicates') }}</label>
        </div>
        <GenericList
            :entity-service="currentService"
            :query-key="queryKey"
            :list-label="listLabel"
            :entity-label="entityLabel"
            router-prefix="person"
            :settings-store-suffix="settingStoreSuffix"
            :columns="columns"
            :changeable="authStore.isAdmin"
            filter-display="row"
            :visible="authStore.isAdmin"
            :initial-table-settings="initialTableSettings"
        >
            <template #gender="{ value }">
                <Chip v-if="value?.id" class="gender-chip">
                    <span class="gender-name">{{ t(`gender.${value?.id}`) }}</span>
                </Chip>
            </template>
            <template #extra_row_actions="{ value }">
                <router-link
                    v-if="authStore.isAdmin"
                    :to="{
                        name: `person-merge`,
                        params: { id: value.id },
                    }"
                >
                    <Button
                        v-if="authStore.isAdmin"
                        v-tooltip="t('labels.merge')"
                        icon="pi pi-link"
                        class="mr-2"
                        :aria-label="t('labels.merge')"
                        outlined
                        raised
                        rounded
                    />
                </router-link>
            </template>
        </GenericList>
    </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}

/* Gender Chip Styling (Pink) */
.gender-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    margin-right: 0.375rem;
    margin-bottom: 0.125rem;
    padding: 0.125rem 0.5rem;
    border-radius: 6px;
    background: linear-gradient(135deg, rgba(236, 72, 153, 0.08) 0%, rgba(236, 72, 153, 0.02) 100%);
    border: 1px solid rgba(236, 72, 153, 0.2);
    font-size: 0.8125rem;
}

.gender-name {
    font-weight: 500;
    color: rgb(var(--text-primary));
}

/* Force column widths */
:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
    .gender-chip {
        background: linear-gradient(135deg, rgba(236, 72, 153, 0.12) 0%, rgba(236, 72, 153, 0.04) 100%);
        border-color: rgba(236, 72, 153, 0.25);
    }
}
</style>
