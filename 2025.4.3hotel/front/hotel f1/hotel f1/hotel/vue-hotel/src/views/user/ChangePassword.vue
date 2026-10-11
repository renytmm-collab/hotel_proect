<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const passwordFormRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证新密码
const validateNewPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 提交修改密码
const submitForm = () => {
  if (!passwordFormRef.value) return
  
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 准备数据
        const data = {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        }
        
        // 调用API
        const res = await updatePassword(data)
        ElMessage.success('密码修改成功，请重新登录')
        
        // 清除登录状态，跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/user/login')
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 返回个人信息页
const goBack = () => {
  router.push('/user/profile')
}
</script>

<template>
  <div class="password-container">
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
          <el-button text @click="goBack">返回个人中心</el-button>
        </div>
      </template>
      
      <el-form 
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
          <div class="form-tip">密码长度不少于6位,建议使用字母、数字和符号的组合</div>
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        
        <div class="form-actions">
          <el-button 
            type="primary" 
            @click="submitForm"
            :loading="loading"
          >
            确认修改
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </div>
      </el-form>
      
      <div class="security-tips">
        <h4>安全提示：</h4>
        <ul>
          <li>定期修改密码可以提高账号安全性</li>
          <li>建议使用字母、数字和符号的组合作为密码</li>
          <li>密码修改成功后，需要重新登录</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.password-container {
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}

.password-card {
  width: 100%;
  max-width: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-actions {
  margin-top: 30px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.security-tips {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #e6e6e6;
}

.security-tips h4 {
  margin-bottom: 10px;
  color: #606266;
}

.security-tips ul {
  padding-left: 20px;
  color: #909399;
  font-size: 13px;
}

.security-tips li {
  margin-bottom: 5px;
}
</style> 