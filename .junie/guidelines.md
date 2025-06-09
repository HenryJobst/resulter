# Resulter Project Guidelines for Junie

This document outlines the guidelines for Junie when working with the Resulter project.

## Project Structure

Resulter is a full-stack application with:

### Backend (Java)
- **Hexagonal/Clean Architecture** with clear separation of concerns:
  - `src/main/java/de/jobst/resulter/domain`: Core business logic and entities
  - `src/main/java/de/jobst/resulter/application`: Application services and use cases
  - `src/main/java/de/jobst/resulter/adapter`: External interfaces (REST, DB)
  - `src/main/java/de/jobst/resulter/springapp`: Spring configuration

### Frontend (Vue.js/TypeScript)
- **Feature-based organization**:
  - `frontend/src/features`: Feature modules
  - `frontend/src/components`: Reusable UI components
  - `frontend/src/assets`: Static resources
  - `frontend/src/utils`: Utility functions

## Code Style Guidelines

### Java
- Use 4-space indentation
- Follow Domain-Driven Design principles
- Use JMolecules annotations for DDD concepts
- Use domain-specific types rather than primitives
- Use interfaces for services
- Use proper JavaDoc comments
- Follow clean code principles

### TypeScript/Vue
- Use 4-space indentation
- Use TypeScript interfaces for data models
- Use Vue 3 Composition API with `<script setup>` syntax
- Use PrimeVue components for UI
- Use Vue Query for data fetching
- Use Vue Router for navigation
- Use Vue i18n for internationalization

## Testing Guidelines

When implementing changes:
1. Run relevant unit tests to ensure functionality
2. For backend changes: `./mvnw test -Dtest=<TestClass>`
3. For frontend changes: `cd frontend && npm run test:unit`

## Build Process

The project uses Maven for the backend and npm for the frontend:
- Backend: `./mvnw clean install`
- Frontend: `cd frontend && npm run build`

## Submission Guidelines

Before submitting changes:
1. Ensure all tests pass
2. Verify that the code follows the project's style guidelines
3. Provide a clear description of the changes made
4. If applicable, include any necessary documentation updates
