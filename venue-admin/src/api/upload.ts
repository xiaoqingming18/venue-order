import request from '@/utils/request'

/**
 * 上传文件
 */
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传场馆封面图片
 */
export function uploadVenueCover(id: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `/venues/${id}/cover`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 