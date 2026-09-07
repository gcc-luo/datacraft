import { createApp } from 'vue'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/main.scss'

import { pinia } from './stores/pinia'

createApp(App).use(pinia).use(router).mount('#app')
