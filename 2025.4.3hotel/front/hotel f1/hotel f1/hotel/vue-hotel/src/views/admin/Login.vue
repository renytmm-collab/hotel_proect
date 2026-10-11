<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/admin'

const router = useRouter()
const route = useRoute()

// 获取重定向URL
const redirectUrl = route.query.redirect || '/admin'

// 表单数据
const loginForm = reactive({
  empNo: '',
  password: '',
  remember: false
})

// 表单规则
const rules = {
  empNo: [
    { required: true, message: '请输入员工编号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 登录状态
const loading = ref(false)
//绑定表单实例，调用表单的验证和重置方法。
const loginFormRef = ref(null)

// 登录处理
const handleLogin = () => {
  if (!loginFormRef.value) return

  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const res = await login({
          empNo: loginForm.empNo,
          password: loginForm.password
        })
        
        if (res.code === 200) {
          // 登录成功处理
          ElMessage.success('登录成功，欢迎回来')
          
          // 保存登录信息
          localStorage.setItem('LoginEmployee', JSON.stringify(res.data))
          
          // 跳转到重定向URL或管理首页
          router.push(redirectUrl)
        } else {
          ElMessage.error(res.message || '用户名或密码错误')
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请稍后再试')
      } finally {
        loading.value = false
      }
    } else {
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
}
</script>

<template>
  <div class="admin-login-container">
    <div class="login-box">
      <div class="login-header">
        <h2 class="title">酒店管理系统</h2>
        <p class="subtitle">管理员登录</p>
      </div>
      
      <div class="login-form">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="员工编号" prop="empNo">
            <el-input 
              v-model="loginForm.empNo"
              placeholder="请输入员工编号"
              prefix-icon="User"
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
          
          <el-form-item>
            <div class="remember-forgot">
              <el-checkbox v-model="loginForm.remember" class="remember-me">记住我</el-checkbox>
              <el-link type="primary" :underline="false" class="forgot-password">忘记密码?</el-link>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="info"
              plain
              class="reset-button"
              @click="resetForm"
            >
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="login-tips">
        <p>温馨提示：</p>
        <ul>
          <li>请输入您的员工编号和密码</li>
          <li>如果忘记密码，请联系系统管理员重置</li>
        </ul>
      </div>
      
      <div class="login-footer">
        <p>© {{ new Date().getFullYear() }} 酒店管理系统 版权所有</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e5799 0%, #207cca 51%, #2989d8 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
  padding: 30px;
  position: relative;
  overflow: hidden;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: #606266;
}

.login-form {
  margin-bottom: 20px;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}


.remember-me {
  text-align: left;
}

.forgot-password {
  margin-left: 230px;
}


.login-button, .reset-button {
  width: 100%;
  margin-top: 10px;
}

.login-button {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: #fff !important;
}

.login-button:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

.login-tips {
  background-color: #f8f9fa;
  border-left: 4px solid #409EFF;
  padding: 10px 15px;
  margin: 20px 0;
  font-size: 14px;
  color: #606266;
}

.login-tips ul {
  padding-left: 20px;
  margin: 10px 0;
}

.login-tips li {
  margin-bottom: 5px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-box {
    padding: 20px;
  }
  
  .title {
    font-size: 24px;
  }
}
</style> 