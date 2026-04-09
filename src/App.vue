<template>
  <div class="app">
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);">
        <h1 style="margin: 0; font-size: 18px; font-weight: 600;">问卷管理系统</h1>
        <a-menu mode="horizontal" :selected-keys="[currentRoute]" @select="handleMenuSelect">
          <a-menu-item key="/">
            问卷列表
          </a-menu-item>
        </a-menu>
      </a-layout-header>
      <a-layout-content style="padding: 24px; min-height: calc(100vh - 64px);">
        <router-view />
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const currentRoute = computed(() => route.path);

const handleMenuSelect = (key) => {
  router.push(key);
};
</script>

<style scoped>
.app {
  min-height: 100vh;
}
</style>