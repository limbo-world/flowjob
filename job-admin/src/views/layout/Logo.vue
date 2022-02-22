<script setup lang="ts">

defineProps({
    collapse: {
        type: Boolean,
        default() {
            return false;
        }
    }
})

</script>


<template>
    <div class="logo-container" :class="collapse ? 'logo-container__collapse' : ''">
        <el-image src="/src/assets/images/limbo.png" class="logo-image"></el-image>
        <span class="logo-title"> Limbo Group </span>
    </div>
</template>


<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({

});
</script>


<style scoped lang="scss">
.logo-container {
    height: 100%;

    display: flex;
    justify-content: flex-start;
    align-items: center;

    .logo-image {
        height: 40px;
        width: 40px;
    }

    .logo-title {
        width: 150px;
        margin-left: 20px;

        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        backface-visibility: hidden;
        transition: width .5s ease-in-out;
    }

    &.logo-container__collapse {
        justify-content: center;

        .logo-image {
            margin: 0 auto;
        }
        .logo-title {
            width: 0;
            margin-left: 0;
        }
    }
}
</style>