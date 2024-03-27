<script setup lang="ts">
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useI18n } from 'vue-i18n'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { useToast } from 'primevue/usetoast'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { cupService } from '@/features/cup/services/cup.service'

import { toastDisplayDuration } from '@/utils/constants'

const { t } = useI18n()

const authStore = useAuthStore()

const queryClient = useQueryClient()

const toast = useToast()

const cupMutation = useMutation({
    mutationFn: (id: number) => cupService.deleteById(id, t),
    onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['cups'] })
        toast.add({
            severity: 'info',
            summary: t('messages.success'),
            detail: t('messages.cup_deleted'),
            life: toastDisplayDuration,
        })
    },
})

function deleteCup(id: number) {
    cupMutation.mutate(id)
}

const cupQuery = useQuery({
    queryKey: ['cups'],
    queryFn: () => cupService.getAll(t),
})

function reload() {
    cupQuery.refetch()
    cupMutation.reset()
}
</script>

<template>
    <h1>{{ t('labels.cup', 2) }}</h1>
    <div class="flex justify-content-between my-4">
        <div class="flex justify-content-start">
            <router-link v-if="authStore.isAdmin" :to="{ name: 'cup-new' }">
                <Button icon="pi pi-plus" :label="t('labels.new')" outlined />
            </router-link>
        </div>
        <Button
            icon="pi pi-refresh"
            :label="t('labels.reload')"
            outlined
            severity="secondary"
            @click="reload"
        />
    </div>

    <div>
        <span v-if="cupQuery?.status.value === 'pending' || cupMutation.status.value === 'pending'">
            {{ t('messages.loading') }}
            <Spinner />
        </span>
        <span v-else-if="cupQuery?.status.value === 'error' || cupMutation.status.value === 'error'">
            <ErrorMessage :message="t('messages.error', { message: cupQuery?.error.value?.message })" />
            <ErrorMessage
                :message="t('messages.error', { message: cupMutation?.error.value?.message })"
            />
        </span>
        <div v-else-if="cupQuery?.data && cupQuery?.data.value" class="card">
            <DataTable :value="cupQuery?.data.value.content" class="p-datatable-sm">
                <Column field="name" :header="t('labels.name')" />
                <Column field="type.id" :header="t('labels.type')" />
                <Column :header="t('labels.event', 2)">
                    <template #body="slotProps">
                        {{ slotProps.data.eventIds.length }}
                    </template>
                </Column>
                <Column class="text-right">
                    <template #body="slotProps">
                        <router-link :to="{ name: 'cup-edit', params: { id: slotProps.data.id } }">
                            <Button
                                v-if="authStore.isAdmin"
                                icon="pi pi-pencil"
                                class="mr-2"
                                :label="t('labels.edit')"
                                outlined
                            />
                        </router-link>
                        <Button
                            v-if="authStore.isAdmin"
                            icon="pi pi-trash"
                            severity="danger"
                            outlined
                            :label="t('labels.delete')"
                            @click="deleteCup(slotProps.data.id)"
                        />
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<style scoped></style>
