import request from '@/utils/request'
import type { BookingOrderDTO, BookingOrder } from '@/types/booking'
import { useUserStore } from '@/stores/user'

/**
 * 获取场馆可预约时间
 */
export function getVenueAvailability(venueId: number, date: string) {
  console.log(`请求场馆可用性接口: /venues/${venueId}/availability?date=${date}`)
  return request({
    url: `/venues/${venueId}/availability`,
    method: 'get',
    params: { date },
  })
}

/**
 * 预约场馆
 */
export function createBooking(data: BookingOrderDTO) {
  // 深拷贝请求数据，避免引用问题
  const requestData = JSON.parse(JSON.stringify(data));
  
  // 验证日期和时间格式
  if (!requestData.bookingDate || !requestData.startTime || !requestData.endTime) {
    console.error('预约数据不完整：', requestData)
    return Promise.reject(new Error('预约数据不完整，请填写所有必要信息'))
  }
  
  // 验证设施预约至少有一个设施
  if (requestData.bookingType === 2 && (!requestData.facilities || requestData.facilities.length === 0)) {
    console.error('设施预约必须选择至少一个设施')
    return Promise.reject(new Error('设施预约必须选择至少一个设施'))
  }
  
  // 确保bookingType是数字
  if (typeof requestData.bookingType === 'string') {
    requestData.bookingType = Number(requestData.bookingType);
  }
  
  // 确保venueId是数字
  if (typeof requestData.venueId === 'string') {
    requestData.venueId = Number(requestData.venueId);
  }
  
  // 确保locationId是数字(如果存在)
  if (requestData.locationId && typeof requestData.locationId === 'string') {
    requestData.locationId = Number(requestData.locationId);
  } else {
    // 不再需要locationId，直接删除该字段
    delete requestData.locationId;
  }
  
  // 确保设施数据是正确的
  if (requestData.facilities && requestData.facilities.length > 0) {
    requestData.facilities = requestData.facilities.map((facility: { facilityId: number | string, quantity: number | string }) => ({
      facilityId: typeof facility.facilityId === 'string' ? Number(facility.facilityId) : facility.facilityId,
      quantity: typeof facility.quantity === 'string' ? Number(facility.quantity) : facility.quantity
    }));
  }

  // 日期格式再次验证 - 确保格式为yyyy-MM-dd
  if (requestData.bookingDate) {
    // 验证并重新格式化日期
    try {
      const date = new Date(requestData.bookingDate);
      if (!isNaN(date.getTime())) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        requestData.bookingDate = `${year}-${month}-${day}`;
      }
    } catch (e) {
      console.error('日期格式化错误:', e);
    }
  }
  
  // 时间格式再次验证 - 确保格式为HH:mm:ss
  ['startTime', 'endTime'].forEach(key => {
    if (requestData[key]) {
      // 首先尝试解析为标准时间格式
      if (requestData[key].split(':').length < 3) {
        // 如果不是HH:mm:ss格式，添加秒数
        requestData[key] = requestData[key] + (requestData[key].split(':').length === 2 ? ':00' : ':00:00');
      }
    }
  });

  console.log('最终提交预约数据:', JSON.stringify(requestData, null, 2));
  
  // 为了确保数据格式绝对正确，我们尝试两种方式提交
  const attemptSubmit = async () => {
    try {
      // 方式1: 使用标准JSON格式提交
      return await request({
        url: '/booking-orders',
        method: 'post',
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        },
        data: requestData
      });
    } catch (error: any) {
      console.error('标准JSON提交失败:', error);
      
      // 输出更多错误信息
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
        
        // 检查是否是外键约束错误
        const errorMessage = error.response.data?.message || '';
        if (
          errorMessage.includes('SQLIntegrityConstraintViolationException') ||
          errorMessage.includes('foreign key constraint')
        ) {
          console.error('外键约束错误');
          
          if (errorMessage.includes('facility_id')) {
            throw new Error('选择的设施不存在或已被禁用，请重新选择');
          } else if (errorMessage.includes('venue_id')) {
            throw new Error('选择的场馆不存在或已被禁用，请选择其他场馆');
          } else {
            throw new Error('数据关联错误，请检查您的输入');
          }
        }
        
        if (error.response.headers) {
          console.error('响应头:', error.response.headers);
        }
      }
      
      throw error;
    }
  };
  
  return attemptSubmit();
}

/**
 * 获取预约订单列表
 */
export function getBookingOrders(params: {
  page?: number
  size?: number
  status?: number
}) {
  return request({
    url: '/booking-orders',
    method: 'get',
    params,
  })
}

/**
 * 获取预约订单详情
 */
export function getBookingOrderById(id: number) {
  return request({
    url: `/booking-orders/${id}`,
    method: 'get',
  })
}

/**
 * 取消预约
 */
export function cancelBooking(id: number) {
  return request({
    url: `/booking-orders/${id}/cancel`,
    method: 'put',
  })
}

/**
 * 使用支付宝支付订单
 * @param orderId 订单ID
 * @returns 支付表单响应
 */
export function payBookingWithAlipay(orderId: number) {
  console.log('正在调用支付宝支付接口，订单ID:', orderId)
  return request({
    url: `/payment/pay/${orderId}`,
    method: 'get',
    responseType: 'text', // 指定响应类型为文本，这很重要，确保能正确接收HTML表单
    headers: {
      'Accept': 'text/html,application/xhtml+xml,application/json'
    }
  })
}

