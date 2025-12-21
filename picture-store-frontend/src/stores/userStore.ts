/* eslint-disable */
import { getLoginUserUsingGet } from '@/api/userController'
import { defineStore } from 'pinia'
import { ref } from 'vue'

const USER_KEY = 'login_user'
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<any>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      setLoginUser(res.data.data)
    }
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return {
    loginUser,
    fetchLoginUser,
    setLoginUser,
  }
})
