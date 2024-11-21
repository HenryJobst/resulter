<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MultiSelect from 'primevue/multiselect'
import { useQuery } from '@tanstack/vue-query'
import type { Cup } from '@/features/cup/model/cup'
import { eventService } from '@/features/event/services/event.service'
import { CupService } from '@/features/cup/services/cup.service'

const props = defineProps<{ cup?: Cup }>()

const emit = defineEmits(['cupSubmit'])

const { t } = useI18n()

const formData = ref<Cup | Omit<Cup, 'id'>>({
    name: '',
    type: { id: 'ADD' },
    year: new Date().getFullYear(),
    events: [],
})

onMounted(() => {
    if (props.cup) {
        formData.value = { ...props.cup }
    }
})

const eventQuery = useQuery({
    queryKey: ['events'],
    queryFn: () => eventService.getAll(t),
})

const cupTypesQuery = useQuery({
    queryKey: ['cup_types'],
    queryFn: () => CupService.getCupTypes(t),
})

function formSubmitHandler() {
    // console.log(formData.value)
    emit('cupSubmit', formData.value)
}
</script>

<template>
    <form @submit.prevent="formSubmitHandler">
        <div class="flex flex-col">
            <div class="flex flex-row">
                <label for="name" class="col-fixed w-32">{{ t('labels.name') }}</label>
                <div class="col">
                    <InputText id="name" v-model="formData.name" type="text" class="w-96" />
                </div>
            </div>
            <div class="flex flex-row">
                <label for="year" class="col-fixed w-32">{{ t('labels.year') }}</label>
                <div class="col">
                    <InputNumber
                        id="year"
                        v-model="formData.year"
                        class="w-96"
                        mode="decimal"
                        show_buttons="true"
                        :use-grouping="false"
                        :min="1970"
                        :max="9999"
                    />
                </div>
            </div>
            <div class="flex flex-row">
                <label for="type" class="col-fixed w-32">{{ t('labels.type') }}</label>
                <div class="col">
                    <span v-if="cupTypesQuery.status.value === 'pending'">{{
                        t('messages.loading')
                    }}</span>
                    <span v-else-if="cupTypesQuery.status.value === 'error'">
                        {{ t('messages.error', { message: cupTypesQuery.error.toLocaleString() }) }}
                    </span>
                    <Dropdown
                        v-else-if="cupTypesQuery.data && cupTypesQuery.data.value"
                        id="type"
                        v-model="formData.type"
                        :options="cupTypesQuery.data.value"
                        option-label="id"
                        data-key="id"
                        :placeholder="t('messages.select')"
                        class="w-full md:w-14rem"
                    />
                </div>
            </div>
            <div class="flex flex-row">
                <label for="cups" class="col-fixed w-32">{{ t('labels.event') }}</label>
                <div class="col">
                    <span v-if="eventQuery.status.value === 'pending'">{{
                        t('messages.loading')
                    }}</span>
                    <span v-else-if="eventQuery.status.value === 'error'">
                        {{ t('messages.error', { message: eventQuery.error.toLocaleString() }) }}
                    </span>
                    <div v-else-if="eventQuery.data && eventQuery.data.value" class="card">
                        <MultiSelect
                            id="events"
                            v-model="formData.events"
                            :options="eventQuery.data.value.content"
                            data-key="id"
                            filter
                            option-label="name"
                            option-value="id"
                            :placeholder="t('messages.select')"
                            class="w-full md:w-20rem"
                        />
                    </div>
                </div>
            </div>
        </div>
        <div class="mt-2">
            <slot />
        </div>
    </form>
</template>

<style scoped></style>
