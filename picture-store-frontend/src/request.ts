import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  baseURL: 'http://localhost:8123',
  timeout: 60000,
  withCredentials: true,
})

//全局请求拦截器
myAxios.interceptors.request.use(
  (config) => {
    //在发送请求之前做些什么
    return config
  },
  (error) => {
    //对请求错误做些什么
    return Promise.reject(error)
  },
)
//全局响应拦截器
myAxios.interceptors.response.use(
  (response) => {
    //对响应数据做点什么
    const { data } = response
    if (data.code == 40100) {
      if (
        !response.request.responseURL.includes('/api/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  (error) => {
    //对响应错误做点什么
    return Promise.reject(error)
  },
)

export default myAxios
