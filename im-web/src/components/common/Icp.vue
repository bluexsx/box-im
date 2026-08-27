<template>
  <div class="icp">
    <img class="icp-icon" :src="icpLogo" alt="备案图标" />
    <a target="_blank" rel="noopener noreferrer" href="https://beian.miit.gov.cn/">粤ICP备xxxx号-1</a>
  </div>
</template>

<script setup lang="ts">
import icpLogo from '@/assets/image/icp_logo.png';
</script>

<style scoped lang="scss">
.icp {
  position: fixed;
  text-align: center;
  bottom: 20px;
  margin: 0 auto;
  width: 100%;
  color: #5c6b77;
  z-index: 2;

  .icp-icon {
    width: 20px;
    height: 20px;
    vertical-align: bottom;
  }

  a {
    color: #5c6b77;
    text-decoration: none;
    margin-left: 4px;
  }
}
</style>
