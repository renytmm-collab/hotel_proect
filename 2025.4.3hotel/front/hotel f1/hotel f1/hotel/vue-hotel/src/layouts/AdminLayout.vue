<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  House, 
  User, 
  Setting, 
  Tickets,
  OfficeBuilding,
  Money,
  Document,
  Clock,
  UserFilled,
  Medal,
  ArrowDown
} from '@element-plus/icons-vue'

// 在组件挂载时检查是否有导航栏重复的问题
onMounted(() => {
  console.log('AdminLayout 组件已挂载')
})

const router = useRouter()
const route = useRoute()
const activeMenu = ref(route.path)

// 监听路由变化实时更新高亮菜单
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })

// 获取管理员用户名
const adminUsername = ref(JSON.parse(localStorage.getItem('LoginEmployee'))?.username || '管理员')

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录信息
    localStorage.removeItem('LoginEmployee')
    localStorage.removeItem('staffLoggedIn')
    localStorage.removeItem('staffUsername')
    localStorage.removeItem('staffRole')
    localStorage.removeItem('staffToken')
    
    // 跳转到登录页
    router.push('/login')
  }).catch(() => {})
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/admin/profile-manage')
  }
}
</script>

<template>
  <div class="admin-layout">
    <el-container>
      <el-header>
        <div class="header-container">
          <div class="logo">
            <h1>酒店管理系统</h1>
          </div>
          
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            router
            class="el-menu-horizontal"
          >
            <el-menu-item index="/admin">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/account">
              <el-icon><User /></el-icon>
              <span>账户管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/role">
              <el-icon><Setting /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/staff">
              <el-icon><UserFilled /></el-icon>
              <span>员工管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/room-type">
              <el-icon><Tickets /></el-icon>
              <span>房型管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/room">
              <el-icon><OfficeBuilding /></el-icon>
              <span>房间管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/attendance">
              <el-icon><Clock /></el-icon>
              <span>考勤管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/performance">
              <el-icon><Medal /></el-icon>
              <span>绩效管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/finance">
              <el-icon><Money /></el-icon>
              <span>财务管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/report">
              <el-icon><Document /></el-icon>
              <span>报表统计</span>
            </el-menu-item>
          </el-menu>
          
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ adminUsername }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.el-container {
  min-height: 100vh;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  width: 100%;
}

.header-container {
  display: flex;
  align-items: center;
  height: 60px;
  width: 100%;
  padding: 0 10px;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 10px;
  flex-shrink: 0;
}

.logo h1 {
  color: #409EFF;
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
}

.el-menu-horizontal {
  border-bottom: none;
  flex: 1;
  overflow-x: auto;
  white-space: nowrap;
  width: calc(100% - 170px);
  justify-content: flex-start;
}

.el-menu-horizontal::-webkit-scrollbar {
  height: 4px;
}

.el-menu-horizontal::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

:deep(.el-menu--horizontal>.el-menu-item) {
  height: 60px;
  line-height: 60px;
  padding: 0 8px;
  font-size: 14px;
}

:deep(.el-menu--horizontal>.el-menu-item .el-icon) {
  margin-right: 3px;
  font-size: 16px;
}

:deep(.el-menu--horizontal>.el-menu-item.is-active) {
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
}

.user-info {
  margin-left: 10px;
  flex-shrink: 0;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.el-dropdown-link:hover {
  color: #409EFF;
}

.el-main {
  padding: 20px;
}

@media (max-width: 1200px) {
  :deep(.el-menu--horizontal>.el-menu-item) {
    padding: 0 8px;
    font-size: 13px;
  }
  
  :deep(.el-menu--horizontal>.el-menu-item .el-icon) {
    margin-right: 3px;
    font-size: 14px;
  }
  
  .logo h1 {
    font-size: 14px;
  }
}

@media (max-width: 992px) {
  .header-container {
    padding: 0 5px;
  }
  
  .logo h1 {
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  :deep(.el-menu--horizontal>.el-menu-item) {
    padding: 0 6px;
    font-size: 12px;
  }
  
  :deep(.el-menu--horizontal>.el-menu-item .el-icon) {
    margin-right: 2px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .logo {
    margin-right: 5px;
  }
  
  .logo h1 {
    font-size: 12px;
    max-width: 80px;
  }
  
  :deep(.el-menu--horizontal>.el-menu-item) {
    padding: 0 4px;
    font-size: 12px;
  }
  
  :deep(.el-menu--horizontal>.el-menu-item .el-icon) {
    margin-right: 1px;
    font-size: 12px;
  }
  
  .user-info {
    margin-left: 5px;
  }
}
</style> 