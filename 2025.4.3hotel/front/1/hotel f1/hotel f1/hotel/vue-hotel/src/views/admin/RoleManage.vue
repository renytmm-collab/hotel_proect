<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole } from '@/api/admin'

// 表格加载状态
const loading = ref(false)

// 角色数据
const roleList = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加角色')
const isEdit = ref(false)
const submitting = ref(false)

// 表单数据
const roleForm = reactive({
  id: '',
  roleName: '',
  description: '',
  status: 1
})

// 加载角色数据
const fetchRoleList = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    if (res.code === 200) {
      roleList.value = res.data
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

// 添加角色
const openAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '添加角色'
  
  // 重置表单
  roleForm.id = ''
  roleForm.roleName = ''
  roleForm.description = ''
  roleForm.status = 1
  
  dialogVisible.value = true
}

// 编辑角色
const openEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  
  // 填充表单
  roleForm.id = row.id
  roleForm.roleName = row.roleName
  roleForm.description = row.description
  roleForm.status = row.status
  
  dialogVisible.value = true
}

// 提交角色表单
const submitRoleForm = async () => {
  if (!roleForm.roleName) {
    ElMessage.warning('请输入角色名称')
    return
  }
  
  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      // 编辑角色
      res = await updateRole(roleForm)
    } else {
      // 添加角色
      res = await addRole(roleForm)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchRoleList()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '编辑失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交角色表单错误:', error)
    ElMessage.error(isEdit.value ? '编辑失败，请稍后再试' : '添加失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 格式化状态
const statusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

// 组件加载时获取数据
onMounted(() => {
  fetchRoleList()
})
</script>

<template>
  <div class="role-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">角色管理</span>
          <el-button type="primary" @click="openAddDialog">添加角色</el-button>
        </div>
      </template>

      <!-- 角色列表 -->
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="roleForm"
        label-width="100px"
      >
        <el-form-item label="角色名称" required>
          <el-input 
            v-model="roleForm.roleName" 
            placeholder="请输入角色名称"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="roleForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitRoleForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}
</style> 