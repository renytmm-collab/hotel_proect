<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, getUserInfo } from '@/api/admin'

// 表格加载状态
const loading = ref(true)

// 账户数据
const accountList = ref([])

// 分页设置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索条件
const searchForm = reactive({
  realName: '',
  phone: '',
  status: ''
})

// 状态选项
const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' }
]

// 详情对话框
const detailDialogVisible = ref(false)
const userDetail = ref({})

// 加载账户数据
const loadAccountData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      ...searchForm,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      accountList.value = res.data.records
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    ElMessage.error('获取用户列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.realName = ''
  searchForm.phone = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadAccountData()
}

// 切换用户状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${statusText}用户 ${row.realName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updateUserStatus({
      id: row.id,
      status: newStatus
    })
    
    if (res.code === 200) {
      ElMessage.success(`${statusText}成功`)
      loadAccountData() // 重新加载数据
    } else {
      ElMessage.error(res.message || `${statusText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改用户状态错误:', error)
      ElMessage.error(`${statusText}失败，请稍后再试`)
    }
  }
}

// 查看用户详情
const showUserDetail = async (row) => {
  try {
    const res = await getUserInfo(row.id)
    if (res.code === 200) {
      userDetail.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情错误:', error)
    ElMessage.error('获取用户详情失败，请稍后再试')
  }
}

// 处理分页变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadAccountData()
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadAccountData()
}

// 页面加载时获取数据
onMounted(() => {
  loadAccountData()
})

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}
</script>

<template>
  <div class="account-container">
    <div class="header">
      <h1 class="text-2xl font-bold mb-4">用户账户管理</h1>
      <div class="breadcrumb">
        <span @click="goToHome">首页</span> / <span>账户管理</span>
      </div>
    </div>
    
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAccountData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card" v-loading="loading">
      <el-table 
        :data="accountList" 
        style="width: 100%" 
        border 
        stripe
        @row-click="showUserDetail"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="sex" label="性别" width="80" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click.stop="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 用户详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="用户详情"
      width="500px"
    >
      <div class="user-detail">
        <div class="detail-item">
          <span class="label">头像：</span>
          <el-avatar :size="60" :src="userDetail.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
        </div>
        <div class="detail-item">
          <span class="label">用户名：</span>
          <span class="value">{{ userDetail.username }}</span>
        </div>
        <div class="detail-item">
          <span class="label">真实姓名：</span>
          <span class="value">{{ userDetail.realName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机号：</span>
          <span class="value">{{ userDetail.phone }}</span>
        </div>
        <div class="detail-item">
          <span class="label">性别：</span>
          <span class="value">{{ userDetail.sex }}</span>
        </div>
        <div class="detail-item">
          <span class="label">身份证号：</span>
          <span class="value">{{ userDetail.idCard }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <el-tag :type="userDetail.status === 1 ? 'success' : 'info'">
            {{ userDetail.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ userDetail.createTime }}</span>
        </div>
        <!-- <div class="detail-item">
          <span class="label">更新时间：</span>
          <span class="value">{{ userDetail.updateTime }}</span>
        </div> -->
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.account-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

.user-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.detail-item .label {
  width: 100px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

/* 添加按钮基础样式 */
:deep(.el-button) {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

:deep(.el-button--warning) {
  background-color: #e6a23c;
  border-color: #e6a23c;
  color: #fff;
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

:deep(.el-button--info) {
  background-color: #909399;
  border-color: #909399;
  color: #fff;
}
</style> 