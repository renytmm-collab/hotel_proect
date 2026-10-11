<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})
const loading = ref(false)
const registerFormRef = ref(null)

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      // 对比确认密码
      if (value !== registerForm.confirmPassword) {
        callback(new Error('两次输入密码不一致'))
      }
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  password: [{ required: true, validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }]
}

const handleRegister = () => {
  if (!registerFormRef.value) return
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 准备注册数据，去除确认密码字段
        const registerData = {
          username: registerForm.username,
          password: registerForm.password,
          phone: registerForm.phone
        }
        
        // 调用注册API
        const res = await register(registerData)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '注册失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>用户注册</h2>
        </div>
      </template>
      <el-form 
        ref="registerFormRef"
        :model="registerForm" 
        :rules="rules" 
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input 
            v-model="registerForm.phone"
            placeholder="请输入手机号码"
            prefix-icon="Phone"
          />
        </el-form-item>
        <div class="form-actions">
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="handleRegister"
            style="width: 100%;"
          >
            注册
          </el-button>
        </div>
        <div class="form-links">
          <el-button text @click="goToLogin">已有账号？去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 400px;
}

.register-header {
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