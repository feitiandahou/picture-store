import { message } from 'ant-design-vue'
import router from './router'
import { useLoginUserStore } from './stores/userStore'

let firstFetchLoginUser = true

// 定义无需登录即可访问的页面（白名单）
const whiteList = ['/user/login', '/user/register']

router.beforeEach(async (to, from, next) => {
  console.log('进入全局权限校验')

  const toPath = to.path

  // 如果是白名单页面，直接放行，不获取用户信息
  if (whiteList.includes(toPath)) {
    console.log('白名单页面，跳过校验')
    next()
    return
  }

  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  if (firstFetchLoginUser) {
    try {
      await loginUserStore.fetchLoginUser()
      loginUser = loginUserStore.loginUser
      firstFetchLoginUser = false
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 即使失败，也要继续，避免卡死
    }
  }

  console.log('loginUser', loginUser)

  // 权限校验：仅对 /admin 生效
  if (toPath.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      console.log('没有权限')
      message.error('没有权限')
      next(`/user/login?redirect= $ {to.fullPath}`)
      return
    }
  }

  next()
})
