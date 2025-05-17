/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  // 更多环境变量...
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 引入Vue组件的类型声明
/// <reference path="./src/shims-vue.d.ts" />

// 引入Vant组件的类型声明
/// <reference path="./src/vant.d.ts" />
