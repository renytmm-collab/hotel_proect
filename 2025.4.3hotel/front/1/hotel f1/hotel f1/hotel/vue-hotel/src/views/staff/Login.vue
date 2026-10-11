<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { staffLogin } from '@/api/staff'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const isAlreadyLoggedIn = ref(false)

// 获取重定向URL
const redirectUrl = computed(() => route.query.redirect || '/staff/orders')

// 登录表单数据
const loginForm = reactive({
  empNo: '',
  password: ''
})

// 表单验证规则
const rules = {
  empNo: [
    { required: true, message: '请输入员工账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 检查是否已经登录
onMounted(() => {
  const staffLoggedIn = localStorage.getItem('staffLoggedIn') === 'true'
  const staffUsername = localStorage.getItem('staffUsername')
  
  if (staffLoggedIn && staffUsername) {
    isAlreadyLoggedIn.value = true
  } else {
    // 如果登录状态不完整，清除所有登录信息
    clearLoginState()
  }
})

// 清除登录状态
const clearLoginState = () => {
  console.log('清除员工登录状态')
  localStorage.removeItem('staffLoggedIn')
  localStorage.removeItem('staffUsername')
  localStorage.removeItem('staffRole')
  localStorage.removeItem('staffToken')
  localStorage.removeItem('LoginEmployee')
  isAlreadyLoggedIn.value = false
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前登录吗？', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearLoginState()
    ElMessage.success('已退出登录')
  }).catch(() => {
    // 用户取消退出，无需操作
  })
}

// 直接进入系统
const enterSystem = () => {
  router.push(redirectUrl.value)
}

// 处理登录
const handleLogin = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        // 清除之前的登录状态
        clearLoginState()
        
        console.log('尝试员工登录:', loginForm.empNo)
        const res = await staffLogin({
          empNo: loginForm.empNo,
          password: loginForm.password
        })
        
        if (res.code === 200) {
          console.log('员工登录成功:', res.data)
          
          // 保存登录状态
          localStorage.setItem('staffLoggedIn', 'true')
          localStorage.setItem('staffUsername', res.data.username || res.data.realName || loginForm.empNo)
          localStorage.setItem('staffRole', res.data.roleId || '1')
          
          // 保存完整员工数据和token
          const employeeData = {
            ...res.data,
            token: res.data.token
          }
          localStorage.setItem('LoginEmployee', JSON.stringify(employeeData))
          console.log('已保存员工信息和Token')
          
          ElMessage.success('登录成功')
          
          console.log('登录成功，准备跳转到:', redirectUrl.value)
          
          // 登录成功后跳转到重定向URL或默认页面
          setTimeout(() => {
            router.push(redirectUrl.value)
          }, 300)
        } else {
          // 使用后端返回的具体错误信息
          ElMessage.error(res.msg || res.message || '登录失败')
        }
      } catch (error) {
        console.error('登录错误:', error)
        
        // 获取具体的错误信息
        let errorMsg = '登录失败，请检查账号密码';
        
        // 如果响应中有错误信息，优先使用响应中的错误信息
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || errorMsg;
        } else if (error.message) {
          // 如果有错误消息，使用错误消息
          errorMsg = error.message;
        }
        
        ElMessage.error(errorMsg);
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="staff-login-container">
    <div class="login-box">
      <h2>员工登录</h2>
      
      <!-- 已登录提示 -->
      <div v-if="isAlreadyLoggedIn" class="already-logged-in">
        <p>您已经登录，当前登录账号: {{ localStorage.getItem('staffUsername') }}</p>
        <div class="action-buttons">
          <el-button type="primary" @click="enterSystem">进入系统</el-button>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        v-else
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        size="large"
        class="login-form"
      >
        <el-form-item prop="empNo">
          <el-input 
            v-model="loginForm.empNo"
            placeholder="请输入员工账号"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-button" 
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.staff-login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

.already-logged-in {
  text-align: center;
  margin-bottom: 20px;
}

.already-logged-in p {
  margin-bottom: 20px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}
</style> 