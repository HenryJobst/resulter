<script setup lang="ts">
import Button from 'primevue/button'
import Panel from 'primevue/panel'
import { useI18n } from 'vue-i18n'
import { useErrorStore } from '@/features/common/stores/useErrorStore'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'
import { formatDateAndTime } from '@/features/generic/services/GenericFunctions'
import { BackendException, getDetail } from '@/utils/HandleError'

const { t, locale } = useI18n() // same as `useI18n({ useScope: 'global' })`

const errorStore = useErrorStore()
const messageDetailStore = useMessageDetailStore()

async function showErrorDetail(id: number) {
    const error = errorStore.getError(id)
    if (error) {
        const details = await getDetail(error.originalError, t)
        if (details) {
            messageDetailStore.show(details)
        }
        else {
            messageDetailStore.hide()
        }
    }
}
</script>

<template>
    <h2>{{ t('pages.start') }}</h2>

    <img alt="Logo" class="my-4" src="@/assets/Logo_Resulter_400px.webp" width="400" height="396">

    <Panel :header="t('labels.message', 2)" class="my-4" toggleable>
        <template #icons>
            <Button
                v-if="errorStore.errorCount > 0"
                id="deleteAll"
                icon="pi pi-trash"
                severity="secondary"
                outlined
                raised
                rounded
                text
                :title="t('labels.deleteAll')"
                :aria-label="t('labels.deleteAll')"
                @click="errorStore.clearErrors"
            />
        </template>
        <div class="flex flex-row align-items-center" />
        <div v-if="errorStore.errorCount === 0" class="ml-3">
            {{ t("messages.noMessages") }}
        </div>
        <table v-if="errorStore.errorCount > 0" class="ml-3">
            <thead>
                <tr>
                    <th style="width: 6rem; text-align: left">
                        {{ t("labels.type") }}
                    </th>
                    <th style="width: 15rem; text-align: left">
                        {{ t("labels.timestamp") }}
                    </th>
                    <th style="width: 30rem; text-align: left">
                        {{ t("labels.message") }}
                    </th>
                    <th style="width: 4rem; text-align: left">
                        {{ t("labels.action") }}
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="error in errorStore.errors" :key="error.id">
                    <td>{{ t("labels.error") }}</td>
                    <td>{{ formatDateAndTime(error.timestamp ?? new Date(Date.now()), locale) }}</td>
                    <td>
                        {{
                            error.originalError instanceof BackendException
                                ? (error.originalError as BackendException).message
                                : ((error.originalError as Error).name ?? (error.originalError as Error).message)
                        }}
                    </td>
                    <td>
                        <div class="flex flex-row">
                            <Button
                                icon="pi pi-eye"
                                :title="t('labels.detail')"
                                :aria-label="t('labels.detail')"
                                outlined
                                raised
                                rounded
                                @click="showErrorDetail(error.id)"
                            />
                            <Button
                                class="ml-2"
                                icon="pi pi-trash"
                                :title="t('labels.delete')"
                                :aria-label="t('labels.delete')"
                                outlined
                                raised
                                rounded
                                @click="errorStore.removeError(error.id)"
                            />
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </Panel>
</template>

<style scoped></style>
