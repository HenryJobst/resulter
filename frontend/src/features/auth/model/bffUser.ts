/**
 * BFF User Information Response
 * Matches backend BffUserInfoDto structure
 */
export interface BffUserInfo {
    username: string
    email: string
    name: string
    roles: string[]
    groups: string[]
    permissions: UserPermissions
}

/**
 * User Permissions derived from roles
 * Matches backend UserPermissionsDto structure
 */
export interface UserPermissions {
    canManageEvents: boolean
    canUploadResults: boolean
    canManageCups: boolean
    canViewReports: boolean
    canManageUsers: boolean
    canAccessAdmin: boolean
}

/**
 * User model for Pinia store
 */
export interface User {
    username?: string
    email?: string
    name?: string
    roles?: string[]
    groups?: string[]
    permissions?: UserPermissions
}
