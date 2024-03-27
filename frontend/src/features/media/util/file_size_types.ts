//  "fileSizeTypes": ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
export function fileSizeTypes(t: (key: string) => string) {
    return [
        t('labels.bytes'),
        t('labels.kilobytes'),
        t('labels.megabytes'),
        t('labels.gigabytes'),
    ]
}
