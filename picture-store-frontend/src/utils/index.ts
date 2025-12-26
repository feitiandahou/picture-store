import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)} MB`
}

/**
 * 下载图片
 */
export function downloadImage(url?: string, fileName?: string) {
  if (!url) return

  const link = document.createElement('a')
  link.href = url
  link.download = fileName || 'image.jpg' // 注意：部分浏览器需同源才生效
  link.target = '_blank'

  // 对于跨域资源，某些浏览器（如 Chrome）会忽略 download 属性
  // 所以更可靠的方式是：直接跳转（用户手动右键另存为），或使用方案二
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
