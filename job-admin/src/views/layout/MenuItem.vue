<script setup lang="ts">
import { Location } from "@element-plus/icons-vue";
import {computed, ref, toRefs} from "vue";
import { MenuState } from "../../libs/stores/MenuStore";

const props = defineProps<{
    menu: MenuState
}>();

const { menu } = toRefs(props);
const hasChildren = computed(() => menu.value.children && menu.value.children.length > 0)
</script>


<template>

    <!-- 没有子菜单时 -->
    <el-menu-item v-if="!hasChildren" :index="menu.menuId" :route="menu.menuRoute">
        <el-icon>
            <location />
        </el-icon>
        <span>{{menu.menuName}}</span>
    </el-menu-item>

    <!-- 有子菜单时 -->
    <el-sub-menu v-else :index="menu.menuId">
        <!-- 菜单名称、图标 -->
        <template #title>
            <el-icon>
                <location />
            </el-icon>
            <span>{{menu.menuName}}</span>
        </template>

        <!-- 子菜单 -->
        <template v-for="subMenu in menu.children">
            <MenuItem :menu="subMenu" />
        </template>
    </el-sub-menu>
</template>


<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
    name: 'MenuItem'
});
</script>


<style scoped lang="scss">

</style>