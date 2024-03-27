import antfu from '@antfu/eslint-config'

export default antfu(
    {
        stylistic: {
            indent: 4, // 2, or 'tab'
            quotes: 'single', // or 'double'
        },

        // TypeScript and Vue are auto-detected, you can also explicitly enable them:
        typescript: true,
        vue: true,

        // `.eslintignore` is no longer supported in Flat config, use `ignores` instead
        ignores: ['**/node_modules', '**/dist'],

        formatters: false,
        unocss: false,
    },
    {
        rules: {
            'no-console': 'off',
        },
    },
)
