import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
    css: {
        preprocessorOptions: {
            scss: {
                additionData: './src/assets/styles/reset.scss'
            }
        }
    },

    plugins: [
        vue(),

        // 按需导入ElementPlus的组件
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
    ]
})
