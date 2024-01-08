<script setup lang="ts">
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/keycloak/store/auth.store'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { CupService } from '@/features/cup/services/cup.service'
import { useToast } from 'primevue/usetoast'

import { toastDisplayDuration } from '@/utils/constants'

const { t } = useI18n()

const authStore = useAuthStore()

const queryClient = useQueryClient()

const toast = useToast()

const cupMutation = useMutation({
  mutationFn: (id: number) => CupService.deleteById(id, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['cups'] })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.cup_deleted'),
      life: toastDisplayDuration
    })
  }
})

const deleteCup = (id: number) => {
  cupMutation.mutate(id)
}

const cupQuery = useQuery({
  queryKey: ['cups'],
  queryFn: () => CupService.getAll(t)
})

const reload = () => {
  cupQuery.refetch()
  cupMutation.reset()
}
</script>

<template>
  <h1>{{ t('labels.cup', 2) }}</h1>
  <div class="flex justify-content-between my-4">
    <div class="flex justify-content-start">
      <router-link :to="{ name: 'cup-new' }" v-if="authStore.isAdmin">
        <Button icon="pi pi-plus" :label="t('labels.new')" outlined></Button>
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
    <div v-else-if="cupQuery?.data" class="card">
      <DataTable :value="cupQuery?.data.value" class="p-datatable-sm">
        <Column field="name" :header="t('labels.name')" />
        <Column field="type.id" :header="t('labels.type')" />
        <Column :header="t('labels.event', 2)">
          <template #body="slotProps">
            {{ slotProps.data.events.length }}
          </template>
        </Column>
        <Column class="text-right">
          <template #body="slotProps">
            <router-link :to="{ name: 'cup-edit', params: { id: slotProps.data.id } }">
              <Button
                icon="pi pi-pencil"
                class="mr-2"
                :label="t('labels.edit')"
                outlined
                v-if="authStore.isAdmin"
              />
            </router-link>
            <Button
              icon="pi pi-trash"
              severity="danger"
              outlined
              :label="t('labels.delete')"
              @click="deleteCup(slotProps.data.id)"
              v-if="authStore.isAdmin"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped></style>
