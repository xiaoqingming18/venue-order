// 这是一个临时的mock文件，用于模拟Vant组件
// 当无法安装Vant库时，可以使用这个文件提供基本的功能

import { h } from 'vue'
import type { App, Component } from 'vue'

export const showToast = (options: any) => {
  if (typeof options === 'string') {
    console.log('[Toast]', options);
    alert(options);
  } else {
    console.log('[Toast]', options.message || '提示信息');
    alert(options.message || '提示信息');
  }
};

export const showSuccessToast = (message: string) => {
  console.log('[SuccessToast]', message);
  alert(`成功: ${message}`);
};

export const showFailToast = (message: string) => {
  console.log('[FailToast]', message);
  alert(`失败: ${message}`);
};

export const showLoadingToast = (options: any) => {
  const message = typeof options === 'string' ? options : (options.message || '加载中...');
  console.log('[LoadingToast]', message);
};

export const Dialog = {
  confirm: (options: any) => {
    return new Promise((resolve, reject) => {
      if (window.confirm(options.message || '确认操作?')) {
        resolve(true);
      } else {
        reject(false);
      }
    });
  },
  alert: (options: any) => {
    const message = typeof options === 'string' ? options : (options.message || '提示');
    alert(message);
    return Promise.resolve();
  }
};

// 添加SwipeCell的模拟实现
export const SwipeCell = {
  close: (position: string) => {
    console.log(`[SwipeCell] Closing ${position}`);
  }
};

// 图片预览功能的模拟实现
export const showImagePreview = (images: string | string[], options?: any) => {
  console.log('[ImagePreview]', images, options);
  if (typeof images === 'string') {
    window.open(images, '_blank');
  } else if (Array.isArray(images) && images.length > 0) {
    window.open(images[0], '_blank');
  }
};

// 创建模拟的Vant组件
const createMockComponent = (name: string): Component => {
  return {
    name,
    render() {
      // 渲染一个div，显示组件名称，并传递插槽内容
      return h('div', { class: `mock-vant-${name}` }, [
        h('div', { class: 'mock-vant-notice' }, `Mock ${name}`),
        this.$slots.default ? this.$slots.default() : null
      ]);
    }
  };
};

// 模拟Vant插件
export const VantMock = {
  install(app: App) {
    // 注册模拟的Vant组件
    const components = [
      'Button', 'Form', 'Field', 'CellGroup', 'NavBar', 'Tab', 'Tabs', 
      'Popup', 'Picker', 'Uploader', 'List', 'PullRefresh', 'Cell', 
      'Empty', 'SwipeCell', 'Dialog', 'Toast', 'Image', 'Loading'
    ];
    
    components.forEach(name => {
      app.component(`van-${name.toLowerCase()}`, createMockComponent(name));
    });
    
    console.log('[Vant Mock] 已注册模拟的Vant组件');
  }
};

export default VantMock; 