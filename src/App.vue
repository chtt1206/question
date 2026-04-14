<template>
  <router-view />
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
