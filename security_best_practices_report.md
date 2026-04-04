# Sicherheitsbericht: Resulter

**Datum:** 2026-04-04
**Tech-Stack:** Java 21 / Spring Boot 4.x (Backend), Vue 3 / TypeScript / Vite (Frontend), PostgreSQL, Keycloak (OAuth2/OIDC), nginx

---

## Executive Summary

Die Anwendung hat eine solide Sicherheitsbasis: XXE-Angriffe sind explizit blockiert, CSRF-Schutz ist aktiviert, Session-Cookies sind korrekt mit `HttpOnly`, `Secure` und `SameSite=Lax` konfiguriert, JWT-Validierung inkl. Audience-Check ist vorhanden, und der XML-Parser ist gegen XXE gehärtet. Die kritischsten offenen Probleme betreffen fehlende HTTP-Security-Header im nginx-Produktivbetrieb sowie Debug-Logging, das personenbezogene Nutzerdaten in der Browser-Konsole exponiert.

---

## Kritisch

*(Keine kritischen Befunde)*

---

## Hoch

### FIND-001: Fehlende HTTP-Security-Header in nginx-Konfiguration

**Regel:** VUE-HEADERS-001
**Severity:** Hoch
**Datei:** `frontend/default.conf` (Zeilen 1–11)
**Impact:** Ohne `Content-Security-Policy` sind XSS-Angriffe wirkungsvoller, da der Browser keine Einschränkungen für Skript-Quellen kennt. Ohne `X-Frame-Options` / `frame-ancestors` ist die Anwendung für Clickjacking-Angriffe anfällig.

**Evidenz:**
```nginx
server {
    listen 80;
    server_name resulter.olberlin.de;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
    # Keine Security-Header konfiguriert
}
```

**Empfehlung:** Mindestens folgende Header hinzufügen:
```nginx
add_header X-Content-Type-Options "nosniff" always;
add_header X-Frame-Options "DENY" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none';" always;
```

*Hinweis: Die exakte CSP muss auf die verwendeten externen Ressourcen (PrimeIcons, etc.) abgestimmt werden. Mit `'unsafe-inline'` für Styles ist ein Start möglich, der dann schrittweise verschärft werden kann.*

---

### FIND-002: Debug-`console.log`-Aufrufe loggen PII und Auth-Zustand in Produktion

**Regel:** VUE-HTTP-001
**Severity:** Hoch
**Dateien:**
- `frontend/src/features/auth/store/auth.store.ts` (Zeilen 38, 48, 74, 76)
- `frontend/src/features/auth/plugins/authStorePlugin.ts` (Zeilen 26–32)
- `frontend/src/features/auth/services/bffAuthService.ts` (Zeilen 69, 82, 97)

**Impact:** E-Mail-Adresse, Benutzername, Rollen und Gruppen des eingeloggten Nutzers werden bei jedem Seitenaufruf in die Browser-Konsole geschrieben. Browser-Erweiterungen, Monitoringtools oder physischer Zugriff auf den Rechner können diese Daten abgreifen.

**Evidenz:**
```typescript
// auth.store.ts:38
console.log('[Auth Store] setBffUser called with:', userInfo)
// → loggt { username, email, name, roles, groups, permissions }

// authStorePlugin.ts:31-32
console.log('[BFF Auth Plugin] Store state:', {
    user: store.user,  // enthält E-Mail, Rollen, Gruppen
})
```

**Empfehlung:** Alle `console.log`-Aufrufe in Auth-Modulen entfernen oder durch einen Log-Guard ersetzen, der nur in Entwicklung aktiv ist:
```typescript
if (import.meta.env.DEV) {
    console.log('[Auth] ...', ...)
}
```

---

## Mittel

### FIND-003: Kein MIME-Typ-Allowlist für Media-Datei-Upload

**Severity:** Mittel
**Datei:** `backend/src/main/java/de/jobst/resulter/application/MediaFileServiceImpl.java` (Zeilen 58–94)

**Impact:** Obwohl der Upload auf Admins beschränkt ist, akzeptiert der Endpoint beliebige Dateitypen. Ein kompromittiertes Admin-Konto könnte ausführbare Dateien hochladen. Zudem wird zunächst der vom Client gesendete `Content-Type` verwendet (Zeile 145–147), der clientseitig fälschbar ist, bevor Tika zur Fallback-Erkennung genutzt wird.

**Evidenz:**
```java
private String getContentType(MultipartFile file) throws IOException {
    String contentType = file.getContentType(); // Vertraut Client-Header
    if (contentType != null) {
        return contentType;  // Keine Validation
    }
    Tika tika = new Tika();
    return tika.detect(file.getInputStream());
}
// Kein Allowlist-Check der erkannten MIME-Types
```

