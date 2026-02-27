/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                background: '#f8fafc',
                surface: '#ffffff',
                border: '#e2e8f0',
                primary: '#3b82f6',
                text: '#1e293b',
                'text-muted': '#64748b',
                success: '#22c55e',
                danger: '#ef4444',
                warning: '#f59e0b',
                info: '#0ea5e9'
            }
        },
    },
    plugins: [],
}
