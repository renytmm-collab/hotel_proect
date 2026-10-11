<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { login as userLogin } from '@/api/user'
import { staffLogin } from '@/api/staff'
import { login as adminLogin } from '@/api/admin'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activeTab = ref('user')
const isAlreadyLoggedIn = ref(false)
const currentUsername = ref('')
const currentRole = ref('')

// 获取重定向URL
const userRedirectUrl = computed(() => route.query.redirect || '/')
const staffRedirectUrl = computed(() => route.query.redirect || '/staff/orders')
const adminRedirectUrl = computed(() => '/admin')

// 用户登录表单
const userLoginForm = reactive({
  phone: '',
  password: ''
})

// 员工登录表单
const staffLoginForm = reactive({
  empNo: '',
  password: ''
})

// 表单验证规则
const userRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const staffRules = {
  empNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const userFormRef = ref(null)
const staffFormRef = ref(null)

// 检查用户是否已经登录
onMounted(() => {
  checkLoginStatus()
})

// 检查登录状态
const checkLoginStatus = () => {
  // 检查是否是员工登录
  const staffLoggedIn = localStorage.getItem('staffLoggedIn') === 'true'
  const staffUsername = localStorage.getItem('staffUsername')
  const employeeData = localStorage.getItem('LoginEmployee')
  
  // 检查是否是用户登录
  const userToken = localStorage.getItem('token')
  const userData = localStorage.getItem('loginUser')
  
  if (employeeData) {
    try {
      const employee = JSON.parse(employeeData)
      if (employee.roleId === 1) {
        // 管理员登录
        isAlreadyLoggedIn.value = true
        activeTab.value = 'staff'
        currentUsername.value = employee.username || employee.realName || '管理员'
        currentRole.value = '管理员'
      } else {
        // 员工登录
        isAlreadyLoggedIn.value = true
        activeTab.value = 'staff'
        currentUsername.value = employee.username || employee.realName || staffUsername || '员工'
        currentRole.value = '员工'
      }
    } catch (e) {
      if (staffLoggedIn && staffUsername) {
        isAlreadyLoggedIn.value = true
        activeTab.value = 'staff'
        currentUsername.value = staffUsername || '员工'
        currentRole.value = '员工'
      }
    }
  } else if (userToken && userData) {
    // 用户登录
    isAlreadyLoggedIn.value = true
    activeTab.value = 'user'
    try {
      const user = JSON.parse(userData)
      currentUsername.value = user.name || user.phone || '用户'
      currentRole.value = '用户'
    } catch (e) {
      currentUsername.value = '用户'
      currentRole.value = '用户'
    }
  } else {
    // 清除不完整的登录信息
    clearAllLoginState()
  }
}

// 清除所有登录状态
const clearAllLoginState = () => {
  // 清除员工/管理员登录状态
  localStorage.removeItem('staffLoggedIn')
  localStorage.removeItem('staffUsername')
  localStorage.removeItem('staffRole')
  localStorage.removeItem('staffToken')
  localStorage.removeItem('LoginEmployee')
  
  // 清除用户登录状态
  localStorage.removeItem('token')
  localStorage.removeItem('loginUser')
  
  isAlreadyLoggedIn.value = false
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前登录吗？', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearAllLoginState()
    ElMessage.success('已退出登录')
  }).catch(() => {
    // 用户取消退出，无需操作
  })
}

// 继续使用当前登录
const enterSystem = () => {
  if (activeTab.value === 'user') {
    router.push(userRedirectUrl.value)
  } else {
    // 判断是管理员还是员工
    const employeeData = localStorage.getItem('LoginEmployee')
    if (employeeData) {
      try {
        const employee = JSON.parse(employeeData)
        if (employee.roleId === 1) {
          router.push(adminRedirectUrl.value)
        } else {
          router.push(staffRedirectUrl.value)
        }
      } catch (e) {
        router.push(staffRedirectUrl.value)
      }
    } else {
      router.push(staffRedirectUrl.value)
    }
  }
}

// 处理用户登录
const handleUserLogin = () => {
  if (!userFormRef.value) return
  
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 清除之前的登录状态
        clearAllLoginState()
        
        const res = await userLogin(userLoginForm)
        
        // 保存登录状态
        localStorage.setItem('loginUser', JSON.stringify(res.data))
        localStorage.setItem('token', res.data.token || 'dummy-token')
        
        ElMessage.success('登录成功')
        
        // 登录成功后跳转
        router.push(userRedirectUrl.value)
      } catch (error) {
        console.error('登录失败:', error)
        
        // 获取具体的错误信息
        let errorMsg = '登录失败，请检查账号密码'
        
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || errorMsg
        } else if (error.message) {
          errorMsg = error.message
        }
        
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}

