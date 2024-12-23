<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import { computed } from 'vue'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import { PersonService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'

const props = defineProps<{
    person: Person
    entityService: PersonService
    queryKey: string[]
    visible?: boolean
    changeable?: boolean
}>()

const emit = defineEmits(['update:modelValue'])

const { t, locale } = useI18n()

const person = computed({
    get: () => props.person,
    set: value => emit('update:modelValue', value),
})

const genderQuery = useQuery({
    queryKey: ['gender'],
    queryFn: () => PersonService.getGender(t),
})

const localizedGenderOptions = computed(() => {
    if (genderQuery.data.value) {
        return genderQuery.data.value.map(option => ({
            ...option,
            label: t(`gender.${option.id.toLocaleUpperCase()}`),
        }))
    }
    return []
})

const birthDate = computed({
    get: () => {
        const bDate = person.value.birthDate
        return bDate ? new Date(bDate) : null
    },
    set: value => (value ? person.value.birthDate = value! : null),
})
</script>

<template>
    <div v-if="person && (props.visible ?? true)" class="flex flex-col">
        <div class="flex flex-row">
            <label for="family_name" class="col-fixed w-40">{{ t('labels.family_name') }}</label>
            <div class="col">
                <InputText id="family_name" v-model="person.familyName" type="text" :disabled="!props.changeable ?? false" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="given_name" class="col-fixed w-40">{{ t('labels.given_name') }}</label>
            <div class="col">
                <InputText id="given_name" v-model="person.givenName" type="text" :disabled="!props.changeable ?? false" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="birthday" class="col-fixed w-32">{{ t('labels.birth_year') }}</label>
            <div class="col">
                <Calendar
                    id="birthday"
                    v-model="birthDate"
                    show-icon
                    icon-display="input"
                    date-format="y"
                    view="year"
                    :locale="locale"
                    :disabled="!props.changeable ?? false"
                >
                    <template #inputicon="{ clickCallback }">
                        <i class="pi pi-calendar ml-2 mt-2" rounded @click="clickCallback" />
                    </template>
                </Calendar>
            </div>
        </div>
        <div class="flex flex-row">
            <label for="type" class="col-fixed w-32">{{ t('labels.gender') }}</label>
            <div class="col">
                <span v-if="genderQuery.status.value === 'pending'">{{
                    t('messages.loading')
                }}</span>
                <span v-else-if="genderQuery.status.value === 'error'">
                    {{ t('messages.error', { message: genderQuery.error.toLocaleString() }) }}
                </span>
                <Dropdown
                    v-else-if="genderQuery.data"
                    id="gender"
                    v-model="person.gender"
                    :options="localizedGenderOptions"
                    option-label="label"
                    data-key="id"
                    :placeholder="t('messages.select')"
                    class="w-full md:w-14rem"
                    :disabled="!props.changeable ?? false"
                />
            </div>
        </div>
    </div>
</template>

<style scoped></style>
