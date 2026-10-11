<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 判断是否是管理员页面（用于决定是否显示底部页脚）
const isAdminPage = ref(false)
// 判断是否是登录页
const isLoginPage = computed(() => route.path === '/login')

// 判断是否显示底部页脚
const showFooter = computed(() => !isAdminPage.value && !isLoginPage.value)

// 监听路由变化
watch(() => route.path, (newPath) => {
  isAdminPage.value = newPath.startsWith('/admin')
}, { immediate: true })

// 页面切换时回到顶部
router.afterEach(() => {
  window.scrollTo(0, 0)
})
</script>

<template>
  <div class="app-container">
    <el-container>
      <el-main>
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <keep-alive>
                <component :is="Component" :key="route.fullPath" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </el-main>
      <el-footer v-if="showFooter">
        <div class="footer-content">
          © {{ new Date().getFullYear() }} 酒店管理系统 - 版权所有
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<style>
/* 全局样式，确保所有页面都使用相同的布局约束 */
html, body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  width: 100%;
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 路由过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 隐藏水平滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

/* 禁用垂直侧导航栏的错误显示 - 增强版 */
.el-menu--vertical,
.el-menu--vertical.el-menu--popup,
body > .el-menu--vertical,
body > .el-menu--vertical.el-menu,
html > .el-menu--vertical,
#app + .el-menu--vertical,
[id="app"] ~ .el-menu--vertical {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  pointer-events: none !important;
  position: absolute !important;
  left: -9999px !important;
  z-index: -9999 !important;
}

/* 修复子菜单问题 */
.is-opened .el-sub-menu__title {
  border-bottom-color: transparent !important;
}

/* 确保子菜单内容始终以下拉方式显示，不显示为侧边导航 */
.el-menu--popup-container {
  position: fixed !important;
  left: auto !important;
  top: auto !important;
}

/* 覆盖Element Plus可能的样式 */
.el-menu-vertical-demo,
.el-menu--vertical.el-menu,
.el-popper.is-pure.el-menu--vertical {
  display: none !important;
}

/* 确保水平子菜单正确显示 */
.el-menu--horizontal .el-sub-menu .el-menu {
  display: block !important;
  position: absolute !important;
  min-width: 140px !important;
  border-radius: 4px !important;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1) !important;
  background: #fff !important;
  padding: 6px 0 !important;
}
</style>

<style scoped>
.app-container {
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.el-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.el-main {
  flex: 1;
  background-color: #f5f7fa;
  padding: 0;
  display: flex;
  justify-content: center;
  width: 100%;
  overflow-x: hidden;
}

.main-content {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.el-footer {
  background-color: #324057;
  color: #fff;
  text-align: center;
  line-height: 60px;
  width: 100%;
  padding: 0;
}

.footer-content {
  width: 1200px;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 添加响应式设计 */
@media screen and (max-width: 768px) {
  .footer-content {
    padding: 0 10px;
  }
}
</style>
<style scoped>
  .app-container {
    min-height: 100vh;
    background-color: #f5f7fa;
    background-image: url('@/assets/images/background.jpg');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    background-attachment: fixed;
  }
  
  .el-container {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    /* background-color: rgba(255, 255, 255, 0.95); */
  }
  
  .el-header {
    background-color: #fff;
    color: #333;
    line-height: 60px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 1000;
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  .logo {
    font-size: 24px;
    font-weight: bold;
    color: #409EFF;
    letter-spacing: 1px;
  }
  
  .el-menu {
    border-bottom: none !important;
  }
  
  .el-menu-item {
    font-size: 16px;
    transition: all 0.3s;
  }
  
  .el-menu-item:hover {
    color: #409EFF !important;
    background-color: #ecf5ff !important;
  }
  
  .el-menu-item.is-active {
    color: #409EFF !important;
    border-bottom: 2px solid #409EFF !important;
  }
  
  .el-main {
    flex: 1;
    background-color: rgba(255, 255, 255, 0.95) !important;
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
    width: 10000px;
  }
  
  .el-footer {
    background-color: #324057;
    color: #fff;
    text-align: center;
    line-height: 60px;
    margin-top: auto;
  }
  
  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  </style>
  
  <style>
  /* 全局样式 */
  .el-message-box__btns .el-button {
    background-color: #fff !important;
    border-color: #dcdfe6 !important;
    color: #606266 !important;
  }
  
  .el-message-box__btns .el-button--primary {
    background-color: #409eff !important;
    border-color: #409eff !important;
    color: #fff !important;
  }
  
  .el-message-box__btns .el-button--default {
    background-color: #fff !important;
    border-color: #dcdfe6 !important;
    color: #606266 !important;
  }
  
  .el-message-box__btns .el-button--primary:hover,
  .el-message-box__btns .el-button--primary:focus,
  .el-message-box__btns .el-button--primary:active {
    background-color: #409eff !important;
    border-color: #409eff !important;
    color: #fff !important;
    opacity: 0.9;
  }
  
  .el-message-box__btns .el-button--default:hover,
  .el-message-box__btns .el-button--default:focus,
  .el-message-box__btns .el-button--default:active {
    background-color: #f5f7fa !important;
    border-color: #dcdfe6 !important;
    color: #606266 !important;
  }
  
  /* 全局卡片样式 */
  .el-card {
    background-color: rgba(255, 255, 255, 0.95) !important;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
  }
  
  /* 全局表格样式 */
  .el-table {
    border-radius: 8px;
    overflow: hidden;
  }
  
  .el-table th {
    background-color: #f5f7fa !important;
    color: #606266;
    font-weight: 600;
  }
  
  /* 全局按钮样式 */
  .el-button {
    border-radius: 4px;
    transition: all 0.3s;
  }
  
  .el-button--primary {
    background-color: #409eff !important;
    border-color: #409eff !important;
  }
  
  .el-button--primary:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
  }
  
  /* 全局输入框样式 */
  .el-input__inner {
    border-radius: 4px;
  }
  
  /* 全局分页样式 */
  .el-pagination {
    margin-top: 20px;
    justify-content: center;
  }
  
  /* 全局对话框样式 */
  .el-dialog {
    background-color: rgba(255, 255, 255, 0.98) !important;
    border-radius: 8px;
  }
  
  .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-dialog__footer {
    padding: 20px;
    border-top: 1px solid #ebeef5;
  }
  
  /* 添加背景图片样式 */
  .el-main {
    background-color: rgba(255, 255, 255, 0.95) !important;
  }
  
  .el-card {
    background-color: rgba(255, 255, 255, 0.95) !important;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
  }
  
  .el-dialog {
    background-color: rgba(255, 255, 255, 0.98) !important;
  }
  
  .el-message-box {
    background-color: rgba(255, 255, 255, 0.98) !important;
  }
  </style>