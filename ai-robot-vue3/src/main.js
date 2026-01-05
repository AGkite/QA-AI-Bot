import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
// 注册 SVG Icon
import 'virtual:svg-icons-register'

const app = createApp(App)

app.use(router)
app.mount('#app')
