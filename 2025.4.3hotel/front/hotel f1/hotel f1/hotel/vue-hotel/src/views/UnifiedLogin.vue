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
  // 若是从住客受保护页面（个人中心/我的订单/我的评价/我的收藏）或门户登录按钮跳来，
  // 直接展示住客登录表单，避免被后台管理员登录态拦截
  if (route.query.tab === 'user') {
    activeTab.value = 'user'
    const userToken = localStorage.getItem('token')
    const userData = localStorage.getItem('loginUser')
    if (!userToken || !userData) {
      isAlreadyLoggedIn.value = false
      return
    }
  }

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

// 切换登录方式（保留已有另一端会话，方便答辩演示时平滑切换）
const handleTabChange = (tab) => {
  activeTab.value = tab
  isAlreadyLoggedIn.value = false
}

// 一键填充测试演示账号
const fillDemoAccount = (type) => {
  if (type === 'user') {
    activeTab.value = 'user'
    userLoginForm.phone = '13800138001'
    userLoginForm.password = '123456'
  } else if (type === 'admin') {
    activeTab.value = 'staff'
    staffLoginForm.empNo = 'admin'
    staffLoginForm.password = '123456'
  } else if (type === 'staff') {
    activeTab.value = 'staff'
    staffLoginForm.empNo = 'E001'
    staffLoginForm.password = '123456'
  }
}
</script>

<template>
  <div class="unified-login-container">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>

    <div class="login-card">
      <!-- 左侧品牌视觉展示区 -->
      <div class="brand-panel">
        <div class="brand-overlay"></div>
        <div class="brand-content">
          <div class="brand-logo" @click="router.push('/')">
            <span class="logo-mark">逸</span>
            <div class="logo-text">
              <div class="logo-cn">逸云精品酒店</div>
              <div class="logo-en">YIYUN HOTEL SYSTEM</div>
            </div>
          </div>

          <div class="brand-hero">
            <h2>城市中心<br />智慧居停新体验</h2>
            <p>融合线上自助预订、前台高效接待、实时经营数据大屏与 RAG 智能客服助手。</p>
            <div class="brand-features">
              <span class="feat-pill">✦ 经营数据大屏</span>
              <span class="feat-pill">✦ RAG 知识库</span>
              <span class="feat-pill">✦ AI 智能客服</span>
            </div>
          </div>

          <div class="brand-footer">
            <span class="back-home" @click="router.push('/')">← 返回首页浏览房型</span>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-panel">
        <div class="form-header">
          <h1>欢迎登录</h1>
          <p>请选择您的身份并输入账号密码</p>
        </div>

        <!-- 已登录提示 -->
        <div v-if="isAlreadyLoggedIn" class="already-logged-in">
          <div class="logged-badge">{{ currentRole }}</div>
          <p class="logged-title">当前已登录账号：<span class="highlight">{{ currentUsername }}</span></p>
          <p class="logged-sub">您可以直接进入系统，或退出后切换其他角色登录</p>
          <div class="action-buttons">
            <el-button type="primary" size="large" round @click="enterSystem">进入系统</el-button>
            <el-button type="danger" plain size="large" round @click="handleLogout">退出登录</el-button>
          </div>
        </div>

        <!-- 登录表单 -->
        <div v-else class="login-form-wrapper">
          <!-- 分段角色切换器 -->
          <div class="role-switcher">
            <button
              type="button"
              class="role-btn"
              :class="{ active: activeTab === 'user' }"
              @click="handleTabChange('user')"
            >
              住客登录
            </button>
            <button
              type="button"
              class="role-btn"
              :class="{ active: activeTab === 'staff' }"
              @click="handleTabChange('staff')"
            >
              员工 / 管理员
            </button>
          </div>

          <!-- 住客登录 -->
          <el-form
            v-if="activeTab === 'user'"
            ref="userFormRef"
            :model="userLoginForm"
            :rules="userRules"
            label-position="top"
            class="styled-form"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="userLoginForm.phone"
                size="large"
                placeholder="请输入 11 位手机号"
                prefix-icon="Phone"
                clearable
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="userLoginForm.password"
                size="large"
                type="password"
                placeholder="请输入登录密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleUserLogin"
              />
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleUserLogin"
            >
              登录住客端
            </el-button>

            <div class="quick-demo">
              <span>演示快捷填充：</span>
              <button type="button" class="demo-chip" @click="fillDemoAccount('user')">住客（13800138001）</button>
            </div>

            <div class="form-links">
              <span class="link-muted">还没有账号？</span>
              <el-button text type="primary" @click="goToRegister">立即免费注册 →</el-button>
            </div>
          </el-form>

          <!-- 员工 / 管理员登录 -->
          <el-form
            v-else
            ref="staffFormRef"
            :model="staffLoginForm"
            :rules="staffRules"
            label-position="top"
            class="styled-form"
          >
            <el-form-item label="员工编号 / 管理账号" prop="empNo">
              <el-input
                v-model="staffLoginForm.empNo"
                size="large"
                placeholder="例如：admin 或 E001"
                prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="staffLoginForm.password"
                size="large"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleStaffLogin"
              />
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleStaffLogin"
            >
              登录工作台
            </el-button>

            <div class="quick-demo">
              <span>演示快捷填充：</span>
              <button type="button" class="demo-chip" @click="fillDemoAccount('admin')">管理员（admin）</button>
              <button type="button" class="demo-chip" @click="fillDemoAccount('staff')">前台（E001）</button>
            </div>
          </el-form>
        </div>

        <div class="login-footer">
          © {{ new Date().getFullYear() }} 逸云酒店管理系统 · 安全加密登录
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.unified-login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: radial-gradient(circle at 20% 20%, #172554 0%, #0f172a 55%, #090f1d 100%);
  padding: 24px;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  opacity: 0.45;
}

