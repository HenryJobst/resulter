import type { BffUserInfo } from '../model/bffUser'
import type { AuthStore } from '../store/auth.store'

/**
 * BFF Authentication Service
 * Handles authentication via Backend-for-Frontend pattern
 * - No tokens in browser
 * - Session-based authentication with HTTP-only cookies
 * - OAuth2 handled by backend
 */
class BffAuthService {
    private apiBaseUrl: string
    private backendUrl: string

    constructor() {
        // In development: use empty string for Vite proxy (localhost:5173 → proxied to localhost:8080)
        // In production: use VITE_API_ENDPOINT directly (resulter.olberlin.de → resulter-api.olberlin.de)
        this.apiBaseUrl = import.meta.env.PROD
            ? (import.meta.env.VITE_API_ENDPOINT || 'http://localhost:8080')
            : ''

        // OAuth2 redirects use direct backend URL (browser must be redirected to backend → Keycloak)
        this.backendUrl = import.meta.env.VITE_API_ENDPOINT || 'http://localhost:8080'
    }

    /**
     * Initialize authentication by checking current session
     * Calls /bff/user to retrieve user information
     * @returns Promise resolving to user info if authenticated, null otherwise
     */
    async initAuth(): Promise<BffUserInfo | null> {
        try {
            // Use proxy for API calls (apiBaseUrl is empty, so "/bff/user" goes to localhost:5173/bff/user → proxied to backend)
            const response = await fetch(`${this.apiBaseUrl}/bff/user`, {
                method: 'GET',
                credentials: 'include', // Important: Send cookies
                headers: {
                    Accept: 'application/json',
                },
            })

            if (response.ok) {
                return await response.json()
            }

            // Not authenticated (401) or other error
            return null
        }
        catch (error) {
            console.error('Failed to initialize auth:', error)
            return null
        }
    }

    /**
     * Initiate OAuth2 login flow
     * Redirects to backend which handles OAuth2 authorization code flow
     * @param redirectPath Path to redirect to after successful login
     * @param locale Current locale for localized login
     */
    login(redirectPath: string = '/', locale: string = 'de'): void {
        // Clear any logout timestamp (no longer needed with cookie-based auth request storage)
        sessionStorage.removeItem('bff_last_logout_time')

        // Store intended destination for post-login redirect
        sessionStorage.setItem('bff_post_login_redirect', redirectPath)
        sessionStorage.setItem('bff_post_login_locale', locale)

        // Redirect DIRECTLY to backend (not proxied) - browser needs to follow redirects to Keycloak
        window.location.href = `${this.backendUrl}/oauth2/authorization/keycloak`
    }

    /**
     * Logout current user
     * Invalidates session on backend and redirects to login
     * @param redirectPath Path to redirect to after logout
     */
    async logout(redirectPath: string = '/'): Promise<void> {
        try {
            // Clear post-login redirect to prevent confusion on next login
            sessionStorage.removeItem('bff_post_login_redirect')
            sessionStorage.removeItem('bff_post_login_locale')

            // Browser navigation via form submit keeps the redirect chain to Keycloak logout.
            // /bff/logout is exempt from CSRF on the backend (logout-CSRF risk is low;
            // the HttpOnly session cookie is the primary protection).
            const form = document.createElement('form')
            form.method = 'POST'
            form.action = `${this.apiBaseUrl}/bff/logout`
            form.style.display = 'none'

            document.body.appendChild(form)
            form.submit()
        }
        catch (error) {
            console.error('Logout failed:', error)
            // Even if logout fails, redirect to intended path
            window.location.href = redirectPath
        }
    }

    private getCookieValue(name: string): string | null {
        const value = `; ${document.cookie}`
        const parts = value.split(`; ${name}=`)
        if (parts.length === 2) {
            return parts.pop()?.split(';').shift() || null
        }
        return null
    }

    /**
     * Get CSRF token for state-changing operations
     * The token is automatically included in response cookies
     */
    async getCsrfToken(): Promise<void> {
        try {
            // Use proxy for API calls
            await fetch(`${this.apiBaseUrl}/bff/csrf`, {
                method: 'GET',
                credentials: 'include',
            })
        }
        catch (error) {
            console.error('Failed to get CSRF token:', error)
        }
    }

    /**
     * Initialize auth store with BFF user information
     * Called after successful authentication check
     * @param store Pinia auth store
     */
    async initStore(store: AuthStore): Promise<void> {
        const userInfo = await this.initAuth()

        if (userInfo) {
            store.setBffUser(userInfo)
        }
        else {
            store.clearUserData()
        }
    }

    /**
     * Handle post-login redirect
     * Retrieves stored redirect path and locale from sessionStorage
     * @returns Object with redirect path and locale
     */
    getPostLoginRedirect(): { path: string, locale: string } {
        const path = sessionStorage.getItem('bff_post_login_redirect') || '/'
        const locale = sessionStorage.getItem('bff_post_login_locale') || 'de'

        // Clear stored values
        sessionStorage.removeItem('bff_post_login_redirect')
        sessionStorage.removeItem('bff_post_login_locale')

        return { path, locale }
    }
}

// Export singleton instance
export const bffAuthService = new BffAuthService()
