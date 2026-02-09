<script lang="ts">
import { defineComponent } from 'vue'
import Header from '@/widgets/Header.vue'

export default defineComponent({
  name: 'DirectoriesStartPage',
  components: { Header },
})
</script>

<template>
  <div>
    <Header title="Справочники" />
    <h2 class="start">Выберите справочник в сайдбаре или создайте новый.</h2>
  </div>
</template>

<style>
.start {
  padding: 10px;
  text-align: center;
}
</style>