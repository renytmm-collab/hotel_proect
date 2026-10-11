<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEmployeeList, getEmployeeInfo, updateEmployee, addEmployee, getRoleOptions } from '@/api/admin'

// 搜索表单
const searchForm = ref({
  empNo: '',
  realName: '',
  roleId: '',
  status: '',
  beginTime: '',
  endTime: ''
})

// 员工表单
const staffForm = ref({
  id: null,
  empNo: '',
  realName: '',
  phone: '',
  roleId: '',
  status: 1,
  password: '',
  sex: '男'
})

// 表格数据
const staffList = ref([])
const roleList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗控制
const showDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const selectedStaff = ref({})

// 表单验证规则
const formRules = {
  empNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择职位', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  password: [
    { required: false, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 表单引用
const staffFormRef = ref(null)

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑员工' : '添加员工'
})

// 状态文本转换
const statusText = (status) => {
  const statusMap = {
    1: '正常',
    0: '禁用'
  }
  return statusMap[status] || status
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const res = await getRoleOptions()
    if (res.code === 200) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

// 获取员工列表
const fetchStaff = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm.value
    }
    const res = await getEmployeeList(params)
    if (res.code === 200) {
      staffList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

// 获取员工详细信息
const fetchStaffDetail = async (id) => {
  try {
    const res = await getEmployeeInfo(id)
    if (res.code === 200) {
      selectedStaff.value = res.data
      showDetailDialog.value = true
    } else {
      ElMessage.error(res.message || '获取员工信息失败')
    }
  } catch (error) {
    console.error('获取员工信息失败:', error)
    ElMessage.error('获取员工信息失败')
  }
}

// 格式化日期为 yyyy-MM-dd
function formatDate(date) {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');  // 月份从 0 开始，所以需要 +1
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
// 搜索员工
const searchStaff = () => {
  currentPage.value = 1
  searchForm.value.beginTime = searchForm.value.beginTime ? formatDate(searchForm.value.beginTime) : '',
  searchForm.value.endTime = searchForm.value.endTime ? formatDate(searchForm.value.endTime) : ''
  fetchStaff()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    empNo: '',
    realName: '',
    roleId: '',
    status: '',
    beginTime: '',
    endTime: ''
  }
  currentPage.value = 1
  fetchStaff()
}

// 打开添加弹窗
const openAddDialog = () => {
  isEdit.value = false
  staffForm.value = {
    id: null,
    empNo: '',
    realName: '',
    phone: '',
    roleId: roleList.value.length > 0 ? roleList.value[0].id : '',
    status: 1,
    password: '',
    sex: '男'
  }
  showDialog.value = true
}

// 打开编辑弹窗
const openEditDialog = (staff) => {
  isEdit.value = true
  staffForm.value = {
    id: staff.id,
    empNo: staff.empNo,
    realName: staff.realName,
    phone: staff.phone,
    roleId: staff.roleId,
    status: staff.status,
    password: '',
    sex: staff.sex
  }
  showDialog.value = true
}

// 提交员工表单
const submitStaffForm = async () => {
  if (!staffFormRef.value) return
  
  try {
    await staffFormRef.value.validate()
    
    if (isEdit.value) {
      const res = await updateEmployee(staffForm.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        showDialog.value = false
        fetchStaff()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await addEmployee(staffForm.value)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        showDialog.value = false
        fetchStaff()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
  } catch (error) {
    if (error) {
      ElMessage.error('请检查表单填写是否正确')
    } else {
      console.error('保存员工失败:', error)
      ElMessage.error('保存员工失败')
    }
  }
}

// 返回首页
const goToHome = () => {
  window.location.href = '/admin'
}

// 生命周期钩子
onMounted(() => {
  fetchRoles()
  fetchStaff()
})
</script>

<template>
  <div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">员工管理</h1>
    <div class="breadcrumb">
      <span @click="goToHome">首页</span> / <span>员工管理</span>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="bg-white p-4 mb-4 rounded shadow">
      <div class="flex flex-wrap gap-4 mb-4">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">工号</label>
          <input v-model="searchForm.empNo" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入工号" />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
          <input v-model="searchForm.realName" type="text" class="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="请输入姓名" />
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">职位</label>
          <select v-model="searchForm.roleId" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option v-for="role in roleList" :key="role.id" :value="role.id">
              {{ role.roleName }}
            </option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">状态</label>
          <select v-model="searchForm.status" class="w-full px-3 py-2 border border-gray-300 rounded-md">
            <option value="">全部</option>
            <option value="1">正常</option>
            <option value="0">禁用</option>
          </select>
        </div>
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm font-medium text-gray-700 mb-1">入职时间范围</label>
          <div class="flex gap-2">
            <el-date-picker
              v-model="searchForm.beginTime"
              type="datetime"
              placeholder="开始时间"
              class="w-1/2"
            />
            <el-date-picker
              v-model="searchForm.endTime"
              type="datetime"
              placeholder="结束时间"
              class="w-1/2"
            />
          </div>
        </div>
        <div class="flex-1 min-w-[200px] flex items-end">
          <button @click="searchStaff" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            搜索
          </button>
          <button @click="resetSearch" class="ml-2 px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
            重置
          </button>
          <button @click="openAddDialog" class="ml-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">
            添加员工
          </button>
        </div>
      </div>
    </div>

    <!-- 员工表格 -->
    <div class="bg-white p-4 rounded shadow overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              头像
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              工号
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              用户名
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              姓名
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              联系方式
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              职位
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              状态
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              性别
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              操作
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="staff in staffList" :key="staff.id" @click="fetchStaffDetail(staff.id)" class="cursor-pointer hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <img v-if="staff.avatar" :src="staff.avatar" class="h-10 w-10 rounded-full" />
              <div v-else class="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                <span class="text-gray-500">{{ staff.realName?.charAt(0) }}</span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">{{ staff.empNo }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ staff.username }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ staff.realName }}</td>
            <td class="px-6 py-4 whitespace-nowrap">{{ staff.phone }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              {{ roleList.find(role => role.id === staff.roleId)?.roleName }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="{
                'px-2 py-1 text-xs rounded-full': true,
                'bg-green-100 text-green-800': staff.status === 1,
                'bg-gray-100 text-gray-800': staff.status === 0
              }">
                {{ statusText(staff.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">{{ staff.sex }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button @click.stop="openEditDialog(staff)" class="text-indigo-600 hover:text-indigo-900">
                编辑
              </button>
            </td>
          </tr>
          <tr v-if="!staffList || staffList.length === 0">
            <td colspan="9" class="px-6 py-4 text-center">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="flex flex-col items-center mt-4">
        <div class="mb-2">共 {{ total }} 条</div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchStaff"
          @current-change="fetchStaff"
        />
      </div>
    </div>

    <!-- 添加/编辑员工弹窗 -->
    <div v-if="showDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">{{ dialogTitle }}</h2>
        <el-form
          ref="staffFormRef"
          :model="staffForm"
          :rules="formRules"
          @submit.prevent="submitStaffForm"
        >
          <el-form-item label="工号" prop="empNo" v-if="!isEdit">
            <el-input v-model="staffForm.empNo" />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="staffForm.realName" />
          </el-form-item>
          <el-form-item label="联系方式" prop="phone">
            <el-input v-model="staffForm.phone" />
          </el-form-item>
          <el-form-item label="职位" prop="roleId">
            <el-select v-model="staffForm.roleId" class="w-full">
              <el-option
                v-for="role in roleList"
                :key="role.id"
                :label="role.roleName"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status" v-if="isEdit">
            <el-select v-model="staffForm.status" class="w-full">
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="isEdit">
            <el-input v-model="staffForm.password" type="password" />
          </el-form-item>
          <el-form-item label="性别" prop="sex" v-if="!isEdit">
            <el-select v-model="staffForm.sex" class="w-full">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <div class="flex justify-end space-x-2">
            <el-button @click="showDialog = false">取消</el-button>
            <el-button type="primary" native-type="submit">确认</el-button>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 员工详情弹窗 -->
    <div v-if="showDetailDialog" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="absolute inset-0 bg-black opacity-50"></div>
      <div class="bg-white rounded-lg p-6 w-full max-w-md z-10">
        <h2 class="text-xl font-bold mb-4">员工详情</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-center mb-4">
            <img v-if="selectedStaff.avatar" :src="selectedStaff.avatar" class="h-20 w-20 rounded-full" />
            <div v-else class="h-20 w-20 rounded-full bg-gray-200 flex items-center justify-center">
              <span class="text-gray-500 text-xl">{{ selectedStaff.realName?.charAt(0) }}</span>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">工号</label>
              <p class="mt-1">{{ selectedStaff.empNo }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">用户名</label>
              <p class="mt-1">{{ selectedStaff.username }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">姓名</label>
              <p class="mt-1">{{ selectedStaff.realName }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">联系方式</label>
              <p class="mt-1">{{ selectedStaff.phone }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">职位</label>
              <p class="mt-1">{{ selectedStaff.roleName }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">状态</label>
              <p class="mt-1">
                <span :class="{
                  'px-2 py-1 text-xs rounded-full': true,
                  'bg-green-100 text-green-800': selectedStaff.status === 1,
                  'bg-gray-100 text-gray-800': selectedStaff.status === 0
                }">
                  {{ statusText(selectedStaff.status) }}
                </span>
              </p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">入职时间</label>
              <p class="mt-1">{{ selectedStaff.createTime }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">性别</label>
              <p class="mt-1">{{ selectedStaff.sex }}</p>
            </div>
          </div>
          <div class="flex justify-end">
            <button @click="showDetailDialog = false" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.el-pagination {
  justify-content: center;
}

.el-button--primary {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
}

.el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
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