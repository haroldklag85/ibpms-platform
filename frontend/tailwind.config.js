/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                ibpms: {
                    light: '#f8fafc',
                    DEFAULT: '#0f172a',
                    brand: '#3b82f6',
                    danger: '#ef4444'
                }
            }
        },
    },
    plugins: [],
}
