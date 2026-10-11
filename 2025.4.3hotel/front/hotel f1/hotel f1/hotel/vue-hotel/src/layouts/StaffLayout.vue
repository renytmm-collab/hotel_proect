<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Tickets, 
  Calendar, 
  Document, 
  House, 
  Avatar, 
  Clock, 
  ArrowDown,
  ChatDotSquare
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const staffUsername = computed(() => localStorage.getItem('staffUsername') || '员工')

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    // 清除登录状态
    localStorage.removeItem('staffLoggedIn')
    localStorage.removeItem('staffUsername')
    localStorage.removeItem('staffRole')
    localStorage.removeItem('staffToken')
    localStorage.removeItem('LoginEmployee')
    
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    // 导航到个人中心页面
    router.push('/staff/profile')
  }
}
</script>

<template>
  <div class="staff-layout">
    <el-container>
      <el-header height="60px">
        <div class="header-container">
          <div class="logo">
            <h1>酒店管理系统</h1>
          </div>
          
          <el-menu
            :default-active="activeMenu"
            class="el-menu-horizontal"
            mode="horizontal"
            :router="true"
            :collapse="false"
            :collapse-transition="false"
          >
            <el-menu-item index="/staff/orders" class="menu-item-orders">
              <el-icon><Tickets /></el-icon>
              <span>预订管理</span>
            </el-menu-item>
            
            <el-sub-menu index="check-in">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>入住管理</span>
              </template>
              <el-menu-item index="/staff/checkin">入住登记</el-menu-item>
              <el-menu-item index="/staff/checkin/list">入住记录</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="check-out">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>退房管理</span>
              </template>
              <el-menu-item index="/staff/checkout">退房结算</el-menu-item>
              <el-menu-item index="/staff/checkout/list">退房记录</el-menu-item>
            </el-sub-menu>
            
            <el-menu-item index="/staff/room-status">
              <el-icon><House /></el-icon>
              <span>房态管理</span>
            </el-menu-item>
            
            <el-menu-item index="/staff/guest">
              <el-icon><Avatar /></el-icon>
              <span>客人信息</span>
            </el-menu-item>
            
            <el-menu-item index="/staff/reviews">
              <el-icon><ChatDotSquare /></el-icon>
              <span>评价管理</span>
            </el-menu-item>
            
            <el-menu-item index="/staff/attendance">
              <el-icon><Clock /></el-icon>
              <span>考勤管理</span>
            </el-menu-item>
          </el-menu>
          
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ staffUsername }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
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
.staff-layout {
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
}

.header-container {
  display: flex;
  align-items: center;
  height: 60px;
  width: 100%;
  padding: 0 5px;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 5px;
  flex-shrink: 0;
}

.logo h1 {
  color: #409EFF;
  font-size: 14px;
  margin: 0;
  white-space: nowrap;
}

.el-menu-horizontal {
  border-bottom: none;
  flex: 1;
  overflow-x: auto;
  white-space: nowrap;
  width: calc(100% - 130px);
  justify-content: space-between;
}

.el-menu-horizontal::-webkit-scrollbar {
  height: 0;  /* 隐藏滚动条但保留功能 */
}

:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  font-size: 14px;
  padding: 0 10px !important;
  height: 60px;
  line-height: 60px;
  box-sizing: border-box;
}

:deep(.el-menu-item .el-icon), :deep(.el-sub-menu__title .el-icon) {
  margin-right: 5px;
  font-size: 16px;
  vertical-align: middle;
}

:deep(.el-menu-item span), :deep(.el-sub-menu__title span) {
  font-size: 14px;
  vertical-align: middle;
}

/* 清除所有特殊样式，使所有按钮保持一致 */
:deep(.menu-item-orders) {
  font-weight: normal !important;
  letter-spacing: normal !important;
}

:deep(.menu-item-orders.is-active) {
  background-color: transparent !important;
}

/* 用户信息区域样式 */
.user-info {
  margin-left: 5px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  min-width: 80px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #303133;
  font-size: 14px;
  height: 60px;
  line-height: 60px;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}

/* 媒体查询调整 */
@media (max-width: 992px) {
  .el-menu-horizontal {
    width: calc(100% - 120px);
  }
  
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    padding: 0 10px !important;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .el-menu-horizontal {
    width: calc(100% - 110px);
  }
  
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    padding: 0 8px !important;
    font-size: 12px;
  }
  
  .user-info {
    min-width: 80px;
  }
}

:deep(.el-menu--horizontal>.el-sub-menu .el-sub-menu__title) {
  height: 60px;
  line-height: 60px;
}

:deep(.el-menu--horizontal>.el-sub-menu .el-sub-menu__icon-arrow) {
  position: static;
  margin-left: 6px;
  margin-right: 0;
  vertical-align: middle;
}

:deep(.el-menu--horizontal>.el-sub-menu .el-menu--popup) {
  min-width: 140px;
  padding: 0;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

:deep(.el-menu--horizontal .el-menu .el-menu-item) {
  height: 40px;
  line-height: 40px;
  padding: 0 15px;
  color: #606266;
}

:deep(.el-menu--horizontal .el-menu .el-menu-item:hover) {
  color: #409EFF;
  background-color: #ecf5ff;
}

/* 确保垂直菜单不显示 */
:deep(.el-menu--vertical),
:deep(.el-menu--vertical.el-menu--popup) {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
  pointer-events: none !important;
  position: absolute !important;
  left: -9999px !important;
}

/* 确保子菜单正确显示为下拉 */
:deep(.el-sub-menu .el-menu) {
  position: absolute !important;
  top: 60px !important;
  left: auto !important;
  background: #fff !important;
  border-radius: 4px !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1) !important;
}

/* 确保子菜单项样式正确 */
:deep(.el-sub-menu .el-menu .el-menu-item) {
  height: 40px !important;
  line-height: 40px !important;
  background-color: transparent !important;
}

:deep(.el-menu--horizontal>.el-menu-item.is-active) {
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
}

:deep(.el-menu--horizontal>.el-sub-menu.is-active .el-sub-menu__title) {
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
}
</style>