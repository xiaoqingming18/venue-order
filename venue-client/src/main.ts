import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@fortawesome/fontawesome-free/css/all.css'

// 导入Vant组件库
import Vant from 'vant'
import 'vant/lib/index.css' // 导入Vant样式

// 导入全局样式
import './assets/styles/index.css'

import App from './App.vue'
import router from './router'

// 创建Vue应用实例
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(Vant) // 使用真正的Vant组件库

app.mount('#app')
