<template>
    <div id="app">
        <div class="nav">
            <mu-appbar style="width: 100%;" v-show="appbar_show">
                <mu-button flat slot="left" @click="goBack()">&lt; 返回</mu-button>
            </mu-appbar>

            <div class="btn_box" v-show="!appbar_show">
                <mu-paper class="btn_paper" circle :z-depth="10" @click="selectList('动作')">动作</mu-paper>
                <mu-paper class="btn_paper" circle :z-depth="10" @click="selectList('喜剧')">喜剧</mu-paper>
                <mu-paper class="btn_paper" circle :z-depth="10" @click="selectList('爱情')">爱情</mu-paper>
                <mu-paper class="btn_paper" circle :z-depth="10" @click="selectList('科幻')">科幻</mu-paper>
                <mu-paper class="btn_paper" circle :z-depth="10" @click="selectList('恐怖')">恐怖</mu-paper>
            </div>
            <a id="goto"></a>
        </div>
        <div class="chip_box" v-show="!appbar_show">
            <mu-chip class="chip" color="info" @click="selectList('剧情片')">
                剧情片
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('战争片')">
                战争片
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('韩国剧')">
                韩国剧
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('国产剧')">
                国产剧
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('香港剧')">
                香港剧
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('内地综艺')">
                内地综艺
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('港台综艺')">
                港台综艺
            </mu-chip>
            <mu-chip class="chip" color="info" @click="selectList('日韩动漫')">
                日韩动漫
            </mu-chip>
        </div>

        <router-view v-if="isRouterAlive"/>

    </div>
</template>


<script>

  import Vue from "vue";
  import "muse-ui/lib/styles/base.less";
  import { Paper, Chip, Button, AppBar } from "muse-ui";
  import "muse-ui/lib/styles/theme.less";


  Vue.use(Paper);
  Vue.use(Chip);
  Vue.use(Button);
  Vue.use(AppBar);

  export default {
    name: "main",
    data() {
      return {
        size: 3,
        appbar_show: false,
        page_no: 1,
        sk: "",
        isRouterAlive: true,
        f_list:{}
      };
    },
    computed: {},
    watch: {},
    provide() {
      return {
        reload: this.reload
      };
    },
    methods: {
      reload() {
        this.isRouterAlive = false;
        this.$nextTick(function() {
          this.isRouterAlive = true;
        });
      },
      goBack() {
        console.log("goBack");
        this.closeAppbar();

        console.log(this.$route);
        if (this.$route.name == "Play" && typeof (this.$route.query.id) != "undefined") {
          this.$router.push({ path: "/" });
        } else {
          this.$router.go(-1);
        }
      },
      showAppbar() {
        this.appbar_show = true;
      },
      closeAppbar() {
        this.appbar_show = false;
      },
      selectList(sk) {
        this.page_no = 0;
        this.sk = sk;
        console.log(this.$route);
        if (this.$route.name == "ItemList") {
          this.reload();
        } else {
          this.$router.push({ path: "/" });
        }
      },
      boughtList() {
        this.$router.push({ path: "/bought_list" });
      }
    },
    mounted() {
      //this.$router.push("/");//返回上一层

      var _this = this;


      var uid = this.cookie.get("uid");
      if (typeof (uid) == "undefined") {
        uid = "";
      }


      this.$ajax.get(`/webapp/get_appkey?t=` + new Date().getTime()).then(response => {
        _this.$store.commit("upAppKey", response.data);
      }).catch(function(error) {
      });

      if (typeof (this.$store.state.user_info.id) == "undefined") {

        this.$ajax.get(`/webapp/login`, { params: {  uuid: uid } }).then(response => {
          if (response.data.vlist.length > 0) {
            _this.$store.commit("upBoughtShow", true);
          }
          _this.$store.commit("upUserInfo", response.data);
          //this.$store.commit("upOpenId", response.data.openid);

          _this.cookie.set("uid", response.data.id, 999);

          if (uid == "") {
            this.log("login", "新");
          } else {
            this.log("login", "老");
          }
        });

        if (this.getUrlKey("openid")) {
          if (this.getUrlKey("openid") != "") {
            _this.$store.commit("upOpenId", this.getUrlKey("openid"));
          }
        }
      }


    }

  };
</script>


<style lang="scss">
    html, body {
        margin: 0px;
        padding: 0px;
        color: #000;
    }

    .btn_box {
        font-size: 0.6rem;
        height: 2.3rem;

        padding-left: 0.2rem;
        padding-top: 0.3rem;

        .btn_paper {
            float: left;
            width: 1.8rem;
            height: 1.8rem;
            line-height: 1.8rem;
            text-align: center;
            margin-left: 0.1rem;
            background-image: url("/assets/img/bt1.png");
            background-repeat: no-repeat; //不重复
            background-size: 100% 100%; // 满屏
            color: #000;
        }
    }

    .chip_box {
        padding: 0.2rem;
        padding-top: 0.1rem;

        .chip {
            margin: 0.05rem;
            font-size: 0.4rem;
            height: 0.8rem;
            vertical-align: middle;
        }
    }

    .mu-appbar {
        height: 1.2rem;

        .mu-flat-button {
            font-size: 0.6rem;
        }
    }

</style>
