<template>
    <div class="box">
        <VideoPannel :vobj="vobj" ref="video_pannel"/>
        <div class="list_box">
            <div class="list_col" v-for="(item,index) in list" :key="index.key">
                <div @click="show_pannel(item)">
                    <div><img v-lazy="item.img_url" alt=""></div>
                    <div class="title">{{ item.title}}</div>
                    <div class="people">{{item.people}}人购买</div>
                    <div class="clear"><br></div>
                </div>
            </div>
        </div>
        <div class="loading" v-show="isLoading">努力加载中。。。</div>
        <div class="clear"><br></div>
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


  import "muse-ui-loading/dist/muse-ui-loading.css"; // load css
  import Loading from "muse-ui-loading";

  Vue.use(Loading);


  export default {
    name: "ItemList",
    components: { VideoPannel },
    data() {
      return {
        list: {},
        isLoading: false,
        vobj: {},
        page_no: 1
      };
    },
    methods: {
      show_pannel(args) {
        this.vobj = args;
        this.$refs.video_pannel.show_pannel();
      },
      page(args) {
        var _this = this;
        console.log(args);


        var _page_no = this.$parent.page_no;
        console.log(_page_no);

        if (args == 1) {
          if (_page_no <= 0) {
            _page_no = 0;
          } else {
            _page_no = _page_no - 1;
          }
        } else if (args == 2) {
          _page_no = _page_no + 1;
        }
        this.$parent.page_no = _page_no;
        _this.$store.commit("upItemListPage", _page_no);

        if (!_this.isLoading) {
          _this.isLoading = true;
          this.$ajax.get(`/webapp/get_videoss_list`, {
            params: {
              begin: (_page_no * 10),
              sk: _this.$parent.sk
            }
          }).then(response => {

            _this.list = response.data;
            _this.isLoading = false;

            //document.body.scrollTop = document.documentElement.scrollTop = window.pageYOffset = 0;

          }).catch(function(error) {
            console.log("Error", error.message);
            _this.isLoading = false;

          });
        }

      },
      initPage() {
        var _this = this;
        if (!_this.isLoading) {
          _this.isLoading = true;
          this.$ajax.get(`/webapp/get_videoss_list`, {
            params: {
              begin: (0),
              sk: _this.$parent.sk,
              _t: new Date().getTime()
            }
          }).then(response => {

            _this.list = response.data;
            _this.isLoading = false;

            document.body.scrollTop = document.documentElement.scrollTop = window.pageYOffset = 0;

          }).catch(function(error) {
            console.log("Error", error.message);
            _this.isLoading = false;

          });
        }
      },
      onScroll() {
        var _this = this;
        //可滚动容器的高度
        let innerHeight = document.querySelector("#app").clientHeight;
        //屏幕尺寸高度
        let outerHeight = document.documentElement.clientHeight;
        //可滚动容器超出当前窗口显示范围的高度
        let scrollTop = document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop;
        //scrollTop在页面为滚动时为0，开始滚动后，慢慢增加，滚动到页面底部时，出现innerHeight < (outerHeight + scrollTop)的情况，严格来讲，是接近底部。
        if (innerHeight < (outerHeight + scrollTop + 30)) {
          //加载更多操作
          console.log("loadmore");
          //this.items += 10;

          if (!_this.isLoading) {
            _this.isLoading = true;
            this.$ajax.get(`/webapp/get_videoss_list`, {
              params: {
                begin: (this.page_no * 10),
                sk: _this.$parent.sk
              }
            }).then(response => {
              this.page_no = this.page_no + 1;
              response.data.forEach(function(e) {
                _this.list.push(e);
              });
              //_this.list = response.data;
              _this.isLoading = false;

              //document.body.scrollTop = document.documentElement.scrollTop = window.pageYOffset = 0;

            }).catch(function(error) {
              console.log("Error", error.message);
              _this.isLoading = false;

            });
          }


        }
      }
    },
    mounted() {
      this.initPage();
    },
    created() {
      window.addEventListener("scroll", this.onScroll);
    }
  };
</script>

<style lang="scss" scoped>

    .box {
        color: #000;

    }

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
            width: 100%;
            height: 4.3rem;
            padding-left: 0.1rem;
            padding-right: 0.1rem;
            position: relative;

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
            }
            .play_btn{
                position: absolute;
                width: 1.2rem;
                height: 1.2rem;
                top: 1rem;
                left: 1.9rem;
                img{
                    width: 1.2rem;
                    height: 1.2rem;
                }
            }
        }
    }

    .loading {
        font-size: 0.4rem;
        text-align: center;

    }

    .clear {
        clear: both;
    }

    .people{
        color: #f06292;
        padding-left: 0.1rem;
        padding-right: 0.1rem;
    }
</style>