<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, realNameAuth, uploadAvatar } from '@/api/user'
import { Plus, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const isEditing = ref(false)
const loading = ref(false)
const realNameFormVisible = ref(false)
const isDev = ref(process.env.NODE_ENV === 'development')

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  phone: '',
  realName: '',
  idCard: '',
  sex: 0,
  avatar: '',
  status: 0,
  createTime: '',
  gender: ''
})

// 用于编辑的临时数据
const tempUserInfo = reactive({
  id: '',
  username: '',
  phone: '',
  realName: '',
  idCard: '',
  sex: 0,
  avatar: '',
  status: 0,
  createTime: '',
  gender: ''
})

// 实名认证表单
const realNameForm = reactive({
  realName: '',
  idCard: ''
})

// 表单校验规则
const rules = {
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 实名认证表单验证规则
const realNameRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 头像上传相关
const imageUrl = ref('')
const avatarRef = ref(null)
const token = computed(() => localStorage.getItem('token') || '')

// 从环境变量获取配置
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const uploadUrl = `${apiBaseUrl}/api/admin/common/upload` // 修改为正确的上传接口

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await getUserInfo()
    console.log('获取用户信息原始响应:', res)
    
    if (res.code === 200 && res.data) {
      // 处理后端返回的数据
      const userData = res.data;
      
      // 赋值到userInfo
      Object.assign(userInfo, userData)
      
      // 处理头像URL
      if (userInfo.avatar) {
        userInfo.avatar = processImageUrl(userInfo.avatar);
        console.log('处理后的头像URL:', userInfo.avatar)
      }
      
      // 性别从数字转为文本
      if (userInfo.sex === 1 || userInfo.sex === "1") {
        userInfo.gender = '男';
      } else if (userInfo.sex === 2 || userInfo.sex === "2") {
        userInfo.gender = '女';
      } else {
        userInfo.gender = '';
      }
      
      console.log('处理后的用户数据:', JSON.stringify(userInfo))
      
      // 更新头像URL
      imageUrl.value = userInfo.avatar || ''
      return true;
    } else {
      console.error('获取用户信息失败:', res)
      return false;
    }
  } catch (error) {
    console.error('获取用户信息异常:', error)
    ElMessage.error('获取用户信息失败')
    return false;
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})

const startEdit = () => {
  console.log('编辑前的用户信息:', JSON.stringify(userInfo))
  
  // 深拷贝用户信息到临时对象
  tempUserInfo.id = userInfo.id
  tempUserInfo.username = userInfo.username
  tempUserInfo.phone = userInfo.phone
  tempUserInfo.realName = userInfo.realName
  tempUserInfo.idCard = userInfo.idCard
  tempUserInfo.sex = userInfo.sex
  tempUserInfo.avatar = userInfo.avatar
  tempUserInfo.status = userInfo.status
  tempUserInfo.createTime = userInfo.createTime
  
  // 根据sex设置gender
  if (userInfo.sex === 1 || userInfo.sex === "1") {
    tempUserInfo.gender = '男'
  } else if (userInfo.sex === 2 || userInfo.sex === "2") {
    tempUserInfo.gender = '女'
  } else {
    tempUserInfo.gender = ''
  }
  
  console.log('开始编辑，初始化临时数据:', JSON.stringify(tempUserInfo))
  
  // 设置当前头像
  imageUrl.value = userInfo.avatar || ''
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = async () => {
  loading.value = true
  try {
    // 处理性别字段转换
    if (tempUserInfo.gender === '男') {
      tempUserInfo.sex = 1;
    } else if (tempUserInfo.gender === '女') {
      tempUserInfo.sex = 2;
    } else {
      tempUserInfo.sex = 0;
    }
    
    // 准备发送的数据
    const updateData = {
      id: tempUserInfo.id,
      username: tempUserInfo.username,
      phone: tempUserInfo.phone,
      sex: tempUserInfo.sex,
      realName: tempUserInfo.realName,
      idCard: tempUserInfo.idCard
    };
    
    console.log('提交的更新数据:', updateData)
    
    const res = await updateUserInfo(updateData)
    console.log('更新用户信息响应:', res)
    
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      isEditing.value = false
      
      // 重新加载用户信息，确保获取最新数据
      await fetchUserInfo()
    }
  } catch (error) {
    console.error('保存用户信息错误:', error)
    ElMessage.error(error.message || '更新用户信息失败')
  } finally {
    loading.value = false
  }
}

const goToChangePassword = () => {
  router.push('change-password')
}

const showRealNameForm = () => {
  realNameFormVisible.value = true
}

const handleRealNameAuth = async () => {
  loading.value = true
  try {
    // 适配字段名称
    const authData = {
      realName: realNameForm.realName,
      idCard: realNameForm.idCard
    };
    
    const res = await realNameAuth(authData)
    if (res.code === 200) {
      ElMessage.success('实名认证成功')
      realNameFormVisible.value = false
      fetchUserInfo() // 重新获取最新信息
    }
  } catch (error) {
    ElMessage.error(error.message || '实名认证失败')
  } finally {
    loading.value = false
  }
}

// 处理图片URL
const processImageUrl = (url) => {
  if (!url) return ''
  
  try {
    // 如果已经是完整的URL（包含域名），直接返回
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url
    }
    
    // 如果是相对路径，拼接完整URL
    return `${apiBaseUrl}${url}`
  } catch (error) {
    console.error('处理图片URL错误:', error)
    return ''
  }
}

