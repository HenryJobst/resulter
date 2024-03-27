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
    person?: Person
    entityService: PersonService
    queryKey: string[]
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
</script>

<template>
    <div v-if="person" class="flex flex-col">
        <div class="flex flex-row">
            <label for="family_name" class="col-fixed w-40">{{ t('labels.family_name') }}</label>
            <div class="col">
                <InputText id="family_name" v-model="person.familyName" type="text" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="given_name" class="col-fixed w-40">{{ t('labels.given_name') }}</label>
            <div class="col">
                <InputText id="given_name" v-model="person.givenName" type="text" />
            </div>
        </div>
        <div class="flex flex-row">
            <label for="birthday" class="col-fixed w-32">{{ t('labels.birth_year') }}</label>
            <div class="col">
                <Calendar
                    id="birthday"
                    v-model="person.birthDate"
                    show-icon
                    icon-display="input"
                    date-format="y"
                    view="year"
                    :locale="locale"
                >
                    <template #inputicon="{ clickCallback }">
                        <i class="pi pi-calendar" @click="clickCallback" />
                    </template>
                </Calendar>
            </div>
        </div>
        <div class="flex flex-row">
            <label for="type" class="col-fixed w-32">{{ t('labels.gender') }}</label>
            <div class="col">
                <span v-if="genderQuery.status.value === 'pending'">{{ t('messages.loading') }}</span>
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
                />
            </div>
        </div>
    </div>
</template>

<style scoped></style>
