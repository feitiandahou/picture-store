<template>
  <div id="userLoginPage">
    <h2 class="title">云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item class="buttonItem">
        <a-button class="loginButton" type="primary" html-type="submit">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { useLoginUserStore } from '@/stores/userStore'
import { userLoginUsingPost } from '@/api/userController'
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import router from '@/router'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 */

const handleSubmit = async (values: API.UserLoginRequest) => {
  console.log('用户登录')
  console.log(values)

  const res = await userLoginUsingPost(values)
  //登陆成功，吧登录态保存到全局状态中
  console.log('用户登录res')
  console.log(res)

  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')

    router.replace('/')
  } else {
    message.error('登录失败,' + res.data.message)
  }
}
</script>
<style scoped>
#userLoginPage {
  max-width: 460px;
  margin: 0 auto;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
.buttonItem {
  text-align: center;
}
.loginButton {
  width: 50%;
}
</style>