// 头像上传
const handleAvatarUpload = async (response, file) => {
  try {
    console.log('上传响应:', response)
    
    // 检查响应格式
    if (!response) {
      throw new Error('上传失败：服务器无响应')
    }
    
    // 处理成功响应
    if (response.code === 200) {
      // 直接获取data字段的值作为文件URL
      const fileUrl = response.data
      if (!fileUrl) {
        throw new Error('上传成功但未获取到文件URL')
      }
      
      // 处理头像URL
      const processedUrl = processImageUrl(fileUrl)
      console.log('处理后的头像URL:', processedUrl)
      
      // 更新用户头像
      userInfo.avatar = processedUrl
      // 更新显示的头像URL
      imageUrl.value = processedUrl
      // 更新临时数据中的头像
      if (tempUserInfo) {
        tempUserInfo.avatar = processedUrl
      }

      // 调用上传头像的API
      try {
        const updateRes = await updateUserInfo({
          ...tempUserInfo,
          avatar: fileUrl // 使用原始的相对路径
        })
        if (updateRes.code === 200) {
          ElMessage.success('头像上传成功')
        } else {
          throw new Error(updateRes.msg || '更新用户信息失败')
        }
      } catch (error) {
        console.error('更新用户头像失败:', error)
        ElMessage.warning('头像已上传但更新用户信息失败')
      }
      
      return true
    } else {
      // 处理业务错误
      throw new Error(response.message || response.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传头像失败：' + (error.message || '未知错误'))
    return false
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像必须是JPG/PNG/GIF格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB!')
    return false
  }
  return true
}

// 上传出错处理
const handleAvatarError = (err, file) => {
  console.error('头像上传出错:', err)
  ElMessage.error('头像上传失败，请重试')
}
</script>

<template>
  <div class="profile-container">
    <h2 class="page-title">个人信息</h2>

    <!-- 个人信息卡片 -->
    <el-card class="profile-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div>用户资料</div>
          <div v-if="!isEditing">
            <el-button type="primary" @click="startEdit">编辑资料</el-button>
          </div>
        </div>
      </template>

      <!-- 查看模式 -->
      <div v-if="!isEditing" class="view-mode">
        <div class="avatar-container">
          <div v-if="userInfo.avatar">
            <img 
              :src="userInfo.avatar" 
              class="avatar-display" 
              @error="(e) => e.target.src = ''"
              alt="用户头像"
            />
          </div>
          <div v-else class="avatar-placeholder">
            <el-icon :size="40"><UserFilled /></el-icon>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号码">{{ userInfo.phone }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ userInfo.gender || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ userInfo.realName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ userInfo.idCard ? userInfo.idCard.replace(/^(.{6})(?:\d+)(.{4})$/, '$1****$2') : '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ userInfo.createTime || '未知' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 编辑模式 -->
      <el-form v-else :model="tempUserInfo" :rules="rules" label-width="100px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarUpload"
            :on-error="handleAvatarError"
            :before-upload="beforeAvatarUpload"
            :headers="{
              token: token
            }"
            name="file"
            accept="image/jpeg,image/png,image/gif"
          >
            <div v-if="tempUserInfo.avatar || imageUrl" class="avatar-container">
              <img 
                :src="imageUrl || tempUserInfo.avatar" 
                class="avatar" 
                @error="(e) => e.target.src = ''"
              />
            </div>
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            <div class="avatar-tips">点击上传头像，JPG/PNG/GIF格式，不超过2MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="tempUserInfo.username"></el-input>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="tempUserInfo.phone"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="tempUserInfo.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="loading">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 安全验证卡片 -->
    <el-card class="security-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div>账户安全</div>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账户密码">
          <div class="security-item">
            <span>安全性高的密码可以保护您的账户</span>
            <el-button type="primary" size="small" @click="goToChangePassword">修改密码</el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="实名认证">
          <div class="security-item">
            <span>{{ userInfo.realName && userInfo.idCard ? '已完成实名认证' : '未完成实名认证' }}</span>
            <el-tag type="success" v-if="userInfo.realName && userInfo.idCard">已认证</el-tag>
            <el-button type="primary" size="small" v-else @click="showRealNameForm">去认证</el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="手机绑定">
          <div class="security-item">
            <span>{{ userInfo.phone ? userInfo.phone : '未绑定手机号' }}</span>
            <el-tag type="success" v-if="userInfo.phone">已绑定</el-tag>
            <el-button type="primary" size="small" v-else @click="startEdit">去绑定</el-button>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>

  <!-- 实名认证对话框 -->
  <el-dialog
    title="实名认证"
    v-model="realNameFormVisible"
    width="30%"
  >
    <el-form :model="realNameForm" :rules="realNameRules" label-width="100px">
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="realNameForm.realName" placeholder="请输入真实姓名"></el-input>
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="realNameForm.idCard" placeholder="请输入身份证号"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="realNameFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRealNameAuth" :loading="loading">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.profile-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 15px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  position: relative;
  padding-bottom: 10px;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 3px;
  background-color: #409EFF;
}

.profile-card, .security-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
}

.avatar-display {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
  background-color: #f5f7fa;
}

.avatar-uploader {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-container {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
  text-align: center;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}
</style> 