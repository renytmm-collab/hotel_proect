<template>
  <div class="test-route-container">
    <h1>路由测试页面</h1>
    
    <div class="test-actions">
      <h3>调试信息</h3>
      <el-button type="primary" @click="checkEnvironment">
        检查环境变量
      </el-button>
      <el-button type="danger" @click="testDirectAPI">
        直接测试API
      </el-button>
      <div v-if="envInfo" class="env-info">
        <h4>环境变量信息：</h4>
        <pre>{{ envInfo }}</pre>
      </div>
      <div v-if="apiResult" class="api-result">
        <h4>API测试结果：</h4>
        <pre>{{ apiResult }}</pre>
      </div>
    </div>
    
    <div class="test-actions">
      <h3>路由测试操作</h3>
      <el-button type="primary" @click="navigateAbsolute">
        使用绝对路径导航 (/room/list)
      </el-button>
      
      <el-button type="success" @click="navigateRelative">
        使用相对路径导航 (room/list)
      </el-button>
      
      <el-button type="info" @click="navigateHardcoded">
        使用硬编码路径导航
      </el-button>
      
      <el-button type="warning" @click="navigateWithRouter">
        使用push方法导航
      </el-button>
    </div>
    
    <div class="current-route">
      <h3>当前路由信息：</h3>
      <pre>{{ currentRoute }}</pre>
    </div>
    
    <div class="available-routes">
      <h3>可用路由列表：</h3>
      <ul>
        <li v-for="route in availableRoutes" :key="route.path">
          路径: {{ route.path }}, 名称: {{ route.name }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const envInfo = ref(null)
const apiResult = ref(null)

const currentRoute = computed(() => {
  return {
    path: route.path,
    fullPath: route.fullPath,
    name: route.name,
    params: route.params,
    query: route.query,
    meta: route.meta
  }
})

const availableRoutes = computed(() => {
  return router.getRoutes().filter(r => r.path.includes('room') || r.name?.includes('Room'))
})

const checkEnvironment = () => {
  console.log('检查环境变量')
  envInfo.value = {
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
    BASE_URL: import.meta.env.BASE_URL,
    DEV: import.meta.env.DEV,
    PROD: import.meta.env.PROD,
    MODE: import.meta.env.MODE,
    window_location: window.location.href,
    origin: window.location.origin
  }
}

const testDirectAPI = async () => {
  try {
    apiResult.value = { status: '请求中...' }
    
    // 测试房型API
    const roomResponse = await axios.get('/api/roomType/hot')
    
    apiResult.value = {
      status: '成功',
      data: roomResponse.data
    }
    ElMessage.success('API测试成功')
  } catch (error) {
    console.error('API测试失败:', error)
    apiResult.value = {
      status: '失败',
      error: error.message,
      response: error.response?.data
    }
    ElMessage.error('API测试失败')
  }
}

const navigateAbsolute = () => {
  console.log('导航到绝对路径: /room/list')
  window.location.href = '/room/list'
}

const navigateRelative = () => {
  console.log('导航到相对路径: room/list')
  window.location.href = 'room/list'
}

const navigateHardcoded = () => {
  console.log('使用硬编码URL导航')
  window.location.href = `${window.location.origin}/room/list`
}

const navigateWithRouter = () => {
  console.log('使用router.push导航到/room/list')
  router.push('/room/list')
}
</script>

<style scoped>
.test-route-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-actions {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.test-actions .el-button {
  margin: 10px;
}

.current-route, .available-routes, .env-info, .api-result {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f7fa;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #ecf5ff;
  padding: 10px;
  border-radius: 4px;
}

ul {
  padding-left: 20px;
}

li {
  margin-bottom: 5px;
}
</style> 