<script setup lang="ts">
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup
</script>

<template>
    <!-- 子组件中要在setup块使用await，需要在父组件使用子组件的地方，使用 <suspense> 标签将异步组件包起来 -->
    <suspense>
        <router-view></router-view>
    </suspense>
</template>

<style scoped lang="scss">
</style>
