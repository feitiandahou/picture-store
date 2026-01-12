<template>
  <div id="base-layout">
    <a-layout class="a-layout">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">
        <a href="#" target="_blank"> 云图库 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '../components/GlobalHeader.vue'
import GlobalSider from '../components/GlobalSider.vue'
</script>

<style scoped lang="less">
#base-layout {
  .a-layout {
    min-height: 100vh;
  }

  .header {
    padding-inline: 20px;
    background: white;
    color: unset;
    margin-bottom: 1px;
    width: 100%;
  }
  .sider {
    background: #fff;
    border-right: 0.5px solid #eee;
    padding-top: 20px;
  }

  :deep(.ant-menu-root) {
    border-bottom: none !important;
    border-inline-end: none !important;
  }
  .content {
    padding: 28px;
    background: linear-gradient(to right, #fefefe, #fff);
  }
  .footer {
    background: #efefef;
    padding: 16px;
    text-align: center;
  }
}
</style>
