// https://on.cypress.io/api

describe('App Test', () => {
    it('visits the app root url', () => {
        cy.visit('/')
        cy.contains('h1', 'Resulter')
    })
})
