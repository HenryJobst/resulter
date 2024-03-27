export function getValueByPath(obj: any, path: string): any {
    return path.split('.').reduce((acc, part) => acc && acc[part], obj)
}

export function truncateString(obj: any, path: string, maxLength: number = 1000): string {
    const str = getValueByPath(obj, path)
    if (str && typeof str === 'string' && str.length > maxLength)
        return `${str.substring(0, maxLength)}...`

    return str
}
