import { computed } from 'vue'

const dateOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
}

const dateTimeOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
}

const yearOptions: Intl.DateTimeFormatOptions = {
    year: '2-digit',
    month: undefined,
    day: undefined,
}

const timeOptions: Intl.DateTimeFormatOptions = {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
}

const formatDateFunction = computed(() => {
    return (date: string | Date, locale: Intl.LocalesArgument) => {
        if (!date)
            return ''
        if (typeof date === 'string')
            return new Date(date).toLocaleDateString(locale, dateOptions)

        return date.toLocaleDateString(locale, dateOptions)
    }
})

const formatDateTimeFunction = computed(() => {
    return (date: string | Date, locale: Intl.LocalesArgument) => {
        if (!date)
            return ''
        if (typeof date === 'string')
            return new Date(date).toLocaleDateString(locale, dateTimeOptions)

        return date.toLocaleDateString(locale, dateTimeOptions)
    }
})

const formatYearFunction = computed(() => {
    return (date: string | Date, locale: Intl.LocalesArgument) => {
        if (!date)
            return ''
        if (typeof date === 'string')
            return new Date(date).toLocaleDateString(locale, yearOptions)

        return date.toLocaleDateString(locale, yearOptions)
    }
})

const formatTimeFunction = computed(() => {
    return (time: string | Date, locale: Intl.LocalesArgument) => {
        if (!time)
            return ''
        if (typeof time === 'string')
            return new Date(time).toLocaleTimeString(locale, timeOptions)

        return time.toLocaleTimeString(locale, timeOptions)
    }
})

export function formatDate(date: string, locale: Intl.LocalesArgument) {
    return formatDateFunction.value(date, locale)
}

export function formatDateTime(date: string, locale: Intl.LocalesArgument) {
    return formatDateTimeFunction.value(date, locale)
}

export function formatYear(date: string, locale: Intl.LocalesArgument) {
    return formatYearFunction.value(date, locale)
}

export function formatTime(time: string, locale: Intl.LocalesArgument) {
    return formatTimeFunction.value(time, locale)
}
