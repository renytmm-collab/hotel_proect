<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  House,
  User,
  Tickets,
  OfficeBuilding,
  Money,
  Document,
  Clock,
  UserFilled,
  Medal,
  DataAnalysis,
  Collection,
  ArrowDown,
  Fold,
  Expand,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.path)
const collapsed = ref(false)

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })

const isBigScreen = computed(() => route.path === '/admin/bigscreen')

const pageTitle = computed(() => route.meta?.title || '运营管理后台')

const adminUsername = ref((() => {
  try {
    const emp = JSON.parse(localStorage.getItem('LoginEmployee') || '{}')
    return emp.realName || emp.username || '系统管理员'
  } catch {
    return '系统管理员'
  }
})())

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出管理后台吗？', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('LoginEmployee')
    localStorage.removeItem('staffLoggedIn')
    localStorage.removeItem('staffUsername')
    localStorage.removeItem('staffRole')
    localStorage.removeItem('staffToken')
    router.push('/login')
  }).catch(() => {})
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/admin/profile-manage')
  } else if (command === 'portal') {
    router.push('/')
  }
}
</script>

<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <!-- 左侧深色现代侧边栏（全部 11 项菜单清晰可见，不再溢出遮挡） -->
      <el-aside :width="collapsed ? '64px' : '220px'" class="admin-aside">
        <div class="brand" @click="router.push('/admin')">
          <span class="brand-badge">逸</span>
          <div v-if="!collapsed" class="brand-text">
            <div class="brand-name">逸云酒店管理</div>
            <div class="brand-sub">Hotel Admin</div>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#ffffff"
          class="admin-menu"
        >
          <el-menu-item index="/admin">
            <el-icon><House /></el-icon>
            <template #title>运营总览</template>
          </el-menu-item>

          <el-menu-item index="/admin/bigscreen" class="highlight-item">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>
              <span>经营数据大屏</span>
              <span class="menu-pill pill-teal">实时</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/admin/knowledge" class="highlight-item">
            <el-icon><Collection /></el-icon>
            <template #title>
              <span>RAG 知识库</span>
              <span class="menu-pill pill-blue">AI</span>
            </template>
          </el-menu-item>

          <div v-if="!collapsed" class="menu-group-label">客房与用户</div>

          <el-menu-item index="/admin/room-type">
            <el-icon><Tickets /></el-icon>
            <template #title>房型管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/room">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>房间管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/account">
            <el-icon><User /></el-icon>
            <template #title>住客账户</template>
          </el-menu-item>

          <div v-if="!collapsed" class="menu-group-label">人事与财务</div>

          <el-menu-item index="/admin/staff">
            <el-icon><UserFilled /></el-icon>
            <template #title>员工管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/attendance">
            <el-icon><Clock /></el-icon>
            <template #title>考勤管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/performance">
            <el-icon><Medal /></el-icon>
            <template #title>绩效管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/finance">
            <el-icon><Money /></el-icon>
            <template #title>财务管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/report">
            <el-icon><Document /></el-icon>
            <template #title>报表统计</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧主内容区 -->
      <el-container class="main-container">
        <el-header class="admin-header">
          <div class="header-left">
            <el-button text class="collapse-btn" @click="collapsed = !collapsed">
              <el-icon :size="18">
                <Expand v-if="collapsed" />
                <Fold v-else />
              </el-icon>
            </el-button>
            <span class="page-title">{{ pageTitle }}</span>
          </div>

          <div class="header-right">
            <el-button size="small" plain round @click="router.push('/')">
              <el-icon><Monitor /></el-icon>
              <span>住客门户</span>
            </el-button>

            <el-dropdown @command="handleCommand">
              <span class="user-trigger">
                <span class="user-avatar">{{ adminUsername.slice(0, 1) }}</span>
                <span class="user-name">{{ adminUsername }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="portal">前往住客首页</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main :class="['admin-main', { 'no-padding': isBigScreen }]">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: #f1f5f9;
}

.layout-container {
  min-height: 100vh;
}

.admin-aside {
  background: linear-gradient(180deg, #0f172a 0%, #111c33 100%);
  transition: width 0.22s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(15, 23, 42, 0.14);
  z-index: 10;
}

.brand {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.brand-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #14b8a6, #2563eb);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-name {
  color: #f8fafc;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.2;
}

.brand-sub {
  color: #64748b;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.admin-menu {
  border-right: none;
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
}

.menu-group-label {
  padding: 12px 20px 4px;
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(20, 184, 166, 0.24), rgba(37, 99, 235, 0.24)) !important;
  color: #ffffff !important;
  font-weight: 600;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.06) !important;
  color: #e2e8f0 !important;
}

.menu-pill {
  margin-left: auto;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 999px;
  line-height: 16px;
  font-weight: 600;
}

.pill-teal {
  background: rgba(20, 184, 166, 0.2);
  color: #2dd4bf;
}

.pill-blue {
  background: rgba(59, 130, 246, 0.22);
  color: #60a5fa;
}

.admin-header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.collapse-btn {
  padding: 6px;
  color: #475569;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #334155;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-trigger:hover {
  background-color: #f1f5f9;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 13.5px;
  font-weight: 500;
}

.main-container {
  min-width: 0;
  flex: 1;
}

.admin-main {
  padding: 20px;
  overflow-x: hidden;
  min-width: 0;
}

.admin-main.no-padding {
  padding: 0;
}
</style>
