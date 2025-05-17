/**
 * 分页结果接口
 */
export interface PageResult<T> {
  /** 当前页码 */
  current: number
  /** 每页大小 */
  size: number
  /** 总页数 */
  pages: number
  /** 总记录数 */
  total: number
  /** 是否有上一页 */
  hasPrevious: boolean
  /** 是否有下一页 */
  hasNext: boolean
  /** 数据记录列表 */
  records: T[]
}

/**
 * 响应结果接口
 */
export interface ResponseResult<T = any> {
  /** 状态码 */
  code: number
  /** 数据 */
  data: T
  /** 消息 */
  message: string
}

/**
 * 选项接口
 */
export interface Option {
  /** 选项值 */
  value: string | number
  /** 选项标签 */
  label: string
  /** 是否禁用 */
  disabled?: boolean
  /** 子选项 */
  children?: Option[]
} 