<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'

const userInfo = reactive({
  username: '',
  phone: '',
  avatar: ''
})

const loading = ref(false)
const avatarLoading = ref(false)

// 获取用户信息
const getUserInfoData = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      // 只获取需要的字段
      const { username, phone, avatar } = res.data
      Object.assign(userInfo, { username, phone, avatar })
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 更新用户信息
const handleUpdate = async () => {
  try {
    loading.value = true
    // 只更新需要的字段
    const updateData = {
      username: userInfo.username,
      phone: userInfo.phone,
      avatar: userInfo.avatar
    }
    const res = await updateUserInfo(updateData)
    if (res.code === 200) {
      ElMessage.success('更新成功')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理头像上传
const handleAvatarUpload = async (file) => {
  try {
    avatarLoading.value = true
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      userInfo.avatar = res.data
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
  } finally {
    avatarLoading.value = false
  }
}

// 初始化
getUserInfoData()
</script>

<template>
  <div class="user-info-container">
    <el-card class="user-info-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
        </div>
      </template>
      
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="handleAvatarUpload"
          accept="image/*"
        >
          <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="avatar-tip">点击上传头像</div>
      </div>

      <el-form :model="userInfo" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userInfo.phone" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUpdate">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.user-info-container {
  padding: 20px;
}

.user-info-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  text-align: center;
}

.avatar-upload {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  display: inline-block;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.avatar-tip {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}
</style> 