<script setup lang="ts">

import Sidebar from "@/widgets/Sidebar.vue";
</script>

<template>
  <div class="app-layout">
    <Sidebar />
    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
}
body {
  margin: 0;
  font-family: var(--font-sans, system-ui), sans-serif;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  background: var(--bg-app);
  color: var(--text);
}


.app-layout {
  display: flex;
  min-height: 100vh;
}

.app-content {
  flex: 1;
  min-width: 0;
  background: var(--bg-app);
}
</style>
