import type { RouteLocationRaw, Router } from 'vue-router'
import messages from '@intlify/unplugin-vue-i18n/messages'
import { flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { render } from 'vitest-browser-vue'
// @ts-expect-error - JSON import without explicit type
import de from '@/locales/de.json'
import { setupI18n } from '../../src/i18n'
import { renderApp } from '../../src/main'
import { setupRouter } from '../../src/router'

interface CreateTestAppOptions {
    initialRoute?: string
    locale?: string
}

interface TestApp {
    router: Router
    container: Element
    navigateTo: (to: RouteLocationRaw) => Promise<void>
    cleanup: () => void
}

/**
 * Creates a test application instance with full routing, i18n, and state management.
 * Useful for integration tests that need the complete application context.
 *
 * @param options - Configuration options for the test app
 * @param options.initialRoute - Initial route to navigate to (default: '/')
 * @param options.locale - Locale to use (default: 'de')
 * @returns Promise resolving to TestApp instance with router, container, navigation, and cleanup
 *
 * @example
 * ```ts
 * const app = await createTestApp({ initialRoute: '/events' })
 * await app.navigateTo('/cups')
 * expect(app.container.textContent).toContain('Cups')
 * app.cleanup()
 * ```
 */
// noinspection JSUnusedGlobalSymbols
export async function createTestApp(options: CreateTestAppOptions = {}): Promise<TestApp> {
    const { initialRoute = '/', locale = 'de' } = options

    // Setup i18n with proper locale
    const i18n = setupI18n({
        legacy: false,
        locale,
        fallbackLocale: 'en',
        messages,
    })

    // Set locale message explicitly for German
    i18n.global.setLocaleMessage('de', de)
    i18n.global.locale = locale

    // Setup state management
    const pinia = createPinia()

    // Setup routing
    const router = setupRouter(i18n)

    // Add initial route if it's not the default
    if (initialRoute !== '/') {
        router.addRoute({ path: initialRoute, redirect: '/' })
    }

    // Render the application
    const app = renderApp()

    // Mount with all plugins
    const screen = render(app, { global: { plugins: [router, pinia, i18n] } })

    // Wait for router to be ready
    await router.isReady()

    // Navigate to initial route if specified
    if (initialRoute !== '/') {
        await router.push(initialRoute)
    }

    // Flush all pending promises
    await flushPromises()

    /**
     * Navigate to a new route
     * @param to - Route location to navigate to
     */
    async function navigateTo(to: RouteLocationRaw) {
        await router.push(to)
        await flushPromises()
    }

    /**
     * Cleanup and unmount the test app
     */
    function cleanup() {
        screen.unmount()
    }

    return {
        router,
        container: screen.container,
        navigateTo,
        cleanup,
    }
}
