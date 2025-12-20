import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as Icons from '@element-plus/icons-vue';
import 'element-plus/theme-chalk/dark/css-vars.css'

const app = createApp(App)

axios.defaults.baseURL="http://123.57.82.189:9001/nullbot"

Object.keys(Icons).forEach(key => {  
   app.component(key, Icons[key]);  
}); 

app.use(router)
   .use(ElementPlus)
   .mount('#app')
   
app.config.globalProperties.$axios = axios
