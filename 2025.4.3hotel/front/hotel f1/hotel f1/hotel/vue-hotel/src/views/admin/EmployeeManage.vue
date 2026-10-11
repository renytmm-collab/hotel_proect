<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getEmployeeList, 
  addEmployee, 
  updateEmployee, 
  resetPassword, 
  getEmployeeInfo,
  getRoleList
} from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  empNo: '',
  realName: '',
  roleId: '',
  status: '',
  beginTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

// 员工表单
const employeeForm = reactive({
  id: '',
  empNo: '',
  roleId: '',
  realName: '',
  phone: '',
  sex: '男',
  password: '',
  status: 1
})

// 重置密码表单
const resetPasswordForm = reactive({
  id: '',
  password: ''
})

// 日期范围
const dateRange = ref([])

// 员工列表
const employeeList = ref([])
const total = ref(0)

// 角色列表
const roleList = ref([])

// 对话框控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加员工')
const passwordDialogVisible = ref(false)

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 组件加载时获取数据
onMounted(() => {
  fetchEmployeeList()
  fetchRoleList()
})

// 获取员工列表
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.beginTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    } else {
      searchForm.beginTime = ''
      searchForm.endTime = ''
    }

    const res = await getEmployeeList(searchForm)
    if (res.code === 200) {
      employeeList.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取员工列表错误:', error)
    ElMessage.error('获取员工列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoleList = async () => {
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
  }
}

// 处理搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchEmployeeList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.empNo = ''
  searchForm.realName = ''
  searchForm.roleId = ''
  searchForm.status = ''
  dateRange.value = []
  searchForm.beginTime = ''
  searchForm.endTime = ''
  searchForm.pageNum = 1
  fetchEmployeeList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchEmployeeList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  searchForm.pageSize = val
  searchForm.pageNum = 1
  fetchEmployeeList()
}

// 打开添加对话框
const openAddDialog = () => {
  resetEmployeeForm()
  dialogTitle.value = '添加员工'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = async (row) => {
  resetEmployeeForm()
  dialogTitle.value = '编辑员工'
  
  try {
    const res = await getEmployeeInfo(row.id)
    if (res.code === 200) {
      // 将返回的数据填充到表单
      Object.assign(employeeForm, res.data)
      employeeForm.password = '' // 密码字段不回显
    } else {
      ElMessage.error(res.message || '获取员工信息失败')
    }
  } catch (error) {
    console.error('获取员工信息错误:', error)
    ElMessage.error('获取员工信息失败，请稍后再试')
  }
  
  dialogVisible.value = true
}

// 打开重置密码对话框
const openResetPasswordDialog = (row) => {
  resetPasswordForm.id = row.id
  resetPasswordForm.password = ''
  passwordDialogVisible.value = true
}

// 重置员工表单
const resetEmployeeForm = () => {
  employeeForm.id = ''
  employeeForm.empNo = ''
  employeeForm.roleId = ''
  employeeForm.realName = ''
  employeeForm.phone = ''
  employeeForm.sex = '男'
  employeeForm.password = ''
  employeeForm.status = 1
}

// 提交员工表单
const submitEmployeeForm = async () => {
  if (!employeeForm.empNo) {
    ElMessage.warning('请输入员工编号')
    return
  }
  if (!employeeForm.realName) {
    ElMessage.warning('请输入员工姓名')
    return
  }
  if (!employeeForm.roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  if (!employeeForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  submitting.value = true
  try {
    let res
    if (employeeForm.id) {
      // 编辑员工
      res = await updateEmployee(employeeForm)
    } else {
      // 添加员工
      res = await addEmployee(employeeForm)
    }
    
    if (res.code === 200) {
      ElMessage.success(employeeForm.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchEmployeeList()
    } else {
      ElMessage.error(res.message || (employeeForm.id ? '编辑失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交员工表单错误:', error)
    ElMessage.error(employeeForm.id ? '编辑失败，请稍后再试' : '添加失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 提交重置密码
const submitResetPassword = async () => {
  if (!resetPasswordForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  submitting.value = true
  try {
    const res = await resetPassword(resetPasswordForm)
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '密码重置失败')
    }
  } catch (error) {
    console.error('重置密码错误:', error)
    ElMessage.error('密码重置失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 状态转换为文本
const statusText = (status) => {
  switch (Number(status)) {
    case 0: return '禁用'
    case 1: return '正常'
    default: return '未知'
  }
}

// 获取角色名称
const getRoleName = (roleId) => {
  const role = roleList.value.find(item => item.id === roleId)
  return role ? role.roleName : ''
}
</script>

<template>
  <div class="employee-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">员工管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="员工编号">
          <el-input v-model="searchForm.empNo" placeholder="请输入员工编号" clearable />
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="请选择角色" clearable>
            <el-option
              v-for="item in roleList"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAddDialog">新增员工</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="employeeList"
        border
        style="width: 100%"
      >
        <el-table-column prop="empNo" label="员工编号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="scope">
            {{ getRoleName(scope.row.roleId) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="sex" label="性别" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="openResetPasswordDialog(scope.row)"
            >
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑员工对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        :model="employeeForm"
        label-width="100px"
      >
        <el-form-item label="员工编号" required>
          <el-input 
            v-model="employeeForm.empNo" 
            placeholder="请输入员工编号"
            :disabled="!!employeeForm.id"
          />
        </el-form-item>
        <el-form-item label="员工姓名" required>
          <el-input 
            v-model="employeeForm.realName" 
            placeholder="请输入员工姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input 
            v-model="employeeForm.phone" 
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="employeeForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select 
            v-model="employeeForm.roleId" 
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleList"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!employeeForm.id" label="密码">
          <el-input 
            v-model="employeeForm.password" 
            type="password"
            placeholder="密码为空时将使用默认密码"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="employeeForm.id" label="状态">
          <el-radio-group v-model="employeeForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitEmployeeForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
      destroy-on-close
    >
      <el-form
        :model="resetPasswordForm"
        label-width="80px"
      >
        <el-form-item label="新密码" required>
          <el-input 
            v-model="resetPasswordForm.password" 
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting" 
          @click="submitResetPassword"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.employee-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 