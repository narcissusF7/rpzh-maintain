import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/index.css'

import App from './App.vue'
import router from './router'
import pinia from './store'
import { useAppStore } from './store/modules/app'

// 路由守卫（登录态 + 动态路由注册）
import './permission'

const app = createApp(App)

// 图标全局注册，菜单 icon 字段直接写组件名即可（如 Setting / User）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 恢复上次的主题
useAppStore().applyTheme()

app.mount('#app')
