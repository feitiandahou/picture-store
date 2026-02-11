<template>
  <div id="mySpacePage">
    <p>正在跳转，请稍后。。。</p>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/userStore'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space'
const router = useRouter()
const loginUserStore = useLoginUserStore()

//检查用户是否有个人空间
const checkUserSpace = async () => {
  //用户未登录，则直接跳转到登陆页面
  const loginUser = loginUserStore.loginUser
  const userId = loginUser.id
  if (!userId) {
    router.replace('/user/login')
    return
  }
  //如果用户已登录，会获取该用户已经创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: userId,
    current: 1,
    pageSize: 1,
    spaceType: SPACE_TYPE_ENUM.PRIVATE,
  })
  if (res.data.code === 0) {
    //如果有，则进入第一个空间
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data.records[0]
      router.replace(`/space/${space.id}`)
    } else {
      //如果用户未创建空间，则跳转到创建空间页
      router.replace(`/add_space`)
      message.warning('请先创建空间')
    }
  } else {
    message.error('加载空间失败,' + res.data.message)
  }
}
//在页面加载时检查用户空间
onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped></style>
