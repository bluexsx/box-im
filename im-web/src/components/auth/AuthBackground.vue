<template>
  <div class="auth-bg" :class="{ 'auth-bg--dimmed': dimmed }">
    <div class="auth-bg__base" />
    <div class="auth-bg__glows" />
    <div class="auth-bg__panels" />
    <div class="auth-bg__dots" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  dimmed?: boolean;
}>();
</script>

<style scoped lang="scss">
.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-bg__base,
.auth-bg__glows,
.auth-bg__panels,
.auth-bg__dots {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-bg__base {
  background: linear-gradient(135deg, #d4e2f8 0%, #dce8fa 25%, #e6eefb 50%, #eef2fc 75%, #f8f9fd 100%);
}

.auth-bg__glows {
  background-image:
    radial-gradient(ellipse 70% 50% at 85% 15%, rgba(220, 235, 255, 0.6) 0%, transparent 55%),
    radial-gradient(ellipse 60% 45% at 10% 75%, rgba(230, 240, 255, 0.5) 0%, transparent 50%),
    radial-gradient(ellipse 50% 40% at 50% 50%, rgba(255, 255, 255, 0.4) 0%, transparent 45%);
}

.auth-bg__dots {
  opacity: 0.4;
  background-image: radial-gradient(circle, rgba(200, 218, 248, 0.35) 1px, transparent 1px);
  background-size: 24px 24px;
}

.auth-bg__panels {
  opacity: 0.5;

  background-image:
    repeating-linear-gradient(115deg, transparent 0, transparent 60px, rgba(190, 210, 245, 0.12) 60px, rgba(190, 210, 245, 0.12) 65px),
    repeating-linear-gradient(25deg, transparent 0, transparent 90px, rgba(200, 218, 248, 0.08) 90px, rgba(200, 218, 248, 0.08) 95px);
}

.auth-bg--dimmed {
  .auth-bg__base,
  .auth-bg__glows,
  .auth-bg__panels,
  .auth-bg__dots {
    opacity: 0.5;
  }
}
</style>
