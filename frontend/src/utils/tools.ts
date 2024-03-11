function getValueByPath(obj: any, path: string): string {
  return path.split('.').reduce((acc, part) => acc && acc[part], obj)
}

export function truncateString(obj: any, path: string, maxLength: number = 50) {
  const str = getValueByPath(obj, path)
  if (str && str.length > maxLength) {
    return str.substring(0, maxLength) + '...'
  }
  return str
}
