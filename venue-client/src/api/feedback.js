import http from '@/utils/http'

/**
 * 提交用户反馈
 * @param {Object} data 反馈数据
 * @returns {Promise}
 */
export function submitFeedback(data) {
  return http.post('/user/feedback/submit', data)
}

/**
 * 获取用户反馈详情
 * @param {number} id 反馈ID
 * @returns {Promise}
 */
export function getFeedbackDetail(id) {
  return http.get(`/user/feedback/detail/${id}`)
}

/**
 * 获取用户反馈列表
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 * @returns {Promise}
 */
export function getUserFeedbackList(pageNum = 1, pageSize = 8) {
  // 确保页码至少为1
  const page = Math.max(1, pageNum || 1);
  
  console.log(`API调用: getUserFeedbackList - 页码: ${page}, 每页数量: ${pageSize || 8}`);
  
  return http.get('/user/feedback/list', {
    params: { 
      pageNum: page, 
      pageSize: pageSize || 8
    },
    timeout: 15000
  }).then(response => {
    console.log('获取反馈列表响应:', response);
    return response;
  }).catch(error => {
    console.error('获取反馈列表错误:', error);
    throw error;
  })
}

/**
 * 获取反馈回复列表
 * @param {number} feedbackId 反馈ID
 * @returns {Promise}
 */
export function getFeedbackReplies(feedbackId) {
  return http.get(`/feedback/reply/list/${feedbackId}`)
} 