/**
 * 日期格式化函数
 * @param date 日期对象或日期字符串
 * @param fmt 格式化模板，如 YYYY-MM-DD HH:mm:ss
 * @returns 格式化后的日期字符串
 */
export const formatDate = (date: Date | string, fmt = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) return ''
  
  let result = fmt
  const d = typeof date === 'string' ? new Date(date) : date
  
  const o: Record<string, any> = {
    'M+': d.getMonth() + 1, // 月份
    'D+': d.getDate(), // 日
    'H+': d.getHours(), // 小时
    'm+': d.getMinutes(), // 分
    's+': d.getSeconds(), // 秒
    'q+': Math.floor((d.getMonth() + 3) / 3), // 季度
    'S': d.getMilliseconds() // 毫秒
  }
  
  if (/(Y+)/.test(result)) {
    result = result.replace(RegExp.$1, (d.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  
  for (const k in o) {
    if (new RegExp('(' + k + ')').test(result)) {
      result = result.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }
  
  return result
}

/**
 * 金额格式化函数
 * @param amount 金额数值
 * @param decimals 小数位数，默认2位
 * @param separator 千位分隔符，默认为,
 * @returns 格式化后的金额字符串
 */
export const formatCurrency = (amount: number | string, decimals = 2, separator = ','): string => {
  if (amount === undefined || amount === null) return '0.00'
  
  const num = Number(amount)
  if (isNaN(num)) return '0.00'
  
  const numStr = num.toFixed(decimals)
  const parts = numStr.split('.')
  const intPart = parts[0]
  const decimalPart = parts[1] ? '.' + parts[1] : ''
  
  // 添加千位分隔符
  const reg = /(\d)(?=(?:\d{3})+$)/g
  const formattedIntPart = intPart.replace(reg, '$1' + separator)
  
  return formattedIntPart + decimalPart
} 