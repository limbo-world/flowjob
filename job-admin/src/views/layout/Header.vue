<script setup lang="ts">
import { Fold } from "@element-plus/icons-vue";

defineProps({
    collapse: {
        type: Boolean,
        default() {
            return false;
        }
    }
});

const collapseEmit = defineEmits<{
    (e: 'collapse'): void
}>();

</script>

<template>
    <div class="header-container">

        <el-icon @click="collapseEmit('collapse')" class="expand-icon">
            <fold :class="collapse ? 'expand__collapse' : ''" />
        </el-icon>

    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({});
</script>

<style scoped lang="scss">
.header-container {
    height: 100%;

    display: flex;
    align-items: center;

    .expand-icon {
        font-size: 1.5em;
        cursor: pointer;

        .icon {
            backface-visibility: hidden;
            transform: rotate(0);
            transition: transform .5s ease-in-out;

            &.expand__collapse {
                transform: rotate(180deg);
            }
        }
    }
}

</style>