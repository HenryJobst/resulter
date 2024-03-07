//  "fileSizeTypes": ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
export const fileSizeTypes = (t: (key: string) => string) => [
  t('labels.bytes'),
  t('labels.kilobytes'),
  t('labels.megabytes'),
  t('labels.gigabytes')
]
