// tests/unit.setup.ts
import { config } from '@vue/test-utils'
import { vi } from 'vitest'

// Mock localStorage to fix Vue devtools issue
const localStorageMock = (() => {
    let store: Record<string, string> = {}
    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => {
            store[key] = value.toString()
        },
        removeItem: (key: string) => {
            delete store[key]
        },
        clear: () => {
            store = {}
        },
    }
})()

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
})

// Mock PrimeVue useToast globally for all tests
vi.mock('primevue/usetoast', () => ({
    useToast: vi.fn(() => ({
        add: vi.fn(),
        remove: vi.fn(),
        removeGroup: vi.fn(),
        removeAllGroups: vi.fn(),
    })),
}))

// Mock Keycloak to suppress initialization warnings in tests
vi.mock('@/features/keycloak/services/keycloak', () => ({
    default: {
        callInit: vi.fn(),
        callInitStore: vi.fn(),
        callLogin: vi.fn(),
        callLogout: vi.fn(),
        callTokenRefresh: vi.fn(),
    },
}))

// Global test-utils config
config.global.stubs = {
    teleport: true,
    transition: false,
}

config.global.mocks = {
    $t: (tKey: string) => tKey,
}
