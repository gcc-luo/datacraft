import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({plugins: [vue()], server: {port: 8081, proxy: {'/api': 'http://localhost:9081', '/actuator': 'http://localhost:9081'}}})

