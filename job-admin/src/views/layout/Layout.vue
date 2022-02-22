<script setup lang="ts">
import { ref } from 'vue'
import { useStore } from "vuex";

// 直接在setup块中import组件，不需要在components属性中声明即可直接使用
import Logo from './Logo.vue'
import Menu from './Menu.vue'
import Header from './Header.vue'

// setup块中定义的const常量，可以直接使用，但由于const是常量无法再赋值，需要使用响应式常量
const asideCollapse = ref(false);

// 异步加载菜单
const menus = await useStore().dispatch('menu/loadMenus').then(ms => ms);
</script>

<template>
    <el-container id="page-layout">

        <el-aside class="layout-aside" :class="asideCollapse ? 'layout-aside__collapse' : ''">
            <el-container>

                <!-- Logo区 -->
                <el-header height="60px" class="layout-aside-header">
                    <Logo :collapse="asideCollapse" />
                </el-header>

                <!-- 菜单区 -->
                <el-main class="layout-aside-main">
                    <Menu :collapse="asideCollapse" :menus="menus" />
                </el-main>

            </el-container>
        </el-aside>

        <el-container>

            <!-- 顶部Header -->
            <el-header height="60px" class="layout-header">
                <Header :collapse="asideCollapse" @collapse="asideCollapse = !asideCollapse"></Header>
            </el-header>

            <el-main class="layout-main">

                <el-container class="layout-page-container">
                    <router-view></router-view>
                </el-container>

                <el-footer class="layout-footer"></el-footer>

            </el-main>


        </el-container>
    </el-container>
</template>


<script lang="ts">
import { defineComponent } from "vue";

defineComponent({
});
</script>


<style scoped lang="scss">
#page-layout {
    height: 100%;

    .layout-aside {
        // 尤大说这三个属性中，任意一个都可以开启动画时的硬件加速
        // perspective: 1000px;
        backface-visibility: hidden;
        //transform: translateZ(0);

        width: 250px;
        transition: width .5s ease-in-out;
        border-right: 1px solid var(--el-border-color-base);

        .el-container {
            height: 100%;
        }

        .layout-aside-header {
            //border-bottom: 1px solid var(--el-border-color-base);
        }

        .layout-aside-main {
            padding: 0;
        }

        &.layout-aside__collapse {
            width: 64px;

            .layout-aside-header {
                padding: 0;
            }
        }
    }

    .layout-header {
        border-bottom: 1px solid var(--el-border-color-base);
    }

    .layout-main {
        padding: 0;

        display: flex;
        flex-direction: column;
    }
}

</style>