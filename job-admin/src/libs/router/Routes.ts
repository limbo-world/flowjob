import {RouteRecordRaw} from "vue-router";

/**
 * 路由定义
 */
const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Root",
        component: () => import('/src/views/layout/Layout.vue'),
        children: [
            {
                path: "/home",
                name: "Home",
                component: () => import('/src/views/home/Home.vue')
            },
        ]
    },

    {
        path: "/login",
        name: "Login",
        component: () => import('/src/views/login/Login.vue')
    },
    {
        path: "/register",
        name: "Register",
        component: () => import('/src/views/login/Register.vue')
    }
];

export default routes;

/**
 * TODO 更新路由数据
 * @param auths 服务端API返回的权限数据
 */
export const updateRoutes = function (auths: Array<any>): Array<RouteRecordRaw> {
    return routes;
}