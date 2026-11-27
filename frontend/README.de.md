# frontend

> **Deutsch** · [English](README.md)

Diese Vorlage soll dir den Einstieg in die Entwicklung mit Vue 3 in Vite erleichtern.

## Empfohlene IDE-Einrichtung

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (und Vetur deaktivieren) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).

## Typunterstützung für `.vue`-Importe in TS

TypeScript kann Typinformationen für `.vue`-Importe standardmäßig nicht verarbeiten. Deshalb ersetzen wir für die Typprüfung die `tsc`-CLI durch `vue-tsc`. In Editoren brauchen wir das [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin), damit der TypeScript-Language-Service die `.vue`-Typen kennt.

Wenn dir das eigenständige TypeScript-Plugin nicht schnell genug ist, hat Volar zusätzlich einen performanteren [Take Over Mode](https://github.com/johnsoncodehk/volar/discussions/471#discussioncomment-1361669) implementiert. Du aktivierst ihn mit den folgenden Schritten:

1. Die integrierte TypeScript-Erweiterung deaktivieren
    1) Führe in der Befehlspalette von VSCode `Extensions: Show Built-in Extensions` aus
    2) Suche `TypeScript and JavaScript Language Features`, klicke mit der rechten Maustaste und wähle `Disable (Workspace)`
2. Lade das VSCode-Fenster neu, indem du in der Befehlspalette `Developer: Reload Window` ausführst.

## Konfiguration anpassen

Siehe [Vite Configuration Reference](https://vitejs.dev/config/).

## Projekt einrichten

```sh
npm install
```

### Kompilieren und Hot-Reload für die Entwicklung

```sh
npm run dev
```

### Typprüfung, Kompilieren und Minifizieren für die Produktion

```sh
npm run build
```

### Unit-Tests mit [Vitest] ausführen(https://vitest.dev/)

```sh
npm run test:unit
```

### End-to-End-Tests mit [Cypress] ausführen(https://www.cypress.io/)

```sh
npm run test:e2e:dev
```

Dies führt die End-to-End-Tests gegen den Vite-Entwicklungsserver aus.
Das ist deutlich schneller als der Production-Build.

Es wird aber weiterhin empfohlen, vor dem Deployment (z. B. in CI-Umgebungen) den Production-Build mit `test:e2e` zu testen:

```sh
npm run build
npm run test:e2e
```

### Linting mit [ESLint](https://eslint.org/)

```sh
npm run lint
```
