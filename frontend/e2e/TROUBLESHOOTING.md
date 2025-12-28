# E2E Tests Troubleshooting Guide

Detailed solutions for common E2E test problems with database isolation.

## 🔍 Quick Diagnostics

```bash
# 1. Check if servers are running
curl http://localhost:5173           # Frontend
curl http://localhost:8080/actuator/health  # Backend

# 2. Check Keycloak OAuth2 endpoint
curl -L http://localhost:8080/oauth2/authorization/keycloak

# 3. Test auth setup with debugging
pnpm playwright test auth-setup.ts --headed

# 4. See complete logs
pnpm playwright test auth-setup.ts --debug

# 5. Check backend profile
curl http://localhost:8080/actuator/env | grep -i activeProfiles
# Should show: "e2e-frontend-tests"
```

## 📋 Common Issues Index

### Authentication Issues
- ["Timeout 10000ms exceeded" during auth setup](#-timeout-10000ms-exceeded-during-auth-setup)
- [Wrong credentials / Invalid username or password](#-wrong-credentials--authentication-fails)
- [Session expired during tests](#-session-expired-during-tests)

### Database Isolation Issues
- [Database creation fails](#-database-creation-fails)
- [Wrong database used (data from other tests visible)](#-wrong-database-used)
- [Database not cleaned up (memory issues)](#-database-not-cleaned-up)
- [Liquibase migration fails](#-liquibase-migration-fails)

### Server & Configuration Issues
- [Backend/Frontend not reachable](#-backendfrontend-not-reachable)
- [e2e/.env.local not found](#-e2eenvlocal-not-found)
- [API token issues (403/401)](#-api-token-issues)

### Test Execution Issues
- [Tests run but fail](#-tests-run-but-fail)
- [Tests work locally but fail in CI](#-tests-work-locally-but-fail-in-ci)
- [Performance issues / Tests slow](#-performance-issues--tests-slow)
- [Element not found errors](#-element-not-found-errors)

---

## 🔒 Authentication Issues

### ❌ "Timeout 10000ms exceeded" during auth setup

**Error:**
```
TimeoutError: page.waitForURL: Timeout 10000ms exceeded.
waiting for navigation to "**/auth/**" until "load"
```

**Possible Causes & Solutions:**

#### 1. Keycloak is not reachable

**Check:**
```bash
# Manually test OAuth2 flow
curl -L http://localhost:8080/oauth2/authorization/keycloak
```

**Expected Response:** Redirect to Keycloak login page

**If not:** Backend not running or OAuth2 not configured

**Solution:**
```bash
# Check backend logs
cd ../backend
./mvnw spring-boot:run

# Look for errors in logs:
# - "OAuth2ClientRegistrationRepository"
# - "Keycloak"
# - "redirect-uri"
```

#### 2. Keycloak URL doesn't match

**The script automatically tries multiple URL patterns:**
- `**/realms/**/protocol/openid-connect/auth**`
- `**/auth/realms/**/protocol/openid-connect/auth**`
- `**/openid-connect/auth**`

**If all fail:** Check logs to see actual URL used:

```bash
pnpm playwright test auth-setup.ts --headed
# See "Current URL after OAuth2 redirect: ..."
```

**Solution:** Adjust URL pattern in `auth-setup.ts`

#### 3. Wrong backend profile

**Check in `e2e/.env.local`:**
```env
BACKEND_PROFILES=dev  # NOT "e2e-frontend-tests" for local auth tests!
```

**Why?** `e2e-frontend-tests` starts isolated containers, not local Keycloak

**Solution:**
```bash
# Adjust e2e/.env.local
BACKEND_PROFILES=dev

# Restart backend
cd ../backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

#### 4. Session already exists

**The script skips auth if session is <10 minutes old**

**Solution:**
```bash
# Delete session and re-authenticate
rm e2e/.auth/storageState.json
pnpm playwright test auth-setup.ts
```

---

### ❌ Wrong credentials / Authentication fails

**Error:**
```
Error: locator.fill: Target closed
```
or Keycloak shows "Invalid username or password"

**Solution:**

**1. Check credentials in Keycloak:**

```bash
# Open Keycloak Admin Console
open https://keycloak.jobst24.de/admin

# Navigate to:
# Realm: resulter
# Users → Search for your USERNAME
# Verify:
# - User exists
# - User is "Enabled"
# - Email is verified (if required)
```

**2. Check credentials in .env.local:**

```bash
# Show current credentials (WARNING: password visible!)
cat e2e/.env.local | grep USERNAME
cat e2e/.env.local | grep PASSWORD

# Test login manually in browser:
# 1. Open http://localhost:8080/oauth2/authorization/keycloak
# 2. Enter USERNAME and PASSWORD
# 3. Should redirect to http://localhost:5173
```

**3. Check user roles:**

Some tests require ADMIN role:

```bash
# In Keycloak Admin Console:
# Realm: resulter
# Users → Your User → Role Mappings
# Add: "ADMIN" or "admin"
```

---

### ❌ Session expired during tests

**Error:** 401 Unauthorized or redirect to login during test

**Cause:** Session cookie expired (default: 30 minutes)

**Solutions:**

**1. Increase session timeout (Backend):**

```yaml
# application.yml
server:
  servlet:
    session:
      timeout: 60m  # Increase from 30m to 60m
```

**2. Re-authenticate before each test run:**

```bash
# Delete old session
rm e2e/.auth/storageState.json

# Run tests (auth-setup runs automatically)
pnpm test:e2e
```

**3. Write faster tests:**
- Reduce `page.waitForTimeout()`
- Use `expect().toBeVisible()` instead of fixed waits

---

## 💾 Database Isolation Issues

### ❌ Database creation fails

**Error:**
```
Failed to create test database: Connection refused
```
or
```
Failed to create test database: 500 Internal Server Error
```

**Causes & Solutions:**

#### 1. Backend not running

**Check:**
```bash
curl http://localhost:8080/actuator/health
```

**Solution:**
```bash
cd backend
./start-e2e-frontend-tests.sh
```

#### 2. Wrong backend profile

**Check active profile:**
```bash
curl http://localhost:8080/actuator/env | grep -i activeProfiles
# Should show: "e2e-frontend-tests"
```

**Solution:**
```bash
# Restart with correct profile
cd backend
export SPRING_PROFILES_ACTIVE=e2e-frontend-tests
./mvnw spring-boot:run
```

#### 3. Docker not running

**Error:** `Could not start PostgreSQL container`

**Check:**
```bash
docker ps
# Should show running containers
```

**Solution:**
```bash
# Start Docker Desktop
open -a Docker

# Wait for Docker to start
docker ps
```

#### 4. Missing API token

**Error:** `403 Forbidden` or `401 Unauthorized`

**Check:**
```bash
# .env file (project root)
grep CREATEDATABASE_API_TOKEN .env

# e2e/.env.local
grep CREATEDATABASE_API_TOKEN e2e/.env.local
```

**Solution:**
```bash
# Generate token
openssl rand -base64 32

# Add to .env and e2e/.env.local
CREATEDATABASE_API_TOKEN=<generated-token>

# Restart backend
cd backend
./start-e2e-frontend-tests.sh
```

---

### ❌ Wrong database used

**Error:** Test sees data from other tests or default database

**Cause:** Cookie not set or set incorrectly

**Solution:**

**1. Verify cookie is set BEFORE navigation:**

```typescript
// ✅ CORRECT: Set cookie first
await page.context().addCookies([{
    name: 'X-DB-Identifier',
    value: dbId,
    domain: 'localhost', // Must be 'localhost', not '127.0.0.1'
    path: '/',
    httpOnly: false,
    secure: false,
    sameSite: 'Lax',
}])

await page.goto('/event') // Cookie sent automatically

// ❌ WRONG: Navigate first
await page.goto('/event') // Uses default database!
await page.context().addCookies([...]) // Too late
```

**2. Add debug logging:**

```typescript
test('debug cookie', async ({ page }) => {
    const dbId = await createTestDatabase()

    await page.context().addCookies([{
        name: 'X-DB-Identifier',
        value: dbId,
        domain: 'localhost',
        path: '/',
        httpOnly: false,
        secure: false,
        sameSite: 'Lax',
    }])

    // Verify cookie was set
    const cookies = await page.context().cookies()
    console.log('Cookies:', cookies.filter(c => c.name === 'X-DB-Identifier'))

    await page.goto('/event')
})
```

**3. Check backend logs:**

```bash
# Backend logs should show:
# DataSourceInterceptor - X-DB-Identifier header: <uuid>
# DataSourceInterceptor - Routing to database: <uuid>
# DataSourceManager - DataSource found and set for: <uuid>

# If not, cookie is not being sent or read correctly
```

---

### ❌ Database not cleaned up

**Error:** Too many databases, memory issues, performance degradation

**Cause:** Cleanup scheduler not running or databases still active

**Solutions:**

**1. Check cleanup scheduler is running:**

```bash
# Backend logs should show every 10 seconds:
# DataSourceCleanupScheduler - Checking for inactive databases...
# DataSourceCleanupScheduler - Cleaning up inactive database: <uuid>
# DataSourceManager - Database dropped: test-db-<uuid>
```

**2. Verify profile is active:**

```bash
curl http://localhost:8080/actuator/env | grep "e2e-frontend-tests"
# Should return: "e2e-frontend-tests"
```

**3. Check scheduler is enabled:**

```bash
curl http://localhost:8080/actuator/scheduledtasks
# Should show: DataSourceCleanupScheduler.cleanupInactiveDatabases
```

**4. Wait for automatic cleanup:**

Cleanup happens automatically:
- Last database access recorded
- Wait 30 seconds
- Scheduler runs every 10 seconds
- Detects inactivity >30 seconds
- Closes connections and drops database

**5. Manual cleanup (if needed):**

```bash
# Restart backend to clean all test databases
cd backend
./start-e2e-frontend-tests.sh
```

---

### ❌ Liquibase migration fails

**Error:**
```
Failed to run Liquibase migrations
```
or
```
Database migration failed for test-db-<uuid>
```

**Causes & Solutions:**

**1. Check Liquibase changelog:**

```bash
# View master changelog
cat backend/src/main/resources/db/changelog/db.changelog-master.yaml

# Test migrations on default database
cd backend
./mvnw liquibase:update -Dspring.profiles.active=e2e-frontend-tests
```

**2. Check for migration conflicts:**

```bash
cd backend
./mvnw liquibase:status -Dspring.profiles.active=e2e-frontend-tests
```

**3. Clear Liquibase locks:**

```sql
-- If migrations are stuck with locked status
-- Connect to database and run:
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE;
```

**4. Check migration logs:**

```bash
# Backend logs should show:
# DataSourceManager - Running Liquibase migrations for: test-db-<uuid>
# Liquibase - Successfully acquired change log lock
# Liquibase - Reading from PUBLIC.DATABASECHANGELOG
# Liquibase - Successfully released change log lock
```

---

### ❌ API token issues

**Error:**
```
403 Forbidden
```
or
```
401 Unauthorized
```
when calling `/createDatabase`

**Causes & Solutions:**

**1. Token not configured:**

**Check:**
```bash
# Backend .env
grep CREATEDATABASE_API_TOKEN .env

# Frontend e2e/.env.local
grep CREATEDATABASE_API_TOKEN e2e/.env.local
```

**Solution:**
```bash
# Generate new token
openssl rand -base64 32

# Set in .env (project root)
CREATEDATABASE_API_TOKEN=<generated-token>

# Set in e2e/.env.local (same token)
CREATEDATABASE_API_TOKEN=<generated-token>

# Restart backend
cd backend
./start-e2e-frontend-tests.sh
```

**2. Token mismatch:**

**Verify tokens match:**
```bash
# Backend token
grep CREATEDATABASE_API_TOKEN .env

# Frontend token
grep CREATEDATABASE_API_TOKEN frontend/e2e/.env.local

# They must be identical!
```

**3. Test token manually:**

```bash
# Get token from .env
TOKEN=$(grep CREATEDATABASE_API_TOKEN .env | cut -d '=' -f2)

# Test endpoint
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/createDatabase

# Should return:
# {"success":true,"data":{"identifier":"<uuid>"}}
```

---

## 🖥️ Server & Configuration Issues

### ❌ Backend/Frontend not reachable

**Error:**
```
✅ Frontend server is running on http://localhost:5173
❌ Backend server is not running on http://localhost:8080
```

**Solutions:**

#### Backend not running

```bash
# Check if port is occupied
lsof -i :8080

# Start backend
cd backend
./start-e2e-frontend-tests.sh

# Wait for:
# "Started ResulterApplication in X.XXX seconds"
```

#### Frontend not running

```bash
# Check if port is occupied
lsof -i :5173

# Start frontend
pnpm dev

# Wait for:
# "VITE vX.X.X ready in XXms"
```

#### Port already occupied

```bash
# Find process using port
lsof -i :5173
lsof -i :8080

# Kill process
kill -9 <PID>

# Restart servers
pnpm dev  # Frontend
cd backend && ./start-e2e-frontend-tests.sh  # Backend
```

---

### ❌ e2e/.env.local not found

**Error:**
```
❌ e2e/.env.local not found
```

**Solution:**

Create `e2e/.env.local`:

```bash
cat > e2e/.env.local << 'EOF'
# Backend Configuration
HOSTNAME=localhost
BACKEND_PROTOCOL=http
BACKEND_PORT=8080
BACKEND_PROFILES=e2e-frontend-tests

# Frontend Configuration
FRONTEND_PROTOCOL=http
PORT=5173
VITE_MODE=development

# Test Credentials
USERNAME=your-username
PASSWORD=your-password

# API Tokens
CREATEDATABASE_API_TOKEN=your-token-here
EOF
```

**Important:**
- `USERNAME` and `PASSWORD` must exist in Keycloak
- Realm: `resulter`
- Client: `resulter-backend` (for BFF)
- Token must match backend `.env` file

---

## 🐛 Test Execution Issues

### ❌ Element not found errors

**Error:**
```
Error: locator.fill: Target page, context or browser has been closed
```
or
```
Error: locator.click: Element not found
```

**Solutions:**

**1. Check selectors:**

```bash
# Run test in headed mode
pnpm playwright test event-form.spec.ts --headed

# Use Playwright Inspector
pnpm playwright test event-form.spec.ts --debug

# In Inspector: Hover over elements to see selectors
```

**2. Wait for elements:**

```typescript
// ❌ BAD: Element might not be ready
await page.getByLabel('Name').fill('Test')

// ✅ GOOD: Wait for element
await page.getByLabel('Name').waitFor()
await page.getByLabel('Name').fill('Test')

// ✅ BETTER: Use expect
await expect(page.getByLabel('Name')).toBeVisible()
await page.getByLabel('Name').fill('Test')
```

**3. Check element timing:**

```typescript
// Add debug logging
page.on('load', () => console.log('Page loaded'))
page.on('domcontentloaded', () => console.log('DOM ready'))

await page.goto('/event')
console.log('Navigation complete')

await page.screenshot({ path: 'debug.png' })
```

---

### ❌ Tests run but fail

**Error:** Tests start but functional assertions fail

**Debug Strategies:**

#### 1. Headed Mode

```bash
# See what happens in browser
pnpm playwright test event-form.spec.ts --headed

# Or specific test
pnpm playwright test event-form.spec.ts --headed -g "should create event"
```

#### 2. Debug Mode (Step-through)

```bash
# Playwright Inspector opens
pnpm playwright test event-form.spec.ts --debug

# Or specific test
pnpm playwright test event-form.spec.ts --debug -g "should create event"

# In Inspector:
# - Step through: F10
# - Resume: F8
# - Screenshot: Camera Icon
```

#### 3. Screenshots & Videos

**In playwright.config.ts:**
```typescript
use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
}
```

**Then:**
```bash
pnpm test:e2e

# Error screenshots in:
ls test-results/*/screenshots/

# Error videos in:
ls test-results/*/videos/
```

#### 4. Network Logs

**In test:**
```typescript
page.on('request', request =>
    console.log('>>', request.method(), request.url())
)
page.on('response', response =>
    console.log('<<', response.status(), response.url())
)
```

#### 5. Console Logs

**In test:**
```typescript
page.on('console', msg => console.log('PAGE LOG:', msg.text()))
```

---

### ❌ Tests work locally but fail in CI

**Error:** Tests pass on local machine ✅, fail in GitHub Actions ❌

**Solutions:**

#### 1. Environment Variables

**Check `.github/workflows/*.yml`:**
```yaml
env:
  USERNAME: ${{ secrets.TEST_USERNAME }}
  PASSWORD: ${{ secrets.TEST_PASSWORD }}
  CREATEDATABASE_API_TOKEN: ${{ secrets.CREATEDATABASE_API_TOKEN }}
```

**Ensure:**
- Secrets are defined in GitHub Repository Settings
- Names match exactly (case-sensitive!)
- All required variables are set

#### 2. Server Startup in CI

**playwright.config.ts has `webServer` for CI:**
```typescript
webServer: process.env.CI ? [...] : undefined
```

**Should work, but:**
- Increase timeout if server starts slowly
- Check CI logs for server startup errors
- Verify Docker is available in CI

#### 3. Headless vs Headed

CI always runs headless. Test locally also headless:

```bash
pnpm test:e2e  # Headless like in CI
```

#### 4. Timing Issues

CI may be slower than local machine:

```typescript
// Increase timeouts for CI
test.setTimeout(process.env.CI ? 60000 : 30000)
```

---

### ❌ Performance issues / Tests slow

**Error:** Tests take >10 minutes, database creation slow

**Causes & Solutions:**

#### 1. Optimize Docker

```bash
# Increase Docker resources
# Docker Desktop → Settings → Resources
# - CPUs: 4+
# - Memory: 8GB+
# - Swap: 2GB+
```

#### 2. Use Suite-Level Sharing

```typescript
// ❌ BAD: 7 database creations (35 seconds overhead)
test.beforeEach(async ({ page }) => {
    const dbId = await createTestDatabase()
})

// ✅ GOOD: 1 database creation (5 seconds overhead)
test.beforeAll(async ({ browser }) => {
    sharedDbId = await createTestDatabase()
})
```

#### 3. Reduce Migration Complexity

```xml
<!-- Skip heavy seed data in test databases -->
<changeSet id="seed-data" context="!test">
    <sqlFile path="seed-data.sql"/>
</changeSet>
```

#### 4. Enable Parallel Execution

```typescript
// playwright.config.ts
export default defineConfig({
    workers: 4, // Safe with database isolation
    fullyParallel: true,
})
```

---

## 🛠️ Advanced Debugging Tools

### Playwright Trace Viewer

**After test failure:**
```bash
# Open HTML report
pnpm playwright show-report

# Click on failed test
# Trace is shown automatically

# Or manually:
pnpm playwright show-trace test-results/*/trace.zip
```

**In Trace Viewer:**
- See every action step
- See screenshots for each step
- See network requests
- See console logs
- See source code

### Browser Developer Tools

**In headed mode:**
1. Start test with `--headed`
2. Right-click → Inspect
3. Developer Tools open
4. Use like normal web development

### Pause in Test

**In test:**
```typescript
await page.pause()  // Playwright Inspector opens
```

**Then:**
- Inspect page
- Execute commands manually
- Resume when ready

---

## 📞 Getting Help

If nothing works:

**1. Collect Logs:**
```bash
# Backend logs
cd backend && ./mvnw spring-boot:run > backend.log 2>&1

# Frontend logs
pnpm dev > frontend.log 2>&1

# E2E logs
pnpm test:e2e > e2e.log 2>&1
```

**2. Create Report:**
```bash
pnpm playwright show-report
# Report opens automatically
```

**3. Document Context:**
- Which test fails?
- Error message
- Screenshots/Videos
- Logs
- Environment (OS, Node version, Docker version)

**4. Create Issue:**
- GitHub Issue with all info
- Or team chat with logs

---

## 📚 Additional Resources

- [E2E Database Isolation Documentation](./E2E_DATABASE_ISOLATION.md) - Complete technical guide
- [README.md](./README.md) - E2E test overview and quick start
- [Playwright Documentation](https://playwright.dev/docs/debug)
- [Docker Documentation](https://docs.docker.com/)

---

*Last Updated: 2025-12-28*
*Version: 2.0.0*