/**
 * 支付预约订单 - 旧接口，保留兼容性
 * @deprecated 使用 payBookingWithAlipay 替代
 */
export function payBooking(id: number) {
  return payBookingWithAlipay(id)
}

/**
 * 获取按类型分组的预约规则
 */
export function getBookingRulesByType() {
  return request({
    url: '/booking-rules/group-by-type',
    method: 'get'
  })
}

/**
 * 根据日期范围获取特殊日期规则
 */
export function getSpecialDateRules(startDate: string, endDate: string) {
  return request({
    url: '/special-date-rules/date-range',
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取用户个人的预约订单列表
 */
export function getUserBookingOrders(params: {
  page?: number
  size?: number
  status?: number
}) {
  // 处理空参数情况
  const validParams: any = {};
  
  try {
    // 从用户仓库获取用户ID
    const userStore = useUserStore();
    if (!userStore) {
      console.error('获取订单失败：无法获取用户存储');
      return Promise.reject(new Error('系统错误：无法获取用户信息'));
    }
    
    const userId = userStore.userInfo?.id;
    console.log('当前用户ID:', userId);
    
    // 只有在用户已登录且有ID的情况下才添加userId参数
    if (userId) {
      validParams.userId = userId;
    } else {
      console.error('获取订单失败：用户未登录或无法获取用户ID');
      return Promise.reject(new Error('未登录或无法获取用户ID'));
    }
    
    // 只添加有效的非空参数
    if (params.page !== undefined && params.page !== null) {
      validParams.page = params.page;
    }
    
    if (params.size !== undefined && params.size !== null) {
      validParams.size = params.size;
    }
    
    if (params.status !== undefined && params.status !== null) {
      validParams.status = params.status;
    }
    
    console.log('订单查询参数:', validParams);
    
    // 尝试使用标准订单查询接口
    return request({
      url: '/booking-orders',
      method: 'get',
      params: validParams,
    }).then(response => {
      console.log('订单API完整响应:', response);
      
      // 尝试标准化响应结构
      if (response.data) {
        // 检查是否已经按照预期结构返回
        if (Array.isArray(response.data)) {
          // 直接返回数组
          return response;
        }
        
        // 检查是否是Spring分页格式
        if (response.data.content && Array.isArray(response.data.content)) {
          return response;
        }
        
        // 检查是否是MyBatis Plus分页格式
        if (response.data.records && Array.isArray(response.data.records)) {
          // 不需要转换，原样返回
          return response;
        }
        
        // 检查是否单个对象
        if (typeof response.data === 'object' && response.data.id) {
          // 将单个对象转为数组包装在响应中
          const newData = { ...response, data: [response.data] };
          console.log('将单个对象转换为数组返回');
          return newData;
        }
        
        // 如果是其他格式但包含数据，尝试查找数据
        const findArrayInObject = (obj: any): any[] | null => {
          if (!obj || typeof obj !== 'object') return null;
          
          // 遍历对象所有属性
          for (const key in obj) {
            // 如果属性是数组且至少有一个元素是对象且有id属性
            if (Array.isArray(obj[key]) && obj[key].length > 0 && 
                typeof obj[key][0] === 'object' && obj[key][0].id) {
              return obj[key];
            }
            
            // 如果属性是对象，递归查找
            if (obj[key] && typeof obj[key] === 'object') {
              const result = findArrayInObject(obj[key]);
              if (result) return result;
            }
          }
          
          return null;
        };
        
        const foundArray = findArrayInObject(response.data);
        if (foundArray) {
          console.log('在响应对象中找到可能的订单数组');
          const newData = { ...response, data: foundArray };
          return newData;
        }
      }
      
      // 默认返回原始响应
      return response;
    });
  } catch (error) {
    console.error('构建订单请求时出错:', error);
    return Promise.reject(error);
  }
}

/**
 * 获取用户个人的预约订单详情
 */
export function getUserBookingOrderById(id: number) {
  if (!id || isNaN(id)) {
    console.error('无效的订单ID:', id);
    return Promise.reject(new Error('无效的订单ID'));
  }
  
  try {
    // 从用户仓库获取用户ID
    const userStore = useUserStore();
    if (!userStore) {
      console.error('获取订单详情失败：无法获取用户存储');
      return Promise.reject(new Error('系统错误：无法获取用户信息'));
    }
    
    const userId = userStore.userInfo?.id;
    
    // 只有在用户已登录且有ID的情况下才添加userId参数
    if (!userId) {
      console.error('获取订单详情失败：用户未登录或无法获取用户ID');
      return Promise.reject(new Error('未登录或无法获取用户ID'));
    }
    
    // 使用标准订单详情接口
    return request({
      url: `/booking-orders/${id}`,
      method: 'get',
      params: {
        userId: userId
      }
    });
  } catch (error) {
    console.error('构建订单详情请求时出错:', error);
    return Promise.reject(error);
  }
}

/**
 * 获取订单的设施列表
 */
export function getOrderFacilities(orderId: number) {
  return request({
    url: `/booking-orders/${orderId}/facilities`,
    method: 'get',
  })
} 