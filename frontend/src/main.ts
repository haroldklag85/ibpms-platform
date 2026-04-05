import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import './assets/base.css'
import App from './App.vue'
import { LocalStorageGarbageCollector } from './services/LocalStorageGarbageCollector'

// CA-92: Init LocalStorage GC (Silent limit enforcer)
LocalStorageGarbageCollector.run();

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