// 处理员工登录
const handleStaffLogin = () => {
  if (!staffFormRef.value) return
  
  staffFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 清除之前的登录状态
        clearAllLoginState()
        
        // 尝试员工登录
        const staffRes = await staffLogin({
          empNo: staffLoginForm.empNo,
          password: staffLoginForm.password
        })
        
        if (staffRes.code === 200) {
          // 员工登录成功，保存共同的信息
          const employeeData = {
            ...staffRes.data,
            token: staffRes.data.token
          }
          
          // 保存登录信息到localStorage
          localStorage.setItem('LoginEmployee', JSON.stringify(employeeData))
          
          // 无论是管理员还是员工，都设置staffToken
          localStorage.setItem('staffToken', staffRes.data.token || 'dummy-token')
          
          // 根据role_id判断是管理员还是普通员工
          if (staffRes.data.roleId === 1) {
            // 管理员登录成功
            localStorage.setItem('staffLoggedIn', 'true')
            localStorage.setItem('staffUsername', staffRes.data.username || staffRes.data.realName || staffLoginForm.empNo)
            localStorage.setItem('staffRole', '1')
            
            ElMessage.success('管理员登录成功')
            // 跳转到管理首页
            router.push(adminRedirectUrl.value)
          } else {
            // 普通员工登录成功
            localStorage.setItem('staffLoggedIn', 'true')
            localStorage.setItem('staffUsername', staffRes.data.username || staffRes.data.realName || staffLoginForm.empNo)
            localStorage.setItem('staffRole', staffRes.data.roleId || '2')
            
            ElMessage.success('员工登录成功')
            // 跳转到员工页面
            router.push(staffRedirectUrl.value)
          }
        } else {
          // 使用后端返回的具体错误信息
          ElMessage.error(staffRes.msg || staffRes.message || '登录失败')
        }
      } catch (error) {
        console.error('登录错误:', error)
        
        // 获取具体的错误信息
        let errorMsg = '登录失败，请检查账号密码'
        
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || errorMsg
        } else if (error.message) {
          errorMsg = error.message
        }
        
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}

// 前往注册页面
const goToRegister = () => {
  router.push('/user/register')
}

// 切换登录方式
const handleTabChange = (tab) => {
  clearAllLoginState()
  isAlreadyLoggedIn.value = false
}
</script>

<template>
  <div class="unified-login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>酒店预订系统</h1>
      </div>
      
      <!-- 已登录提示 -->
      <div v-if="isAlreadyLoggedIn" class="already-logged-in">
        <p>您已经以 <span class="highlight">{{ currentRole }}</span> 身份登录</p>
        <p>当前账号: <span class="highlight">{{ currentUsername }}</span></p>
        <div class="action-buttons">
          <el-button type="primary" @click="enterSystem">继续使用</el-button>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
      
      <!-- 登录表单 -->
      <div v-else class="login-form-wrapper">
        <el-tabs v-model="activeTab" class="login-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="用户登录" name="user">
            <el-form 
              ref="userFormRef"
              :model="userLoginForm" 
              :rules="userRules" 
              label-position="top"
            >
              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="userLoginForm.phone"
                  placeholder="请输入手机号"
                  prefix-icon="Phone"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="userLoginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleUserLogin"
                />
              </el-form-item>
              <div class="form-actions">
                <el-button 
                  type="primary" 
                  :loading="loading" 
                  @click="handleUserLogin"
                  style="width: 100%;"
                >
                  登录
                </el-button>
              </div>
              <div class="form-links">
                <el-button text @click="goToRegister">没有账号？立即注册</el-button>
              </div>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="员工登录" name="staff">
            <el-form
              ref="staffFormRef"
              :model="staffLoginForm"
              :rules="staffRules"
              label-position="top"
            >
              <el-form-item label="员工编号" prop="empNo">
                <el-input 
                  v-model="staffLoginForm.empNo"
                  placeholder="请输入员工编号"
                  prefix-icon="User"
                />
              </el-form-item>
              
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="staffLoginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleStaffLogin"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  class="login-button" 
                  :loading="loading"
                  @click="handleStaffLogin"
                >
                  登录
                </el-button>
              </el-form-item>
              
              <div class="login-tips">
                <p>员工和管理员请使用员工编号登录</p>
              </div>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="login-footer">
        <p>© {{ new Date().getFullYear() }} 酒店预订管理系统 版权所有</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.unified-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e5799 0%, #207cca 51%, #2989d8 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 450px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
  padding: 30px;
  position: relative;
  overflow: hidden;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.login-form-wrapper {
  margin-bottom: 20px;
}

.login-tabs {
  width: 100%;
}

.form-actions {
  margin-top: 20px;
}

.form-links {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

.already-logged-in {
  text-align: center;
  margin: 30px 0;
}

.already-logged-in p {
  margin-bottom: 15px;
  color: #606266;
  font-size: 16px;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.login-tips {
  text-align: center;
  margin-top: 15px;
  color: #909399;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  color: #909399;
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-box {
    padding: 20px;
  }
  
  .login-header h1 {
    font-size: 24px;
  }
}
</style> 