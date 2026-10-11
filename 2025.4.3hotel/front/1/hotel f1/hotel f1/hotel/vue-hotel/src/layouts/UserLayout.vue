<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 获取用户名
const username = computed(() => {
  const userInfo = localStorage.getItem('loginUser')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.nickname || user.username || '用户'
    } catch (e) {
      return '用户'
    }
  }
  return '用户'
})

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除登录信息
    localStorage.removeItem('token')
    localStorage.removeItem('loginUser')
    
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 取消操作
  })
}

// 根据当前路由路径计算激活的菜单项
const activeIndex = computed(() => {
  const path = route.path
  if (path === '/' || path.includes('/home')) {
    return '/'
  } else if (path.includes('room/list') || path.includes('room/detail')) {
    return '/room/list'
  } else if (path.includes('user/profile') || path.includes('user/change-password')) {
    return '/user/profile'
  } else if (path.includes('order/list') || path.includes('order/detail') || path.includes('order/create')) {
    return '/order/list'
  } else if (path.includes('comment') || path.includes('review')) {
    return '/comment'
  } else if (path.includes('favorite')) {
    return '/favorite'
  }
  return '/'
})
</script>

<template>
  <div class="user-layout">
    <!-- 导航菜单 -->
    <div class="user-header">
      <div class="header-container">
        <div class="logo">酒店预订系统</div>
        <el-menu
          :default-active="activeIndex"
          class="user-menu"
          mode="horizontal"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/room/list">客房预订</el-menu-item>
          <el-menu-item index="/user/profile">个人中心</el-menu-item>
          <el-menu-item index="/order/list">我的订单</el-menu-item>
          <el-menu-item index="/comment">我的评价</el-menu-item>
          <el-menu-item index="/favorite">我的收藏</el-menu-item>
        </el-menu>
        
        <!-- 用户信息和退出登录 -->
        <div class="user-info">
          <el-dropdown @command="handleLogout">
            <span class="el-dropdown-link">
              {{ username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="user-content">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.user-layout {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.user-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  white-space: nowrap;
  margin-right: 20px;
}

.user-menu {
  border-bottom: none;
  flex: 1;
  overflow-x: auto;
}

.user-menu::-webkit-scrollbar {
  height: 3px;
}

.user-menu::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

:deep(.el-menu--horizontal > .el-menu-item) {
  height: 60px;
  line-height: 60px;
  padding: 0 15px;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
}

.user-info {
  margin-left: 20px;
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #303133;
  font-size: 14px;
}

.el-dropdown-link:hover {
  color: #409EFF;
}

.user-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .header-container,
  .user-content {
    padding: 0 10px;
  }
  
  .logo {
    font-size: 16px;
    margin-right: 10px;
  }
  
  :deep(.el-menu--horizontal > .el-menu-item) {
    padding: 0 10px;
    font-size: 14px;
  }
  
  .user-info {
    margin-left: 10px;
  }
}

@media (max-width: 576px) {
  .logo {
    display: none;
  }
  
  :deep(.el-menu--horizontal > .el-menu-item) {
    padding: 0 8px;
    font-size: 13px;
  }
}
</style>