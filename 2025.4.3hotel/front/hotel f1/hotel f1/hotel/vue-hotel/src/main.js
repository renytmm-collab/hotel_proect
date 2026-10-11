import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入Tailwind CSS样式
import './assets/css/tailwind.css'
// 导入按钮修复样式
import './assets/css/button-fix.css'
// 导入前台端按钮样式
import './assets/css/staff-buttons.css'
// 导入全局菜单样式
import './assets/css/global-menu-fix.css'
// 导入菜单清理脚本
import { setupMenuCleaner } from './assets/css/menu-cleanup.js'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')

// 应用挂载后，执行菜单清理
setupMenuCleaner()
