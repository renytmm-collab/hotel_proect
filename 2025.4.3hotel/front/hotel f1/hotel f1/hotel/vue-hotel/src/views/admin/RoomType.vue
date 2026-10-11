<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    getRoomTypeList,
    getRoomTypeInfo,
    getRoomTypeImages,
    updateRoomType,
    addRoomType,
    deleteRoomTypeImage,
    getRoomTypeNames,
    uploadRoomTypeImage
  } from '@/api/roomType'
  import { uploadFile } from '@/api/common'
  
  // 表格加载状态
  const loading = ref(true)
  
  // 房型数据
  const roomTypeList = ref([])
  
  // 分页设置
  const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
  })
  
  // 搜索条件
  const searchForm = reactive({
    typeName: '',
    status: ''
  })
  
  // 对话框状态
  const dialogVisible = ref(false)
  const dialogTitle = ref('添加房型')
  const dialogType = ref('add') // add, edit
  
  // 表单引用
  const formRef = ref(null)
  
  // 表单数据
  const formData = reactive({
    id: '',
    typeName: '',
    price: 0,
    area: 0,
    bedType: '',
    maxGuests: 2,
    description: '',
    facilities: '',
    weight: 100,
    status: 1
  })
  
  // 房型详情数据
  const roomTypeDetail = ref(null)
  const roomTypeImages = ref([])
  const mainImage = ref(null)
  const subImages = ref([])
  
  // 房型名称选项
  const roomTypeOptions = ref([])
  
  // 表单规则
  const rules = {
    typeName: [
      { required: true, message: '请输入房型名称', trigger: 'blur' }
    ],
    price: [
      { required: true, message: '请输入价格', trigger: 'blur' },
      { type: 'number', min: 0, message: '价格必须大于0', trigger: 'blur' }
    ],
    area: [
      { required: true, message: '请输入面积', trigger: 'blur' },
      { type: 'number', min: 0, message: '面积必须大于0', trigger: 'blur' }
    ],
    bedType: [
      { required: true, message: '请选择床型', trigger: 'change' }
    ],
    maxGuests: [
      { required: true, message: '请输入最大入住人数', trigger: 'blur' },
      { type: 'number', min: 1, message: '最大入住人数必须大于0', trigger: 'blur' }
    ]
  }
  
  // 加载房型数据
  const loadRoomTypeData = async () => {
    loading.value = true
    try {
      const params = {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        ...searchForm
      }
      const res = await getRoomTypeList(params)
      if (res.code === 200) {
        roomTypeList.value = res.data.records
        pagination.total = res.data.total
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
  
  // 加载房型名称选项
  const loadRoomTypeOptions = async () => {
    try {
      const res = await getRoomTypeNames()
      if (res.code === 200) {
        roomTypeOptions.value = res.data
      }
    } catch (error) {
      console.error('获取房型名称选项错误:', error)
      ElMessage.error('获取房型名称选项失败')
    }
  }
  
  // 获取房型详情
  const getDetail = async (id) => {
    try {
      const [infoRes, imagesRes] = await Promise.all([
        getRoomTypeInfo(id),
        getRoomTypeImages(id)
      ])
  
      if (infoRes.code === 200) {
        roomTypeDetail.value = infoRes.data
        // 填充表单数据
        Object.assign(formData, infoRes.data)
      }
  
      if (imagesRes.code === 200) {
        roomTypeImages.value = imagesRes.data
        // 分离主图和附图
        mainImage.value = imagesRes.data.find(img => img.isMain === 1)
        subImages.value = imagesRes.data.filter(img => img.isMain === 0)
      }
    } catch (error) {
      console.error('获取房型详情错误:', error)
      ElMessage.error('获取房型详情失败')
    }
  }
  
  // 添加房型
  const addRoomTypeBu = () => {
    dialogTitle.value = '添加房型'
    dialogType.value = 'add'
  
    // 重置表单
    Object.keys(formData).forEach(key => {
      if (key === 'maxGuests') {
        formData[key] = 2
      } else if (key === 'weight') {
        formData[key] = 100
      } else if (key === 'status') {
        formData[key] = 1
      } else if (key === 'price' || key === 'area') {
        formData[key] = 0
      } else {
        formData[key] = ''
      }
    })
  
    dialogVisible.value = true
  }
  
  // 编辑房型
  const editRoomType = async (row) => {
    dialogTitle.value = '编辑房型'
    dialogType.value = 'edit'
    await getDetail(row.id)
    dialogVisible.value = true
  }
  
  // 上传图片
  const handleImageUpload = async (file, isMain) => {
    try {
      const formData_2 = new FormData()
      formData_2.append('file', file.raw)
  
      const uploadRes = await uploadFile(formData_2)
      if (uploadRes.code === 200) {
        const imageRes = await uploadRoomTypeImage({
          roomTypeId: formData.id,
          isMain: isMain ? 1 : 0,
          imageUrl: uploadRes.data
        })
  
        if (imageRes.code === 200) {
          ElMessage.success('图片上传成功')
          await getDetail(formData.id)
        }
      }
    } catch (error) {
      console.error('上传图片错误:', error)
      ElMessage.error('上传图片失败')
    }
  }
  
  // 删除图片
  const deleteImage = async (id) => {
    try {
      const res = await deleteRoomTypeImage(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await getDetail(formData.id)
      }
    } catch (error) {
      console.error('删除图片错误:', error)
      ElMessage.error('删除图片失败')
    }
  }
  
  // 提交表单
  const submitForm = async () => {
    if (!formRef.value) return
  
    formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          if (dialogType.value === 'add') {
            const res = await addRoomType(formData)
            if (res.code === 200) {
              ElMessage.success('添加成功')
              dialogVisible.value = false
              loadRoomTypeData()
            }
          } else {
            const res = await updateRoomType(formData)
            if (res.code === 200) {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              loadRoomTypeData()
            }
          }
        } catch (error) {
          console.error('提交表单错误:', error)
          ElMessage.error('操作失败，请稍后再试')
        }
      }
    })
  }
  
  // 搜索
  const handleSearch = () => {
    pagination.currentPage = 1
    loadRoomTypeData()
  }
  
  // 重置搜索
  const resetSearch = () => {
    searchForm.typeName = ''
    searchForm.status = ''
    handleSearch()
  }
  
  // 页面加载时获取数据
  onMounted(() => {
    loadRoomTypeData()
    loadRoomTypeOptions()
  })
  
  // 返回首页
  const goToHome = () => {
    window.location.href = '/admin'
  }
  </script>
  
  <template>
    <div class="room-type-container">
      <div class="header">
        <h1 class="text-2xl font-bold mb-4">房型管理</h1>
        <div class="breadcrumb">
          <span @click="goToHome">首页</span> / <span>房型管理</span>
        </div>
      </div>
  
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item label="房型名称">
            <el-input v-model="searchForm.typeName" placeholder="请输入房型名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 160px">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="search-btn">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetSearch" class="reset-btn">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
  
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="primary" @click="addRoomTypeBu" class="add-btn">
          <el-icon><Plus /></el-icon>添加房型
        </el-button>
      </div>
  
      <!-- 数据表格 -->
      <el-card v-loading="loading" class="data-card">
        <el-table :data="roomTypeList" style="width: 100%" border @row-click="editRoomType" class="data-table">
          <el-table-column prop="typeName" label="房型名称" min-width="120" />
          <el-table-column prop="price" label="价格" min-width="100">
            <template #default="scope">
              <span class="price">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="area" label="面积(㎡)" min-width="100" />
          <el-table-column prop="bedType" label="床型" min-width="100" />
          <el-table-column prop="maxGuests" label="最大入住人数" min-width="120" />
          <el-table-column prop="facilities" label="设施" min-width="200">
            <template #default="scope">
              <el-tag v-for="(facility, index) in scope.row.facilities.split(',')" :key="index" class="mx-1 facility-tag">
                {{ facility }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" class="status-tag">
                {{ scope.row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
  
        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadRoomTypeData"
            @current-change="loadRoomTypeData"
          />
        </div>
      </el-card>
  
      <!-- 房型编辑对话框 -->
      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="70%" destroy-on-close class="edit-dialog">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px" class="edit-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="房型名称" prop="typeName">
                <el-input v-model="formData.typeName" placeholder="请输入房型名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="价格" prop="price">
                <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="面积" prop="area">
                <el-input-number v-model="formData.area" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="床型" prop="bedType">
                <el-input v-model="formData.bedType" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大入住人数" prop="maxGuests">
                <el-input-number v-model="formData.maxGuests" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="权重" prop="weight">
                <el-input-number v-model="formData.weight" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="设施" prop="facilities">
            <el-input v-model="formData.facilities" placeholder="请输入设施，用英文逗号分隔" />
          </el-form-item>
  
          <el-form-item label="描述" prop="description">
            <el-input v-model="formData.description" type="textarea" rows="3" />
          </el-form-item>
  
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">上架</el-radio>
              <el-radio :label="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
  
          <el-form-item label="图片">
            <div class="image-upload-area">
              <div class="main-image">
                <h4>主图</h4>
                <el-upload class="upload-demo" action="#" :auto-upload="false" :show-file-list="false"
                  :on-change="(file) => handleImageUpload(file, true)">
                  <el-button type="primary" class="upload-btn">
                    <el-icon><Upload /></el-icon>上传主图
                  </el-button>
                </el-upload>
                <div class="image-preview" v-if="mainImage">
                  <img :src="mainImage.imageUrl" alt="主图" />
                  <!-- <el-button type="danger" @click="deleteImage(mainImage.id)">删除</el-button> -->
                </div>
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>暂无主图</span>
                </div>
              </div>
  
              <div class="sub-images">
                <h4>附图</h4>
                <el-upload class="upload-demo" action="#" :auto-upload="false" :show-file-list="false"
                  :on-change="(file) => handleImageUpload(file, false)">
                  <el-button type="primary" class="upload-btn">
                    <el-icon><Upload /></el-icon>上传附图
                  </el-button>
                </el-upload>
                <div class="sub-images-list">
                  <div v-for="img in subImages" :key="img.id" class="image-preview">
                    <img :src="img.imageUrl" alt="附图" />
                    <el-button type="danger" @click="deleteImage(img.id)" class="delete-btn">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <div v-if="!subImages || subImages.length === 0" class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>暂无附图</span>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form>
  
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
            <el-button type="primary" @click="submitForm" class="confirm-btn">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <style scoped>
  .room-type-container {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: 100vh;
  }
  
  .header {
    margin-bottom: 24px;
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .search-area {
    margin-bottom: 20px;
    background: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .action-bar {
    margin-bottom: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .data-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .data-table {
    margin-bottom: 20px;
  }
  
  .data-table :deep(.el-table__header) {
    background-color: #f5f7fa;
  }
  
  .data-table :deep(.el-table__row) {
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .data-table :deep(.el-table__row:hover) {
    background-color: #f5f7fa;
  }
  
  .price {
    color: #f56c6c;
    font-weight: bold;
  }
  
  .facility-tag {
    margin-right: 8px;
    margin-bottom: 4px;
  }
  
  .status-tag {
    font-weight: bold;
  }
  
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
  
  .image-upload-area {
    display: flex;
    gap: 24px;
    margin-top: 16px;
  }
  
  .main-image,
  .sub-images {
    flex: 1;
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
  }
  
  .main-image h4,
  .sub-images h4 {
    margin-bottom: 16px;
    color: #606266;
  }
  
  .upload-btn {
    margin-bottom: 16px;
  }
  
  .image-preview {
    position: relative;
    width: 200px;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .image-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .image-placeholder {
    width: 200px;
    height: 150px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    color: #909399;
    font-size: 14px;
    background: #fafafa;
  }
  
  .image-placeholder .el-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  .sub-images-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .delete-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 4px;
    border-radius: 50%;
  }
  
  .edit-dialog {
    border-radius: 8px;
  }
  
  .edit-form {
    padding: 20px;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #ebeef5;
  }
  
  .breadcrumb {
    font-size: 14px;
    color: #666;
  }
  
  .breadcrumb span {
    cursor: pointer;
    transition: color 0.3s;
  }
  
  .breadcrumb span:hover {
    color: #409EFF;
  }
  
  .search-btn,
  .reset-btn,
  .add-btn {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .cancel-btn,
  .confirm-btn {
    min-width: 80px;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
  
  :deep(.el-dialog__header) {
    margin: 0;
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  :deep(.el-dialog__footer) {
    padding: 0;
  }
  .el-button--danger:hover {
    background-color: #f76969 !important;
    border-color: #f76969 !important;
  }
  .el-button--danger {
    background-color: #f76969 !important;
    border-color: #f76969 !important;
  }
  .el-button--primary:hover {
    background-color: #66b1ff !important;
    border-color: #66b1ff !important;
  }
  .el-button--primary {
    background-color: #409EFF !important;
    border-color: #409EFF !important;
  }
  </style>