.orb-1 {
  width: 420px;
  height: 420px;
  background: #0f766e;
  top: -120px;
  left: -80px;
}

.orb-2 {
  width: 460px;
  height: 460px;
  background: #2563eb;
  bottom: -140px;
  right: -100px;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 920px;
  min-height: 560px;
  display: grid;
  grid-template-columns: 1.02fr 1fr;
  background: #ffffff;
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.42);
}

/* 左侧实景品牌面板 */
.brand-panel {
  position: relative;
  background-image: url('/upload/room-deluxe.jpg');
  background-size: cover;
  background-position: center;
  color: #ffffff;
  display: flex;
}

.brand-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, rgba(15, 23, 42, 0.84) 0%, rgba(15, 118, 110, 0.68) 58%, rgba(15, 23, 42, 0.90) 100%);
}

.brand-content {
  position: relative;
  z-index: 1;
  padding: 36px 34px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 100%;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #14b8a6, #3b82f6);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 8px 18px rgba(20, 184, 166, 0.35);
}

.logo-cn {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
}

.logo-en {
  font-size: 10px;
  color: #94a3b8;
  letter-spacing: 1.6px;
}

.brand-hero h2 {
  font-size: 30px;
  line-height: 1.35;
  margin: 0 0 14px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-hero p {
  margin: 0 0 22px;
  font-size: 13.5px;
  line-height: 1.75;
  color: rgba(241, 245, 249, 0.88);
}

.brand-features {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feat-pill {
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 11.5px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(6px);
}

.back-home {
  font-size: 13px;
  color: #cbd5e1;
  cursor: pointer;
  transition: color 0.2s;
}

.back-home:hover {
  color: #5eead4;
}

/* 右侧表单面板 */
.form-panel {
  padding: 40px 42px 28px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #ffffff;
}

.form-header h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}

.form-header p {
  margin: 0 0 24px;
  font-size: 13.5px;
  color: #64748b;
}

.role-switcher {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 22px;
}

.role-btn {
  border: none;
  background: transparent;
  padding: 9px 12px;
  border-radius: 9px;
  font-size: 13.5px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.role-btn.active {
  background: #ffffff;
  color: #0f766e;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
}

.styled-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  padding-bottom: 6px;
}

.styled-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
  padding: 4px 12px;
}

.styled-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #0f766e inset !important;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  border-radius: 10px !important;
  font-size: 15px;
  font-weight: 600;
  border: none !important;
  background: linear-gradient(135deg, #0f766e 0%, #2563eb 100%) !important;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.25);
  transition: transform 0.18s, box-shadow 0.18s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 22px rgba(37, 99, 235, 0.34);
}

.quick-demo {
  margin-top: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}

.demo-chip {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  font-size: 11.5px;
  padding: 3px 10px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}

.demo-chip:hover {
  border-color: #0f766e;
  color: #0f766e;
  background: #f0fdfa;
}

.form-links {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.link-muted {
  color: #64748b;
}

.already-logged-in {
  text-align: center;
  padding: 24px 0;
}

.logged-badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  background: #f0fdfa;
  color: #0f766e;
  font-size: 12.5px;
  font-weight: 600;
  margin-bottom: 14px;
}

.logged-title {
  font-size: 16px;
  color: #0f172a;
  margin: 0 0 6px;
}

.logged-sub {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 24px;
}

.highlight {
  color: #0f766e;
  font-weight: 700;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #94a3b8;
  font-size: 11.5px;
}

@media (max-width: 780px) {
  .login-card {
    grid-template-columns: 1fr;
    max-width: 440px;
  }
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 32px 26px 24px;
  }
}
</style> 