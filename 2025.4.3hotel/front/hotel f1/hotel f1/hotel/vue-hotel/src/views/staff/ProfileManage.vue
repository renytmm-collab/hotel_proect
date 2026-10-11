<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  getStaffInfo,
  updateStaffInfo,
  updateStaffPassword,
  staffLogout
} from '@/api/staff'
import { uploadFile } from '@/api/common'

const router = useRouter()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 用户信息
const userInfo = ref({})

// 个人信息表单
const profileForm = reactive({
  realName: '',
  phone: '',
  avatar: '',
  username: ''
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 当前激活的标签页
const activeTab = ref('profile')

// 获取用户信息
const fetchUserInfo = async () => {
  // 检查员工token是否存在
  const staffToken = localStorage.getItem('staffToken');
  if (!staffToken) {
    ElMessage.warning('未检测到登录状态，请重新登录');
    router.push('/staff/login');
    return;
  }
  
  loading.value = true
  try {
    const res = await getStaffInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      
      // 将返回的数据填充到表单
      profileForm.realName = res.data.realName
      profileForm.phone = res.data.phone
      profileForm.avatar = res.data.avatar
      profileForm.username = res.data.username
    } else {
      ElMessage.error(res.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    ElMessage.error('获取用户信息失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 提交个人信息表单
const submitProfileForm = async () => {
  if (!profileForm.realName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  
  submitting.value = true
  try {
    const res = await updateStaffInfo(profileForm)
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      fetchUserInfo() // 重新获取最新信息
    } else {
      ElMessage.error(res.message || '个人信息更新失败')
    }
  } catch (error) {
    console.error('更新个人信息错误:', error)
    ElMessage.error('个人信息更新失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 提交修改密码表单
const submitPasswordForm = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  
  submitting.value = true
  try {
    const res = await updateStaffPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      
      // 清除登录信息
      localStorage.removeItem('staffToken')
      localStorage.removeItem('staffLoggedIn')
      localStorage.removeItem('staffUsername')
      localStorage.removeItem('staffRole')
      
      // 跳转到登录页
      router.push('/staff/login')
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改错误:', error)
    ElMessage.error('密码修改失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 处理标签页切换
const handleTabChange = (tab) => {
  if (tab.props.name === 'password') {
    resetPasswordForm()
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await staffLogout()
      
      // 清除员工登录信息
      localStorage.removeItem('staffToken')
      localStorage.removeItem('staffLoggedIn')
      localStorage.removeItem('staffUsername')
      localStorage.removeItem('staffRole')
      
      // 提示退出成功
      ElMessage.success('退出成功')
      
      // 跳转到员工登录页
      router.push('/staff/login')
    } catch (error) {
      console.error('退出登录错误:', error)
      ElMessage.error('退出失败，请稍后再试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 处理头像上传
const handleImageChange = async (file) => {
  const formData = new FormData()
  formData.append('file', file.raw)
  
  try {
    const res = await uploadFile(formData)
    
    if (res.code === 200) {
      profileForm.avatar = res.data
      // profileForm.avatar = profileForm.avatar.replace('http://localhost:8080', 'http://localhost:4173');
      console.log(profileForm.avatar);
      
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('上传头像错误:', error)
    ElMessage.error('头像上传失败，请稍后再试')
  }
}

// 组件加载时获取数据
onMounted(() => {
  fetchUserInfo()
})
// 返回首页
const goToHome = () => {
  router.push('/staff/orders')
}
</script>

<template>
  <div class="profile-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-content">
            <h1 class="text-2xl font-bold">个人信息管理</h1>
            <div class="breadcrumb">
              <span @click="goToHome">首页</span> / <span>个人信息管理</span>
            </div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 个人信息标签页 -->
        <el-tab-pane label="基本信息" name="profile">
          <div class="user-profile">
            <div class="avatar-container">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleImageChange"
              >
                <el-avatar 
                  :size="100" 
                  :src="profileForm.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" 
                />
                <div class="avatar-edit">
                  <el-icon><Edit /></el-icon>
                </div>
              </el-upload>
              <div class="role-tag">
                <el-tag type="primary">{{ userInfo.roleName }}</el-tag>
              </div>
            </div>
            
            <el-form 
              :model="profileForm" 
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="员工编号">
                <el-input v-model="userInfo.empNo" disabled />
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="真实姓名" required>
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <!-- <el-form-item label="头像">
                <el-input v-model="profileForm.avatar" placeholder="请输入头像URL地址" />
              </el-form-item> -->
              <el-form-item label="性别">
                <el-input v-model="userInfo.sex" disabled />
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
                  {{ userInfo.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="创建时间">
                <el-input v-model="userInfo.createTime" disabled />
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  :loading="submitting" 
                  @click="submitProfileForm"
                >
                  保存修改
                </el-button>
                <el-button type="danger" @click="handleLogout">退出登录</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 修改密码标签页 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form 
            :model="passwordForm" 
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="原密码" required>
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                placeholder="请输入原密码" 
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" required>
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                placeholder="请输入新密码" 
                show-password
              />
            </el-form-item>
            <el-form-item label="确认新密码" required>
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                placeholder="请再次输入新密码" 
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                :loading="submitting" 
                @click="submitPasswordForm"
              >
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.profile-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.avatar-container {
  margin-bottom: 30px;
  text-align: center;
  position: relative;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-edit {
  opacity: 1;
}

.role-tag {
  margin-top: 10px;
}

.profile-form, .password-form {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.breadcrumb {
  font-size: 14px;
  color: #666;
}

.breadcrumb span {
  cursor: pointer;
}

.breadcrumb span:hover {
  color: #409EFF;
}

.el-button--primary {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
}

.el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

.el-button--danger {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
}

.el-button--danger:hover {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
}
</style> 