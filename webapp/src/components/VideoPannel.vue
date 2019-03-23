<template>
    <div>
        <div class="pannel_box" id="pannel_box">
            <div class="pannel_close" @click="close_pannel()">
                <mu-button fab color="pink">
                    <div>关闭</div>
                </mu-button>
            </div>
            <div v-show="!is_pay"   @click="gotoVideo()">
                <div class="pannel_play_btn">
                    <img src="/assets/img/play_btn.png"/>
                </div>
                <div class="img_box"><img :src="vobj.img_url"></div>
                <div class="pannel_title">{{vobj.title}}</div>
                <div class="pannel_btn_box">
                    <div>
                        <div>
                            <mu-button full-width color="success" v-loading="loading1" >确认支付
                            </mu-button>
                        </div>
                    </div>
                    <div>
                        {{pay_info}}
                    </div>
                </div>
            </div>

            <div v-show="is_pay">
                <div><br><br></div>
                <div class="img_box">
                    <video id="myVideo" class="video-js vjs-big-play-centered vjs-fluid" x5-playsinline=""
                           playsinline=""
                           x-webkit-airplay="allow" style="object-fit:fill">
                        <p class="vjs-no-js">
                            To view this video please enable JavaScript, and consider upgrading to a
                            web browser that
                            <a href="https://videojs.com/html5-video-support" target="_blank">
                                supports HTML5 video
                            </a>
                        </p>
                    </video>
                </div>
                <div class="pannel_title">{{vobj.title}}</div>
            </div>
        </div>
        <PayPannel v-if="show_pay_pannel"></PayPannel>
        <a id="goto"></a>
    </div>
</template>

