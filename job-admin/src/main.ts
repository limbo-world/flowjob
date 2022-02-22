import { createApp } from 'vue'
import 'animate.css'
import '/src/assets/styles/reset.scss'
import '/src/assets/styles/animations.scss'
import '/src/assets/styles/element/index.scss'

import App from './App.vue'

import Router from './libs/router/Router'
import Store from "./libs/stores/Store";

createApp(App)
    .use(Router)
    .use(Store)
    .mount('#app')
