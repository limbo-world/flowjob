import { createStore } from "vuex";
import MenuStore from './MenuStore'

export default createStore({
    modules: {
        menu: MenuStore
    }
});