<script setup lang="ts">
import { defineProps, type PropType } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import Spinner from '@/components/SpinnerComponent.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Button from 'primevue/button'
import { useI18n } from 'vue-i18n'
import { useToast } from 'primevue/usetoast'
import { toastDisplayDuration } from '@/utils/constants'
import type { GenericListColumn } from '@/features/generic/models/GenericListColumn'
import type { IGenericService } from '@/features/generic/services/IGenericService'

const props = defineProps({
  entityService: Object as () => IGenericService<any>,
  queryKey: Array as PropType<(string | number)[]>,
  listLabel: String,
  entityLabel: String,
  routerPrefix: String,
  columns: Array as () => GenericListColumn[],
  changeable: Boolean
})

const { t } = useI18n()

const queryClient = useQueryClient()

const entityQuery = useQuery({
  queryKey: props.queryKey,
  queryFn: () => props.entityService?.getAll(t)
})

const toast = useToast()

const deleteMutation = useMutation({
  mutationFn: (id: number) => props.entityService!.deleteById(id, t),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: props.queryKey })
    toast.add({
      severity: 'info',
      summary: t('messages.success'),
      detail: t('messages.entity_deleted', { entity: props.entityLabel }),
      life: toastDisplayDuration
    })
  }
})

const deleteEntity = (id: number) => {
  deleteMutation.mutate(id)
}

const reload = () => {
  entityQuery.refetch()
  deleteMutation.reset()
}
</script>

<template>
  <div>
    <h1>{{ props.listLabel }}</h1>
    <div class="flex justify-content-between my-4">
      <div class="flex justify-content-start">
        <router-link :to="{ name: `${props.routerPrefix}-new` }" v-if="changeable">
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
    <div v-if="entityQuery.status.value === 'pending' || deleteMutation.status.value === 'pending'">
      {{ t('messages.loading') }}
      <Spinner />
    </div>
    <div
      v-else-if="entityQuery.status.value === 'error' || deleteMutation.status.value === 'error'"
    >
      <ErrorMessage :message="t('messages.error', { message: entityQuery.error.value })" />
      <ErrorMessage
        :message="t('messages.error', { message: deleteMutation?.error.value?.message })"
      />
    </div>
    <div v-else-if="entityQuery.data" class="card">
      <DataTable :value="entityQuery.data.value" class="p-datatable-sm">
        <!-- Add Columns Here -->
        <Column
          v-for="col in props.columns"
          :key="col.label"
          :field="col.field"
          :header="t(col.label)"
        >
          <template v-slot:body="slotProps" v-if="col.type === 'list'">
            <div>
              <Chip
                v-for="(item, index) in slotProps.data[col.field]"
                :key="index"
                :label="item[col.listElemField!]"
              />
            </div>
          </template>
          <template v-slot:body="slotProps" v-if="col.type === 'id'">
            {{ slotProps.data[col.field] }}
          </template>
        </Column>
        <!-- ... Other columns ... -->
        <Column class="text-right">
          <template #body="{ data }">
            <router-link :to="{ name: `${props.routerPrefix}-edit`, params: { id: data.id } }">
              <Button
                v-if="changeable"
                icon="pi pi-pencil"
                class="mr-2"
                :label="t('labels.edit')"
                outlined
              />
            </router-link>
            <Button
              v-if="changeable"
              icon="pi pi-trash"
              severity="danger"
              outlined
              :label="t('labels.delete')"
              @click="deleteEntity(data.id)"
            />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