**Empfehlung:**
1. Den Client-`Content-Type` ignorieren und ausschließlich Tikas Erkennung verwenden.
2. Eine Allowlist erlaubter MIME-Types prüfen:
```java
private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
    "image/jpeg", "image/png", "image/gif", "image/webp",
    "application/pdf"
);
```

---

### FIND-004: Benutzerdaten (PII) werden in `sessionStorage` persistiert

**Regel:** VUE-AUTH-001
**Severity:** Mittel
**Datei:** `frontend/src/features/auth/store/auth.store.ts` (Zeilen 137–142)

**Impact:** Das Pinia-Store-Plugin persistiert `authenticated` und `user` (inkl. `username`, `email`, `roles`, `groups`) in `sessionStorage`. Diese Daten sind für jedes JavaScript auf der Seite lesbar. Bei einer XSS-Schwachstelle (z. B. über Drittbibliotheken) würden diese PII-Daten kompromittiert.

**Evidenz:**
```typescript
{
    persist: {
        storage: sessionStorage,
        paths: ['authenticated', 'user'],  // user enthält E-Mail, Rollen
    },
}
```

**Empfehlung:** Persistenz auf nicht-sensitive Daten beschränken oder ganz entfernen, da der Auth-Zustand beim App-Start ohnehin über `/bff/user` neu geladen wird:
```typescript
{
    persist: {
        storage: sessionStorage,
        paths: ['authenticated'], // Keine user-Objekt-Persistenz
    },
}
```

---

## Niedrig

### FIND-005: `target="_blank"` ohne `rel="noopener noreferrer"`

**Severity:** Niedrig
**Dateien:**
- `frontend/src/features/cup/pages/CupList.vue` (Zeile 65)
- `frontend/src/features/cup/pages/KjPokalResults.vue` (Zeile 276)
- `frontend/src/features/cup/pages/NorCupResults.vue` (Zeile 247)

**Impact:** Das neue Tab kann per `window.opener` auf das öffnende Fenster zugreifen. Da es sich hier um interne Router-Links handelt (nicht externe URLs), ist das Risiko gering.

**Evidenz:**
```html
<router-link target="_blank" ...>
```

**Empfehlung:**
```html
<router-link target="_blank" rel="noopener noreferrer" ...>
```

---

### FIND-006: VueDevTools-Plugin nicht auf Entwicklungsmodus beschränkt

**Severity:** Niedrig
**Datei:** `frontend/vite.config.ts` (Zeile 7–8, 13)

**Impact:** Das `vite-plugin-vue-devtools`-Plugin ist ohne Modusbeschränkung konfiguriert. Je nach Plugin-Version kann es in Production-Builds Debug-Infrastruktur einbetten.

**Evidenz:**
```typescript
import VueDevTools from 'vite-plugin-vue-devtools'
// ...
plugins: [
    vue(),
    VueI18nPlugin({ ... }),
    VueDevTools(), // Kein apply: 'serve' Guard
]
```

**Empfehlung:**
```typescript
...(process.env.NODE_ENV !== 'production' ? [VueDevTools()] : [])
```

---

## Positiv Festgestellt (Gut Umgesetzt)

- **XXE-Schutz:** JAXB-Unmarshaller mit `setSupportDtd(false)` und `setProcessExternalEntities(false)` explizit gehärtet (`XmlConfig.java`).
- **CSRF:** Cookie-basierter CSRF-Token für Session-Auth, korrekt implementiert mit `CookieCsrfTokenRepository` und `SameSite=Lax`.
- **Session-Cookies:** `HttpOnly=true`, `Secure=true`, `SameSite=Lax` korrekt gesetzt.
- **JWT-Validierung:** Issuer-URI-Validierung + Audience-Check implementiert (`OAuth2ResourceServerSecurityConfiguration.java:249–258`).
- **Path-Traversal-Schutz:** `MediaFileServiceImpl.sanitizeFilename()` und `filePath.startsWith(basePath)`-Check vorhanden.
- **CORS:** Origins über Environment-Variable konfiguriert, keine Wildcard-Allowlist.
- **Actuator:** Standardmäßig auf `none` gesetzt, nur `health`, `info` und `prometheus` exponiert.
- **XSS (v-html):** Beide `v-html`-Vorkommen (PrivacyPage, ImprintPage) werden mit DOMPurify bereinigt.
- **Dateipfad-Validierung:** Normalisierung und Basisverzeichnis-Check verhindert Path-Traversal beim Upload.

---

## Bericht gespeichert unter

`security_best_practices_report.md` (dieses Dokument)
