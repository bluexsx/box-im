<template>
  <view class="loading-box">
    <view class="rotate iconfont icon-loading" ></view>
  </view>
</template>

<script>
  export default {
    data() {
      return {};
    },
    methods: {
    },
    computed: {
    }
  };
</script>

<style>
  .loading-box {
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
    position: absolute;
    left: 0;
    top: 0;
    z-index: 10000;
    display: flex;
    justify-content: center;
    align-items: center;
  }



  .rotate {
    animation: rotate 2s ease-in-out infinite;
	font-size: 100rpx;
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg)
    }

    to {
      transform: rotate(360deg)
    }
  }
</style>