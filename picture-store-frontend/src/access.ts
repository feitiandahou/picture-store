import { message } from 'ant-design-vue'
import router from './router'
import { useLoginUserStore } from './stores/userStore'

let firstFetchLoginUser = true

/**
 * 全局权限校验，每次切换页面时都会执行
 */
router.beforeEach(async (to, from, next) => {
  console.log('进入全局权限校验')

  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面刷新时，首次加载时能等待后端返回用户信息后再校验权限
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  console.log('loginUser', loginUser)

  const toUrl = to.fullPath
  console.log('toUrl', toUrl)
  // 自定义权限校验逻辑，比如管理员才能访问/admin开头的页面
  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      console.log('没有权限')
      message.error('没有权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
