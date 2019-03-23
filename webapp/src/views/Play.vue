<template>
    <div>
        <div v-show="!is_pay" class="pay_btn" @click="pay()">
            <div>
                <img src="/assets/img/play_btn.png"/>
            </div>
            <img :src="vobj.img_url">
            <div class="title">{{vobj.title}}</div>
        </div>
        <div v-show="is_pay">
            <video id="myVideo" class="video-js vjs-big-play-centered vjs-fluid" x5-playsinline="" playsinline=""
                   x-webkit-airplay="allow" style="object-fit:fill">
                <p class="vjs-no-js">
                    To view this video please enable JavaScript, and consider upgrading to a
                    web browser that
                    <a href="https://videojs.com/html5-video-support" target="_blank">
                        supports HTML5 video
                    </a>
                </p>
            </video>

            <div class="go_back_btn">
                <mu-button full-width  color="primary" @click="goBack()">进入已购</mu-button>
            </div>
        </div>
    </div>
</template>

<script>
  /* eslint-disable no-undef */

  import Vue from "vue";
  import "muse-ui/lib/styles/base.less";
  import {  Button} from "muse-ui";
  import "muse-ui/lib/styles/theme.less";

  Vue.use(Button);

  export default {
    name: "Play",
    data() {
      return {
        is_pay: false,
        vobj: { title: "加载中...", img_url: "/assets/img/0.jpg" },
        player: {},
        vid: ""
      };
    },
    mounted: function() {
      var _this = this;
      _this.vid = this.$route.query.id;
      var openid = this.$route.query.openid;

      //this.$parent.showAppbar();

      this.asyncLoadJs("/assets/js/video.min.js", function() {
        console.log("vjs完成");
        console.log(_this.vid);
        if (typeof (_this.vid) == "undefined") {
          _this.vobj = _this.$route.params.obj;
          _this.isPay();
        } else {
          _this.$parent.page_no = parseInt(_this.$route.query.p);
          _this.$parent.sk = _this.$route.query.sk;
          _this.loadVideoInfo(_this.vid);
          _this.upOpenid(openid);
        }
      });

    },
    methods: {
      goBack() {
        this.$router.push({ path: "/bought_list" });
      },
      upOpenid(openid) {
        if (typeof (this.cookie.get("uid")) != "undefined" && typeof(this.$route.query.openid) != "undefined") {
          this.$ajax.get(`/webapp/up_user_openid`, {
            params: {
              uid: this.cookie.get("uid"),
              openid: openid
            }
          }).then(response => {
            this.$store.commit("upUserInfo", response.data);
            this.$store.commit("upOpenId", response.data.openid);
          }).catch(function(error) {
            //alert(error);
          });
        }
      },
      loadVideoInfo(vid) {
        var _this = this;
        this.$ajax.get(`/webapp/get_videoss`, {
          params: {
            id: vid
          }
        }).then(response => {
          _this.vobj = response.data;
          _this.isPay();

        }).catch(function(error) {
          //alert(error);
        });
      },
      isPay() {
        console.log("isPay");
        var _this = this;
        console.log(this.$store.state.user_info.vlist);

        _this.$ajax.get(`/webapp/up_user_info`, {
          params: {
            uuid: _this.cookie.get("uid")
          }
        }).then(rr => {
          _this.$store.commit("upUserInfo", rr.data);

          if (rr.data.vlist.length > 0) {
            rr.data.vlist.forEach(function(item) {
              if (_this.vid == item.vid) {
                _this.is_pay = true;
                _this.initVideo();
              }
            });
          }

        }).catch(function(error) {
        });


      },
      pay() {

        var _this = this;
        console.log("======");

        this.$ajax.get(`/webapp/pay_order_video`, {
          params: {
            uuid: _this.cookie.get("uid"),
            vid: _this.vobj.id
          }
        }).then(response => {
          var _this = this;


          function onBridgeReady() {
            WeixinJSBridge.invoke(
              "getBrandWCPayRequest", {
                "appId": response.data.data.jsapi_app_id, //公众号名称，由商户传入
                "timeStamp": response.data.data.jsapi_timeStamp, //时间戳，自1970年以来的秒数
                "nonceStr": response.data.data.jsapi_nonceStr, //随机串
                "package": response.data.data.jsapi_package,
                "signType": "MD5",
                "paySign": response.data.data.jsapi_paySign //微信签名
              },
              function(res) {

                if (res.err_msg == "get_brand_wcpay_request:ok") {
                //alert("支付成功");
                _this.is_pay = true;
                _this.initVideo();

                _this.$store.commit("upBoughtShow", true);


                _this.$ajax.get(`/webapp/pay_web_notify`, {
                  params: {
                    key: response.data.data.out_trade_no
                  }
                }).then(response => {

                  _this.$ajax.get(`/webapp/up_user_info`, {
                    params: {
                      uuid: _this.cookie.get("uid")
                    }
                  }).then(rr => {
                    _this.$store.commit("upUserInfo", rr.data);
                  }).catch(function(error) {
                  });

                }).catch(function(error) {
                });

                }
              });
          }

          if (typeof WeixinJSBridge == "undefined") {
            if (document.addEventListener) {
              document.addEventListener("WeixinJSBridgeReady", onBridgeReady, false);
            } else if (document.attachEvent) {
              document.attachEvent("WeixinJSBridgeReady", onBridgeReady);
              document.attachEvent("onWeixinJSBridgeReady", onBridgeReady);
            }
          } else {
            onBridgeReady();
          }
        }).catch(function(error) {
          //alert(error);
        });

      },
      initVideo() {
        var _this = this;
        var _poster = this.vobj.img_url;

        _this.player = videojs(document.getElementById("myVideo"), {
          controls: true, // 是否显示控制条
          poster: _poster, // 视频封面图地址
          preload: "auto",
          autoplay: true,
          fluid: true, // 自适应宽高
          language: "zh-CN", // 设置语言
          muted: false, // 是否静音
          inactivityTimeout: false,
          controlBar: {
            children: [
              { name: "playToggle" }, // 播放按钮
              { name: "currentTimeDisplay" }, // 当前已播放时间
              { name: "progressControl" }, // 播放进度条
              { name: "durationDisplay" }, // 总时间
              { // 倍数播放
                name: "playbackRateMenuButton",
                "playbackRates": [0.5, 1, 1.5, 2, 2.5]
              },
              {
                name: "volumePanel", // 音量控制
                inline: false // 不使用水平方式
              },
              { name: "FullscreenToggle" } // 全屏
            ]
          }
        }, function() {
          console.log("初始化完成");

          this.on("play", function() {
            console.log("开始播放");
            _this.log("v_play", _this.vid);
          });


          this.on("error", function() {// 因为网络加载失败时 重新尝试
            console.log("加载失败");
            _this.log("v_play_error", _this.vid + "," + _this.cookie.get("video_link"));
          });

        });

        let vobj = {};
        vobj.src = _this.vobj.m3u8_url;
        if (vobj.src.toString().indexOf("mp4") > -1) {
          vobj.type = "video/mp4";
        } else {
          vobj.type = "application/x-mpegURL";
        }

        _this.player.src(vobj);
        _this.player.load();
        _this.player.play();
      }
    }
  };
</script>
<style scoped>
    @import '/assets/css/video-js.min.css';
</style>
<style lang="scss" scoped>
    html, body {
        margin: 0px;
        padding: 0px;
    }

    .pay_btn {
        position: relative;
        min-height: 3rem;
        > img {
            width: 100%;
            height: 6rem;
        }

        > div {
            img {
                position: absolute;
                width: 2rem;
                height: 2rem;
                top: 30%;
                left: 40%;
            }
        }
        .title {
            font-size: 0.5rem;
            padding: 0 0.2rem 0 0.2rem;
        }
    }

    .go_back_btn{
        width: 100%;
        margin-top: 1rem;
        padding-left: 2rem;
        padding-right: 2rem;
    }
</style>