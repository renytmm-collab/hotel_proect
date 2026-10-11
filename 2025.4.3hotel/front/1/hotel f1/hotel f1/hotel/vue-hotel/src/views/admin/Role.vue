<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, getRoleOptions } from '@/api/admin'

// 表格加载状态
const loading = ref(true)

// 角色数据
const roleList = ref([])

// 分页设置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加角色')
const dialogType = ref('add') // add, edit

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: '',
  roleName: '',
  description: '',
  status: 1
})

// 表单规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ]
}

// 状态选项
const statusOptions = [
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

// 角色选项列表
const roleOptions = ref([])

// 加载角色数据
const loadRoleData = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roleList.value = res.data.records
      pagination.total = res.data.total
    } else {
      ElMessage.error(res.message || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表错误:', error)
    ElMessage.error('获取角色列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 加载角色选项
const loadRoleOptions = async () => {
  try {
    const res = await getRoleOptions()
    if (res.code === 200) {
      roleOptions.value = res.data
    }
  } catch (error) {
    console.error('获取角色选项错误:', error)
    ElMessage.error('获取角色选项失败')
  }
}

// 添加角色
const addRoleBu = () => {
  dialogTitle.value = '添加角色'
  dialogType.value = 'add'

  // 重置表单
  formData.id = ''
  formData.roleName = ''
  formData.description = ''
  formData.status = 1

  // 清除表单验证
  if (formRef.value) {
    formRef.value.resetFields()
  }

  dialogVisible.value = true
}

// 编辑角色
const editRole = (row) => {
  dialogTitle.value = '编辑角色'
  dialogType.value = 'edit'

  // 设置表单数据
  formData.id = row.id
  formData.roleName = row.roleName
  formData.description = row.description
  formData.status = row.status

  dialogVisible.value = true
}

// 切换角色状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${statusText}角色 ${row.roleName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await updateRole({
      id: row.id,
      roleName: row.roleName,
      description: row.description,
      status: newStatus
    })

    if (res.code === 200) {
      ElMessage.success(`${statusText}成功`)
      loadRoleData() // 重新加载数据
    } else {
      ElMessage.error(res.message || `${statusText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改角色状态错误:', error)
      ElMessage.error(`${statusText}失败，请稍后再试`)
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // 添加角色
          const res = await addRole({
            roleName: formData.roleName,
            description: formData.description
          })

          if (res.code === 200) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            loadRoleData() // 重新加载数据
          } else {
            ElMessage.error(res.message || '添加失败')
          }
        } else {
          // 编辑角色
          const res = await updateRole({
            id: formData.id,
            roleName: formData.roleName,
            description: formData.description,
            status: formData.status
          })

          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadRoleData() // 重新加载数据
          } else {
            ElMessage.error(res.message || '更新失败')
          }
        }
      } catch (error) {
        console.error('提交表单错误:', error)
        ElMessage.error('操作失败，请稍后再试')
      }
    } else {
      return false
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadRoleData()
  loadRoleOptions()
})

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}
</script>

<template>
  <div class="role-container">
    <div class="header">
      <h1 class="text-2xl font-bold mb-4">角色管理</h1>
      <div class="breadcrumb">
        <span @click="goToHome">首页</span> / <span>角色管理</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="addRoleBu">
        <el-icon>
          <Plus />
        </el-icon> 添加角色
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card v-loading="loading">
      <el-table :data="roleList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editRole(scope.row)">编辑</el-button>
            <el-button size="small" :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <!-- <el-select 
            v-model="formData.roleName" 
            placeholder="请选择角色名称"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.roleName"
            />
          </el-select> -->
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" style="width: 100%" />

        </el-form-item>

        <el-form-item label="角色描述">
          <el-input v-model="formData.description" type="textarea" rows="3" placeholder="请输入角色描述" />
        </el-form-item>

        <el-form-item label="状态" v-if="dialogType === 'edit'">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.action-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.tree-container {
  height: 350px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  width: 100%;
  padding: 10px;
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

/* 添加MessageBox按钮样式 */
:deep(.el-message-box__btns .el-button) {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

:deep(.el-message-box__btns .el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

:deep(.el-message-box__btns .el-button--default) {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

/* 添加MessageBox按钮hover和active状态样式 */
:deep(.el-message-box__btns .el-button--primary:hover),
:deep(.el-message-box__btns .el-button--primary:focus) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

:deep(.el-message-box__btns .el-button--primary:active) {
  background-color: #3a8ee6;
  border-color: #3a8ee6;
  color: #fff;
}

:deep(.el-message-box__btns .el-button--default:hover),
:deep(.el-message-box__btns .el-button--default:focus),
:deep(.el-message-box__btns .el-button--default:active) {
  background-color: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #606266 !important;
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
</style>