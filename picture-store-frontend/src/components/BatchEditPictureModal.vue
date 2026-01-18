<template>
  <div class="batch-edit-picture-modal">
    <a-modal v-model:visible="visible" title="批量编辑图片" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type="secondary">* 只对当前页面的图片生效</a-typography-paragraph>
      <!-- 批量创建表单 -->
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item name="category" label="分类">
          <a-auto-complete
            v-model:value="formData.category"
            placeholder="请输入分类"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="tags" label="标签">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="请输入标签"
            :options="tagOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item name="nameRule" label="命名规则">
          <a-input
            v-model:value="formData.nameRule"
            placeholder="请输入命名规则，输入 {序号} 可动态生成"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPictureByBatchUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'

interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}
const props = withDefaults(defineProps<Props>(), {})
//是否可见
const visible = ref(false)
//打开弹窗
const openModal = () => {
  visible.value = true
}
//关闭弹窗
const closeModal = () => {
  visible.value = false
}
//暴露给父组件
defineExpose({
  openModal,
})
const formData = reactive<API.PictureEditByBatchRequest>({
  category: '',
  tags: [],
  nameRule: '',
})
/**
 * 提交表单
 */
const handleSubmit = async (values: any) => {
  if (!props.pictureList) {
    return
  }
  const inputCategory = values.category?.trim() || null
  const inputTags =
    Array.isArray(values.tags) && values.tags.length > 0 ? new Set(values.tags) : null
  const targetPictures = props.pictureList.filter((picture) => {
    // 如果用户没填 category 和 tags，则默认更新全部（向后兼容）
    if (inputCategory === null && inputTags === null) {
      return true
    }

    // 分类匹配（如果用户填写了）
    const categoryMatch = inputCategory === null || picture.category === inputCategory

    // 标签匹配（如果用户填写了）
    let tagsMatch = true
    if (inputTags !== null) {
      const pictureTags = new Set(picture.tags || [])
      // 要求：用户输入的每个 tag 都在图片的 tags 中（交集包含所有输入）
      tagsMatch = values.tags.every((tag) => pictureTags.has(tag))
      // 或者改成：只要有一个匹配就更新？按需调整逻辑
    }

    return categoryMatch && tagsMatch
  })
  console.log('targetPictures', targetPictures)
  if (targetPictures.length === 0) {
    message.warning('当前页面没有符合条件的图片')
    return
  }
  // 4. 构建更新字段（只包含非空值）
  const updateFields: Record<string, any> = {}
  if (inputCategory !== null) {
    updateFields.category = inputCategory
  }
  if (inputTags !== null) {
    updateFields.tags = values.tags
  }
  if (values.nameRule !== undefined && values.nameRule !== '') {
    updateFields.nameRule = values.nameRule
  }
  // 5. 发送请求
  const requestData = {
    pictureIdList: targetPictures.map((p) => p.id),
    spaceId: props.spaceId,
    ...updateFields,
  }

  console.log('【实际更新的图片数量】', targetPictures.length)
  console.log('【发送数据】', requestData)

  const res = await editPictureByBatchUsingPost(requestData)
  if (res.data.code === 0 && res.data.data) {
    message.success(`批量编辑 ${targetPictures.length} 张图片成功`)
    props.onSuccess()
    closeModal()
  } else {
    message.error('批量编辑失败：' + (res.data.message || '未知错误'))
  }
}
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})
</script>
<style scoped></style>
