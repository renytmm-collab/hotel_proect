<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const loginForm = reactive({
  phone: '',
  password: ''
})
const loading = ref(false)
const loginFormRef = ref(null)

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        
        // 成功登录处理
        localStorage.setItem('loginUser', JSON.stringify(res.data));
        localStorage.setItem('token', res.data.token || 'dummy-token');
        
        ElMessage.success('登录成功')
        
        const redirectUrl = route.query.redirect
        if (redirectUrl) {
          router.push(redirectUrl)
        } else {
          router.push('/')
        }
      } catch (error) {
        console.error('登录失败:', error);
        
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

const goToRegister = () => {
  router.push('/user/register')
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>用户登录</h2>
        </div>
      </template>
      <el-form 
        ref="loginFormRef"
        :model="loginForm" 
        :rules="rules" 
        label-position="top"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <div class="form-actions">
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleLogin"
            style="width: 100%;"
          >
            登录
          </el-button>
        </div>
        <div class="form-links">
          <el-button text @click="goToRegister">没有账号？立即注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
}

.form-actions {
  margin-top: 20px;
}

.form-links {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}
</style> 