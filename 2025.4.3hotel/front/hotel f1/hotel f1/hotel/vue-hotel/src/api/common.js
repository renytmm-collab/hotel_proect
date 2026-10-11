import request from '@/utils/request'

/**
 * 上传文件（头像/房型图片）
 * @param {FormData} formData - 包含文件的表单数据
 * @returns {Promise}
 */
export function uploadFile(formData) {
  return request({
    url: '/api/admin/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  uploadFile
} 