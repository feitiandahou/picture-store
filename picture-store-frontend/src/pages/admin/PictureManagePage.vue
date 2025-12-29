<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost
          >+ 批量创建图片</a-button
        >
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="关键词">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="类型">
        <a-input v-model:value="searchParams.category" placeholder="请输入类型" allow-clear />
      </a-form-item>
      <a-form-item label="标签">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          placeholder="请输入标签"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="reviewStatus" label="审核状态">
        <a-select
          v-model:value="searchParams.reviewStatus"
          style="min-width: 180px"
          placeholder="请选择审核状态"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image
            :src="record.url"
            :width="120"
            :height="80"
            :preview="false"
            style="object-fit: cover; border-radius: 4px"
          />
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in safeParseTags(record.tags)" :key="tag">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽度：{{ record.picWidth }}</div>
          <div>高度：{{ record.picHeight }}</div>
          <div>宽高比：{{ record.picScale }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
          <div v-if="record.reviewTime">
            审核时间：{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space size="small">
            <!-- 审核操作下拉 -->
            <a-dropdown
              v-if="
                record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS &&
                record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT
              "
            >
              <a-button type="link" size="small">审核</a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)">
                    <span style="color: #52c41a">✅ 通过</span>
                  </a-menu-item>
                  <a-menu-item @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)">
                    <span style="color: #ff4d4f">❌ 拒绝</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>

            <!-- 直接显示“已通过”或“已拒绝”状态（可选） -->
            <a-tag
              v-else
              :color="record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? 'green' : 'red'"
              style="font-size: 12px; padding: 0 6px"
            >
              {{ record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '已通过' : '已拒绝' }}
            </a-tag>

            <!-- 编辑 & 删除 -->
            <a-button
              type="link"
              size="small"
              :href="`/add_picture?id=${record.id}`"
              target="_blank"
            >
              编辑
            </a-button>
            <a-button danger size="small" @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deletePictureUsingPost,
  listPictureByPageUsingPost,
  doPictureReviewUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_OPTIONS,
  PIC_REVIEW_STATUS_MAP,
} from '../../constants/picture'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
//定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)
//处理tags
const safeParseTags = (tagsStr: string | null | undefined): string[] => {
  if (!tagsStr) return []
  try {
    const parsed = JSON.parse(tagsStr)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.warn('Failed to parse tags:', tagsStr, e)
    return []
  }
}
//搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})
//获取数据
const fetchData = async () => {
  console.log('搜索参数：')
  console.log(searchParams)

  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    console.log('获取数据成功:', res.data.data)

    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败, ' + res.data.message)
  }
}
//页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})
//分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})
//表格变化后重新获取数据
const doTableChange = (page: any) => {
  console.log('表格变化后page:', page)

  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}
//搜索数据
const doSearch = () => {
  //重置页码
  searchParams.current = 1
  fetchData()
}
//删除数据
const doDelete = async (id: number) => {
  if (!id) return
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    //刷新数据
    fetchData()
  } else {
    message.error('删除失败, ' + res.data.message)
  }
}
//审核图片
const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核成功')
    //刷新数据
    fetchData()
  } else {
    message.error('审核失败, ' + res.data.message)
  }
}
</script>

<style scoped></style>
