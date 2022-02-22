import { Module} from "vuex";

/**
 * 菜单的数据格式
 */
export interface MenuState {

    /**
     * 菜单ID
     */
    menuId: string;

    /**
     * 菜单名
     */
    menuName: string;

    /**
     * 菜单图标
     */
    menuIcon?: string;

    /**
     * 菜单对应的路由地址
     */
    menuRoute?: string;

    /**
     * 子菜单
     */
    children?: Array<MenuState>;

    /**
     * 父菜单ID
     */
    parentMenuId?: string;

}


/**
 * 声明菜单存储数据
 */
const menuStore: Module<Array<MenuState>, any> = {
    namespaced: true,

    state () {
        return [];
    },

    mutations: {
        setMenu(state, menus: Array<MenuState>) {
            state = menus;
        },
    },


    actions: {

        /**
         * 异步加载菜单
         */
        loadMenus (context) {
            return Promise.resolve([
                {
                    menuId: 'A001',
                    menuName: '一级菜单A',
                    menuIcon: '',
                    menuRoute: '',
                    children: [
                        {
                            menuId: 'A001001',
                            menuName: '二级菜单A-1',
                            menuIcon: '',
                            menuRoute: '',
                            parentMenuId: 'A001'
                        },
                        {
                            menuId: 'A001002',
                            menuName: '二级菜单A-2',
                            menuIcon: '',
                            menuRoute: '',
                            children: [
                                {
                                    menuId: 'A001001001',
                                    menuName: '三级菜单A-1-1',
                                    menuIcon: '',
                                    menuRoute: '',
                                    children: [
                                        {
                                            menuId: 'A001001001001',
                                            menuName: '四级菜单A-1-1-1',
                                            menuIcon: '',
                                            menuRoute: '',
                                            parentMenuId: 'A001001001'
                                        }
                                    ],
                                    parentMenuId: 'A001002'
                                },
                                {
                                    menuId: 'A001001002',
                                    menuName: '三级菜单A-1-2',
                                    menuIcon: '',
                                    menuRoute: '',
                                    parentMenuId: 'A001002'
                                },
                            ],
                            parentMenuId: 'A001'
                        },
                    ],
                }
            ])
                .then((menus: Array<MenuState>) => {
                    console.log('加载菜单完成', menus);
                    context.commit('setMenu', menus);
                    return menus;
                });
        }
    }
};

export default menuStore;