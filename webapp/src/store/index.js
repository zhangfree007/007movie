import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    is_show_bought: false,
    user_info: {},
    openid: "",
    app_key: {},
    item_list_page: 0,
    appbar_show: false
  },
  mutations: {
    upBoughtShow(state, params) {
      state.is_show_bought = params;
    },
    upUserInfo(state, params) {
      state.user_info = params;
    },
    upApiDomain(state, params) {
      state.api_domain = params;
    },
    upOpenId(state, params) {
      state.openid = params;
    },
    upAppKey(state, params) {
      state.app_key = params
    },
    upItemListPage(state, params) {
      state.item_list_page = params
    },
    upAppbarShow(state, params) {
      state.appbar_show = params
    }
  },
  actions: {},
  modules: {}
});
