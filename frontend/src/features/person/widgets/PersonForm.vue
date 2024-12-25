<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import { computed } from 'vue'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import { PersonService } from '@/features/person/services/person.service'
import type { Person } from '@/features/person/model/person'

interface Props {
    person: Person
    entityService: PersonService
    queryKey: string[]
    visible?: boolean
    changeable?: boolean
}
const props = withDefaults(defineProps<Props>(), {
    visible: true,
    changeable: true,
})

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
    set: (value) => {
        if (value) {
            const newDate = new Date(value.getTime())
            const timezoneOffset = newDate.getTimezoneOffset()
            const hours = newDate.getHours()
            newDate.setHours(hours - (timezoneOffset / 60))
            person.value.birthDate = newDate.toISOString()
        }
        else {
            person.value.birthDate = null
        }
    },
})
</script>

<template>
    <div v-if="person && props.visible" class="flex flex-col">
        <div class="flex flex-row">
            <label for="family_name" class="col-fixed w-40">{{ t('labels.family_name') }}</label>
            <div class="col">
                <InputText
                    id="family_name"
                    v-model="person.familyName"
                    type="text"
                    :disabled="!props.changeable"
                />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="given_name" class="col-fixed w-40">{{ t('labels.given_name') }}</label>
            <div class="col">
                <InputText
                    id="given_name"
                    v-model="person.givenName"
                    type="text"
                    :disabled="!props.changeable"
                />
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
                    :year-navigator="true"
                    :locale="locale"
                    :disabled="!props.changeable"
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
                    :disabled="!props.changeable"
                />
            </div>
        </div>
    </div>
</template>

<style scoped></style>
