import {createRouter, createWebHistory } from "vue-router";
import routes from "./Routes";

// 定义路由器
export const router = createRouter({
    history: createWebHistory(),
    routes
});
export default router;