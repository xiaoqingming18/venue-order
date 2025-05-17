declare module 'vant' {
  import { App } from 'vue'
  
  const Vant: {
    install: (app: App) => void
  }
  
  export default Vant
  
  // 导出组件
  export const Button: any
  export const Form: any
  export const Field: any
  export const CellGroup: any
  export const NavBar: any
  export const Tab: any
  export const Tabs: any
  export const Popup: any
  export const Picker: any
  export const Uploader: any
  export const List: any
  export const PullRefresh: any
  export const Cell: any
  export const Empty: any
  export const SwipeCell: any
  export const Dialog: any
  export const Toast: any
  export const showSuccessToast: (message: string) => void
  export const showFailToast: (message: string) => void
  export const showToast: (options: any) => void
  export const showLoadingToast: (options: any) => void
} 