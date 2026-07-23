import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import './assets/base.css'
import App from './App.vue'
import { LocalStorageGarbageCollector } from './services/LocalStorageGarbageCollector'

// CA-92: Init LocalStorage GC (Silent limit enforcer)
LocalStorageGarbageCollector.run();

import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import VueVirtualScroller from 'vue-virtual-scroller'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(VueVirtualScroller)

import i18n from './i18n'
app.use(i18n)

app.mount('#app')
