import Vue from "vue";
import VueRouter from "vue-router";


Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "ItemList",
    component: () => import('@/views/ItemList.vue')
  },
  {
    path: "/play",
    name: "Play",
    component: () => import('@/views/Play.vue')
  },
  {
    path: "/bought_list",
    name: "BoughtList",
    component: () => import('@/views/BoughtList.vue')
  }


];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;