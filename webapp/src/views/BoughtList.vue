<template>
    <div>
        <VideoPannel :vobj="vobj" ref="video_pannel"/>
        <div class="list_box">
            <div class="list_col" v-for="(item,index) in list" :key="index.key">
                <div @click="show_pannel(item)">
                    <div><img v-lazy="item.img_url" alt=""></div>
                    <div class="title">{{ item.title}}</div>
                    <div class="clear"><br></div>
                </div>
            </div>
        </div>

        <div class="clear"><br></div>
        <div class="page_btn">
            <mu-container class="demo-container">
                <mu-row gutter>
                    <mu-col span="6">
                        <div class="page_btn_b">
                            <mu-button round color="success" v-loading="isLoading" @click="page(1)">上一页</mu-button>
                        </div>
                    </mu-col>
                    <mu-col span="6">
                        <div class="page_btn_b">
                            <mu-button round color="success" v-loading="isLoading" @click="page(2)">下一页</mu-button>
                        </div>
                    </mu-col>
                </mu-row>
            </mu-container>
        </div>
    </div>
</template>

<script>


  import Vue from "vue";
  import "muse-ui/lib/styles/base.less";
  import { Button, Grid } from "muse-ui";
  import "muse-ui/lib/styles/theme.less";
  import VideoPannel from "../components/VideoPannel";

  Vue.use(Button);
  Vue.use(Grid);

  import 'muse-ui-loading/dist/muse-ui-loading.css'; // load css
  import Loading from 'muse-ui-loading';
  Vue.use(Loading);

  export default {
    name: "BoughtList",
    components: { VideoPannel },
    data() {
      return {
        list: {},
        isLoading: false,
        vobj: {}
      };
    },
    methods: {
      show_pannel(args) {
        this.vobj = args;
        this.$refs.video_pannel.show_pannel();
      },
      initPage() {
        var _this = this;
        console.log("bought_list");
        if (!_this.isLoading) {
          _this.isLoading = true;
          this.$ajax.get(`/webapp/get_bough_vss_list`, {
            params: {
              uuid: _this.cookie.get("uid")
            }
          }).then(response => {

            _this.list = response.data;
            _this.isLoading = false;

            document.body.scrollTop = document.documentElement.scrollTop = window.pageYOffset = 0;

            document.getElementById("load_img").style.display = "none";

          }).catch(function(error) {
            console.log("Error", error.message);
            _this.isLoading = false;

          });
        }
      }
    },
    mounted() {
      this.initPage();
    }
  };
</script>

<style lang="scss" scoped>

    .page_btn {
        .page_btn_b {
            margin: auto;
            width: 3rem;
            margin-bottom: 1rem;
        }
        .mu-raised-button {
            width: 100%;
            height: 0.8rem;
            font-size: 0.5rem;
        }
    }


    .list_box {
        .list_col {
            float: left;
            width: 50%;
            height: 4.3rem;
            padding-left: 0.1rem;
            padding-right: 0.1rem;
            img {
                width: 100%;
                height: 3rem;
            }
            .title {
                padding-left: 0.1rem;
                padding-right: 0.1rem;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                white-space: normal;
                color: #000;
            }
        }
    }

    .clear {
        clear: both;
    }
</style>