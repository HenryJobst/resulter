import type { Page } from '@playwright/test'
import type { Organisation } from '@/features/organisation/model/organisation'
import process from 'node:process'

export interface FactoryConfig {
    page: Page
    dbIdentifier?: string
}

// Default test organisation (sensible defaults following factory pattern)
// Reference: https://alexop.dev/posts/vue3_testing_pyramid_vitest_browser_mode
const defaultOrganisation = {
    name: 'Test Organisation',
    shortName: 'TEST',
    type: { id: 'Club' },
    country: null,
    childOrganisations: [],
}

/**
 * Factory function to create a test organisation via API
 * Follows factory pattern: holds defaults and allows overrides via spread operator
 *
 * @param overrides - Partial organisation to override defaults
 * @param config - Factory configuration (page for auth, optional dbIdentifier)
 * @returns Created organisation with ID
 */
export async function createOrganisation(
    overrides: Partial<Omit<Organisation, 'id'>> = {},
    config: FactoryConfig,
): Promise<Organisation> {
    const backendProtocol = process.env.BACKEND_PROTOCOL || 'http'
    const backendPort = process.env.BACKEND_PORT || '8080'
    const baseUrl = `${backendProtocol}://localhost:${backendPort}`

    // Merge defaults with overrides (factory pattern - "Standard Pizza" with custom toppings)
    const organisationData = {
        ...defaultOrganisation,
        ...overrides,
    }

    // Set database cookie if provided (for isolated database routing)
    if (config.dbIdentifier) {
        await config.page.context().addCookies([
            {
                name: 'X-DB-Identifier',
                value: config.dbIdentifier,
                domain: 'localhost',
                path: '/',
                httpOnly: false,
                secure: false,
                sameSite: 'Lax',
            },
        ])
    }

    // Create organisation via POST /organisation (uses authenticated session from auth-setup.ts)
    const response = await config.page.request.post(`${baseUrl}/organisation`, {
        data: organisationData,
    })

    if (!response.ok()) {
        const errorText = await response.text()
        throw new Error(
            `Failed to create organisation: ${response.status()} ${response.statusText()}\n${errorText}`,
        )
    }

    const responseData = await response.json()

    // Backend returns ApiResponse<OrganisationDto> wrapper
    const organisation: Organisation = responseData.data || responseData

    console.log(`[Factory] Created organisation: ${organisation.name} (ID: ${organisation.id})`)

    return organisation
}

/**
 * Seed minimal test data for E2E tests in isolated databases
 * Creates organisations that tests can use for dropdowns
 *
 * Factory pattern benefit: Single source of truth for test data
 * If schema changes, update factory defaults in one place
 *
 * @param page - Playwright page (for authenticated API calls)
 * @param dbIdentifier - Database identifier for isolated database
 * @returns Object containing arrays of created test data
 */
export async function seedTestData(
    page: Page,
    dbIdentifier: string,
): Promise<{
    organisations: Organisation[]
}> {
    console.log(`[Seed Data] Creating test data for database ${dbIdentifier}`)

    // Create test organisations with different types
    // Each test can select from these pre-created organisations
    const organisations = await Promise.all([
        createOrganisation(
            { name: 'Test Club Alpha', shortName: 'TCA', type: { id: 'Club' } },
            { page, dbIdentifier },
        ),
        createOrganisation(
            { name: 'Test Club Beta', shortName: 'TCB', type: { id: 'Club' } },
            { page, dbIdentifier },
        ),
        createOrganisation(
            {
                name: 'National Test Federation',
                shortName: 'NTF',
                type: { id: 'NationalFederation' },
            },
            { page, dbIdentifier },
        ),
    ])

    console.log(`[Seed Data] ✓ Created ${organisations.length} organisations`)

    return { organisations }
}
