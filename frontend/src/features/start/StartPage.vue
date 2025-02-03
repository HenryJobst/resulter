<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { BackendException, getDetail } from '@/utils/HandleError'
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'
import { useErrorStore } from '@/features/common/stores/useErrorStore'

const { t } = useI18n() // same as `useI18n({ useScope: 'global' })`

const errorStore = useErrorStore()
const messageDetailStore = useMessageDetailStore()

function showErrorDetail(id: number) {
    const error = errorStore.getError(id)
    if (error) {
        const details = getDetail(error.originalError, t)
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

    <img alt="Logo" class="my-4" src="@/assets/Logo_Resulter.png" width="400" height="400">

    <div class="flex flex-row align-items-center">
        <h2 class="ml-3">
            {{ t("labels.message", 2) }}
        </h2>
        <Button
            v-if="errorStore.errorCount > 0"
            icon="pi pi-trash"
            :title="t('labels.deleteAll')"
            outlined
            raised
            class="ml-3"
            @click="errorStore.clearErrors"
        />
    </div>
    <div v-if="errorStore.errorCount === 0" class="ml-3">
        {{ t("messages.noMessages") }}
    </div>
    <table v-if="errorStore.errorCount > 0" class="ml-3">
        <thead>
            <tr>
                <th style="width: 6rem; text-align: left">
                    {{ t("labels.type") }}
                </th>
                <th style="width: 8rem; text-align: left">
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
                <td>{{ error.timestamp }}</td>
                <td>
                    {{
                        error.originalError instanceof BackendException
                            ? error.originalError.message
                            : (error.originalError.name ?? error.originalError.message)
                    }}
                </td>
                <td>
                    <div class="flex flex-row">
                        <Button
                            icon="pi pi-eye"
                            :title="t('labels.detail')"
                            outlined
                            raised
                            @click="showErrorDetail(error.id)"
                        />
                        <Button
                            class="ml-2"
                            icon="pi pi-trash"
                            :title="t('labels.delete')"
                            outlined
                            raised
                            @click="errorStore.removeError(error.id)"
                        />
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</template>

<style scoped></style>
