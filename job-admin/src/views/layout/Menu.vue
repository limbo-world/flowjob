<script setup lang="ts">
import MenuItem from "./MenuItem.vue";
import { MenuState } from "../../libs/stores/MenuStore";

interface Props {
    collapse: boolean,
    menus: Array<MenuState>
}

// 声明组件接收属性和默认值
withDefaults(defineProps<Props>(), {
    collapse: false,
    menus: () => []
});
</script>

<template>
    <el-menu :collapse="collapse" class="layout-menu">
        <template v-for="menu in menus">
            <MenuItem :menu="menu" />
        </template>
    </el-menu>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({

});
</script>

<style scoped lang="scss">


.layout-menu {
    border-right: none;

    ::v-deep .el-sub-menu__title {
        //padding-right: 10px;
    }

    ::v-deep .el-menu-item {
        height: 40px;
        line-height: 40px;
    }
}
</style>