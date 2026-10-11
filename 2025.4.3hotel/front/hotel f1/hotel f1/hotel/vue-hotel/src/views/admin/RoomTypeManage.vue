<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { 
  getRoomTypeList, 
  addRoomType, 
  updateRoomType, 
  uploadRoomTypeImage,
  getRoomTypeImages,
  deleteRoomTypeImage,
  getRoomTypeInfo
} from '@/api/roomType'

// 搜索表单
const searchForm = reactive({
  typeName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 房型表单
const roomTypeForm = reactive({
  id: '',
  typeName: '',
  price: '',
  area: '',
  bedType: '',
  maxGuests: '',
  description: '',
  facilities: '',
  weight: 100,
  status: 1
})

// 房型列表
const roomTypeList = ref([])
const total = ref(0)

// 对话框控制
const dialogVisible = ref(false)
const dialogTitle = ref('添加房型')
const imageDialogVisible = ref(false)
const currentRoomTypeId = ref(null)

// 图片列表
const imageList = ref([])
const imageUrl = ref('')
const isMainImage = ref(true)

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const imageLoading = ref(false)

// 加载房型列表
const fetchRoomTypeList = async () => {
  loading.value = true
  try {
    const res = await getRoomTypeList(searchForm)
    if (res.code === 200) {
      roomTypeList.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取房型列表失败')
    }
  } catch (error) {
    console.error('获取房型列表错误:', error)
    ElMessage.error('获取房型列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchRoomTypeList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.typeName = ''
  searchForm.status = ''
  searchForm.pageNum = 1
  fetchRoomTypeList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  searchForm.pageNum = val
  fetchRoomTypeList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  searchForm.pageSize = val
  searchForm.pageNum = 1
  fetchRoomTypeList()
}

// 打开添加对话框
const openAddDialog = () => {
  resetRoomTypeForm()
  dialogTitle.value = '添加房型'
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = async (row) => {
  resetRoomTypeForm()
  dialogTitle.value = '编辑房型'
  
  try {
    const res = await getRoomTypeInfo(row.id)
    if (res.code === 200) {
      // 将返回的数据填充到表单
      Object.assign(roomTypeForm, res.data)
    } else {
      ElMessage.error(res.message || '获取房型信息失败')
    }
  } catch (error) {
    console.error('获取房型信息错误:', error)
    ElMessage.error('获取房型信息失败，请稍后再试')
  }
  
  dialogVisible.value = true
}

// 重置房型表单
const resetRoomTypeForm = () => {
  roomTypeForm.id = ''
  roomTypeForm.typeName = ''
  roomTypeForm.price = ''
  roomTypeForm.area = ''
  roomTypeForm.bedType = ''
  roomTypeForm.maxGuests = ''
  roomTypeForm.description = ''
  roomTypeForm.facilities = ''
  roomTypeForm.weight = 100
  roomTypeForm.status = 1
}

// 提交房型表单
const submitRoomTypeForm = async () => {
  if (!roomTypeForm.typeName) {
    ElMessage.warning('请输入房型名称')
    return
  }
  if (!roomTypeForm.price) {
    ElMessage.warning('请输入房型价格')
    return
  }
  
  submitting.value = true
  try {
    let res
    if (roomTypeForm.id) {
      // 编辑房型
      res = await updateRoomType(roomTypeForm)
    } else {
      // 添加房型
      res = await addRoomType(roomTypeForm)
    }
    
    if (res.code === 200) {
      ElMessage.success(roomTypeForm.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      fetchRoomTypeList()
    } else {
      ElMessage.error(res.message || (roomTypeForm.id ? '编辑失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交房型表单错误:', error)
    ElMessage.error(roomTypeForm.id ? '编辑失败，请稍后再试' : '添加失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

// 打开图片管理对话框
const openImageDialog = async (roomTypeId) => {
  currentRoomTypeId.value = roomTypeId
  imageUrl.value = ''
  isMainImage.value = false
  
  try {
    const res = await getRoomTypeImages(roomTypeId)
    if (res.code === 200) {
      imageList.value = res.data
    } else {
      ElMessage.error(res.message || '获取房型图片失败')
    }
  } catch (error) {
    console.error('获取房型图片错误:', error)
    ElMessage.error('获取房型图片失败，请稍后再试')
  }
  
  imageDialogVisible.value = true
}

// 上传图片
const uploadImage = async () => {
  if (!imageUrl.value) {
    ElMessage.warning('请输入图片URL')
    return
  }
  
  imageLoading.value = true
  try {
    const data = {
      roomTypeId: currentRoomTypeId.value,
      isMain: isMainImage.value ? 1 : 0,
      imageUrl: imageUrl.value
    }
    
    const res = await uploadRoomTypeImage(data)
    if (res.code === 200) {
      ElMessage.success('图片上传成功')
      imageUrl.value = ''
      
      // 重新获取图片列表
      const imageRes = await getRoomTypeImages(currentRoomTypeId.value)
      if (imageRes.code === 200) {
        imageList.value = imageRes.data
      }
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch (error) {
    console.error('上传图片错误:', error)
    ElMessage.error('图片上传失败，请稍后再试')
  } finally {
    imageLoading.value = false
  }
}

// 删除图片
const removeImage = async (id) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRoomTypeImage(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        
        // 更新图片列表
        imageList.value = imageList.value.filter(item => item.id !== id)
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除图片错误:', error)
      ElMessage.error('删除失败，请稍后再试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 设为主图
const setMainImage = async (image) => {
  if (image.isMain === 1) {
    ElMessage.info('该图片已经是主图')
    return
  }
  
  try {
    const data = {
      roomTypeId: currentRoomTypeId.value,
      isMain: 1,
      imageUrl: image.imageUrl
    }
    
    const res = await uploadRoomTypeImage(data)
    if (res.code === 200) {
      ElMessage.success('设置主图成功')
      
      // 更新本地图片列表
      imageList.value = imageList.value.map(item => {
        if (item.id === image.id) {
          return { ...item, isMain: 1 }
        } else {
          return { ...item, isMain: 0 }
        }
      })
    } else {
      ElMessage.error(res.message || '设置主图失败')
    }
  } catch (error) {
    console.error('设置主图错误:', error)
    ElMessage.error('设置主图失败，请稍后再试')
  }
}

// 格式化状态
const statusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

// 组件加载时获取数据
onMounted(() => {
  fetchRoomTypeList()
})
</script>

<template>
  <div class="roomtype-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">房型管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="房型名称">
          <el-input v-model="searchForm.typeName" placeholder="请输入房型名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAddDialog">新增房型</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="roomTypeList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="typeName" label="房型名称" width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            {{ '¥' + row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积" width="100">
          <template #default="{ row }">
            {{ row.area + ' m²' }}
          </template>
        </el-table-column>
        <el-table-column prop="bedType" label="床型" width="120" />
        <el-table-column prop="maxGuests" label="可住人数" width="100" />
        <el-table-column prop="facilities" label="设施" show-overflow-tooltip />
        <el-table-column prop="weight" label="权重" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="openImageDialog(scope.row.id)"
            >
              图片管理
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

    <!-- 添加/编辑房型对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        :model="roomTypeForm"
        label-width="100px"
      >
        <el-form-item label="房型名称" required>
          <el-input 
            v-model="roomTypeForm.typeName" 
            placeholder="请输入房型名称"
          />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number 
            v-model="roomTypeForm.price" 
            :precision="2" 
            :step="10" 
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面积">
          <el-input-number 
            v-model="roomTypeForm.area" 
            :precision="0" 
            :step="1" 
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="床型">
          <el-input 
            v-model="roomTypeForm.bedType" 
            placeholder="请输入床型"
          />
        </el-form-item>
        <el-form-item label="可住人数">
          <el-input-number 
            v-model="roomTypeForm.maxGuests" 
            :precision="0" 
            :step="1" 
            :min="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="设施">
          <el-input 
            v-model="roomTypeForm.facilities" 
            placeholder="请输入设施，多个设施用逗号分隔"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="roomTypeForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入房型描述"
          />
        </el-form-item>
        <el-form-item label="权重">
          <el-input-number 
            v-model="roomTypeForm.weight" 
            :precision="0" 
            :step="10" 
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roomTypeForm.status">
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
          @click="submitRoomTypeForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片管理对话框 -->
    <el-dialog
      v-model="imageDialogVisible"
      title="房型图片管理"
      width="800px"
    >
      <div>
        <el-form inline>
          <el-form-item label="图片URL">
            <el-input 
              v-model="imageUrl" 
              placeholder="请输入图片URL" 
              style="width: 400px;"
            />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="isMainImage">设为主图</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="imageLoading" 
              @click="uploadImage"
            >
              上传图片
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider>图片列表</el-divider>

        <div class="image-list">
          <div v-if="imageList.length === 0" class="no-data">
            暂无图片，请上传
          </div>
          <el-card 
            v-for="image in imageList" 
            :key="image.id"
            class="image-card"
            :body-style="{ padding: '0px' }"
          >
            <div class="image-container">
              <el-image 
                :src="image.imageUrl" 
                fit="contain"
                class="el-image"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-info">
              <div>
                <el-tag v-if="image.isMain === 1" type="success">主图</el-tag>
              </div>
              <div class="image-actions">
                <el-button 
                  v-if="image.isMain !== 1"
                  type="primary" 
                  size="small"
                  @click="setMainImage(image)"
                >
                  设为主图
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="removeImage(image.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.roomtype-container {
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

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.image-card {
  width: 220px;
  overflow: hidden;
}

.image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.el-image {
  width: 100%;
  height: 100%;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  background-color: #f5f7fa;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.image-info {
  padding: 10px;
}

.image-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-around;
}

.no-data {
  width: 100%;
  text-align: center;
  color: #999;
  padding: 30px;
}
</style> 