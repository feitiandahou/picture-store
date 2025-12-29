<template>
  <div id="base-layout">
    <a-layout class="a-layout">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="#" target="_blank"> 云图库 </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '../components/GlobalHeader.vue'
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
    margin-bottom: 16px;
    width: 100%;
  }
  .content {
    padding: 20px;
    background: linear-gradient(to right, #fefefe, #fff);
  }
  .footer {
    background: #efefef;
    padding: 16px;
    text-align: center;
  }
}
</style>