<script>
  /* eslint-disable no-undef */

  import Vue from "vue";
  import "muse-ui/lib/styles/base.less";
  import { Icon } from "muse-ui";
  import "muse-ui/lib/styles/theme.less";

  Vue.use(Icon);


  import "muse-ui-loading/dist/muse-ui-loading.css"; // load css
  import Loading from "muse-ui-loading";
  import PayPannel from "./PayPannel";

  Vue.use(Loading);


  export default {
    name: "VideoPannel",
    components: { PayPannel },
    props: {
      vobj: {}
    },
    data() {
      return {
        is_pay: false,
        loading1: false,
        player: null,
        pay_info: "",
        is_order_err: false,
        show_pay_pannel:false,
        pay_ing:false
      };
    },
    mounted() {
    },
    methods: {
      close_show_pay_pannel(){
        this.show_pay_pannel = false;
      },
      gotoVideo() {
        var _this = this;
        //this.$router.push({ name: "Play", params: { obj: this.vobj } });

        //this.$router.push({ name: "IPayPage", params: { obj: this.vobj } });


        _this.loading1 = true;
        setTimeout(function() {
          _this.loading1 = false;
        }, 5000);

        if (typeof(this.$store.state.openid) == "undefined" || this.$store.state.openid == "") {

          let isPay = false;
          this.$store.state.user_info.vlist.forEach(function(e) {
            /*if (e.vid.toString() == _this.vobj.id) {
              isPay = true;
            }*/
            isPay = true;
          });

          if (isPay) {
            //this.$router.push({ name: "Play", params: { obj: this.vobj } });
            //this.pay();
            console.log("==");
          } else {

            _this.requestOpenId();

          }

        } else {

          this.show_pay_pannel = true;
        }

      },
      requestOpenId() {
        var _this = this;
        this.$ajax.get(`/webapp/up_user_smi`, {
          params: {
            uid: _this.cookie.get("uid"),
            smi: _this.$store.state.app_key.sub_mch_id
          }
        }).then(response => {
          _this.$store.commit("upUserInfo", response.data);

          let protocol = window.location.protocol; //协议
          let host = window.location.host; //主机
          let url = "";
          let cburl = protocol + "//" + host + "/pay.html?id=" + _this.vobj.id + "&p=" + this.$parent.$parent.page_no + "&sk=" + this.$parent.$parent.sk;
          cburl = encodeURIComponent(cburl);


            url = _this.$store.state.app_key.pay_api_url + "/v2/wechat/login?app_key=" + _this.$store.state.app_key.app_key +
              "&sub_mch_id=" + _this.$store.state.app_key.sub_mch_id + "&snsapi_userinfo=false&callback=" + cburl;


          document.getElementById("goto").href = url;
          document.getElementById("goto").click();

        }).catch(function(error) {
        });


      },
      close_pannel() {

        try {
          this.player.pause();  //暂停
          //this.player.dispose();  //销毁
        } catch (e) {
          console.log("err");
        }

        document.getElementById("pannel_box").style.display = "none";
      },
      show_pannel() {
        this.is_pay = false;
        var _this = this;
        this.asyncLoadJs("/assets/js/video.min.js", function() {
          console.log("vjs完成");
          _this.isPay();
        });

        document.getElementById("pannel_box").style.display = "inline";
      },
      isPay() {

        var _this = this;
        console.log(this.$store.state.user_info.vlist);
          _this.pay_info = "资费 " + _this.vobj.pay_money + " 元";
        if (this.$store.state.user_info.vlist.length > 0) {
          this.$store.state.user_info.vlist.forEach(function(item) {
            _this.is_pay = true;
            _this.initVideo();
          });
        }
      },
      initVideo() {
        var _this = this;
        console.log(this.vobj);
        var _poster = this.vobj.img_url;

        if (_this.player == null) {

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
            });


            this.on("error", function() {// 因为网络加载失败时 重新尝试
              console.log("加载失败");
            });

          });

        }


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

      },
      pay() {


        var _this = this;

        this.$ajax.get(`/webapp/pay_order_video`, {
          params: {
            uuid: _this.cookie.get("uid"),
            vid: _this.vobj.id
          }
        }).then(response => {
          var _this = this;
          if (response.data.code == "500" && !_this.is_order_err) {
            _this.is_order_err = true;

            this.$ajax.get(`/webapp/get_appkey?t=` + new Date().getTime()).then(response => {
              _this.$store.commit("upAppKey", response.data);

              if (_this.$store.state.user_info.sub_mch_id == response.data.sub_mch_id) {
                if (!_this.pay_ing) {
                  _this.pay_ing = true;
                  _this.pay();
                }
              } else {
                _this.requestOpenId();
              }
            }).catch(function(error) {
            });

          }

          if (_this.$store.state.user_info.sub_mch_id == response.data.sub_mch_id) {
            console.log("");
          } else {
            _this.requestOpenId();
          }
          _this.pay_ing = false;
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

        this.log("pay_order", _this.vobj.id);
      }
    }
  };
</script>
<style scoped>
    @import '/assets/css/video-js.min.css';
</style>
<style lang="scss">
    .pannel_box {
        display: none;
        z-index: 99999;
        position: fixed;
        width: 100%;
        bottom: 0px;
        background-color: #fff;
        padding: 0.3rem;
        .img_box {
            img {
                width: 100%;
                height: 6rem;
            }
        }
        .pannel_title {
            color: #2196f3;
            font-size: 0.5rem;
            text-align: center;
        }
        .pannel_btn_box {
            font-size: 0.4rem;
            text-align: center;
            color: #000;
            > div {
                margin: 0.2rem;
            }
            .mu-raised-button {
                height: 1.3rem;
            }
            .mu-button-wrapper {
                font-size: 0.5rem;
            }
        }

        .pannel_close {
            position: absolute;
            right: 0.2rem;
            .mu-fab-button {
                width: 0.8rem;
                height: 0.8rem;
            }
        }

        .bj {
            height: 100%;
            width: 100%;
            background-color: black;
        }

        .pannel_play_btn {
            position: absolute;
            top: 2.3rem;
            left: 4rem;
            margin: 0 auto;

            width: 2rem;
            height: 2rem;
            img {

                width: 2rem;
                height: 2rem;
            }
        }

    }
</style>