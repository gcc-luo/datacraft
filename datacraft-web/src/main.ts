import { createApp } from 'vue'
import 'element-plus/dist/index.css'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import App from './App.vue'
import router from './router'
import './styles/main.scss'
import './styles/pipeline.scss'
import './styles/quality.scss'
import './styles/readability.scss'

import { pinia } from './stores/pinia'

createApp(App).use(pinia).use(router).mount('#app')
