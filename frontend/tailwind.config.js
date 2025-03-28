import { HBlue } from './src/constants'

/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
    theme: {
        extend: {},
        colors: {
            'h-blue': HBlue,
        },
    },
    plugins: [],
};
