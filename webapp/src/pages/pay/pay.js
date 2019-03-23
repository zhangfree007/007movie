import Vue from "vue";
import App from "./Pay.vue";
import router from "../../router";
import store from "../../store";

import 'lib-flexible/flexible.js'
import util from "../../assets/js/util";

import axios from "axios";



let protocol = window.location.protocol; //协议
let host = window.location.host; //主机

Vue.prototype.Data = {};
Vue.prototype.Data.api_url = protocol + "//" + host;
//Vue.prototype.Data.api_url = protocol + "//localhost";

axios.defaults.baseURL = Vue.prototype.Data.api_url;
axios.defaults.withCredentials = true;


Vue.prototype.$ajax = axios;

Vue.use(util);

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#pay");
