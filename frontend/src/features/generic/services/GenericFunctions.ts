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

type DateInput = string | Date
type FormatOptions = 'date' | 'datetime' | 'year' | 'time'

const formatOptionsMap: Record<FormatOptions, Intl.DateTimeFormatOptions> = {
    date: dateOptions,
    datetime: dateTimeOptions,
    year: yearOptions,
    time: timeOptions,
}

/**
 * Formatiert ein Datum oder eine Uhrzeit basierend auf dem angegebenen Typ
 * @param input - Das zu formatierende Datum (String oder Date-Objekt)
 * @param locale - Die zu verwendende Locale
 * @param type - Der gewünschte Formatierungstyp
 * @returns Formatierter String
 */
function formatDateTime(input: DateInput, locale: Intl.LocalesArgument, type: FormatOptions): string {
    if (!input)
        return ''

    const date = typeof input === 'string' ? new Date(input) : input
    if (Number.isNaN(date.getTime())) {
        console.error(`Fehler beim Formatieren des Datums: ${new Error('Ungültiges Datum')}`)
        return ''
    }

    const options = formatOptionsMap[type]
    return type === 'time'
        ? date.toLocaleTimeString(locale, options)
        : date.toLocaleDateString(locale, options)
}

export function formatDate(date: DateInput, locale: Intl.LocalesArgument): string {
    return formatDateTime(date, locale, 'date')
}

export function formatDateAndTime(date: DateInput, locale: Intl.LocalesArgument): string {
    return formatDateTime(date, locale, 'datetime')
}

export function formatYear(date: DateInput, locale: Intl.LocalesArgument): string {
    return formatDateTime(date, locale, 'year')
}

export function formatTime(time: DateInput, locale: Intl.LocalesArgument): string {
    return formatDateTime(time, locale, 'time')
}
