<template>
    <div>

        <div class="box">
            <div class="box_info">
                <div>微信支付</div>
            </div>

            <div class="box_btn_t">
                <div class="box_btn" @click="goBack()">
                    返回
                </div>
            </div>

            <div class="pannel1">
                <div class="close" @click="goBack()"><img src="/assets/img/close-cross.png"></div>

                <div class="btn" @click="f_pay()">确认支付</div>
            </div>

            <div class="cover"></div>
        </div>
        <a id="goto"></a>
    </div>
</template>

<script>
  /* eslint-disable no-undef */

  export default {
    name: "Pay",
    data() {
      return {
        is_pay: false,
        openid: "",
        vobj: {},
        pay_ing:false
      };
    },
    mounted: function() {


    },
    methods: {
      goBack() {
        document.getElementById("goto").href = "/?openid=" + this.openid;
        document.getElementById("goto").click();
      },
      f_pay(){
        var vid = this.$route.query.id;
        var openid = this.$route.query.openid;

        //this.loadVideoInfo(vid);
        if(!this.pay_ing){
          this.pay_ing = true;
          this.upOpenid(openid);
        }
      },
      upOpenid(openid) {
        var _this = this;
        if (typeof (this.cookie.get("uid")) != "undefined" && typeof(_this.$route.query.openid) != "undefined") {
          this.$ajax.get(`/webapp/up_user_openid`, {
            params: {
              uid: _this.cookie.get("uid"),
              openid: openid
            }
          }).then(response => {
            _this.$store.commit("upUserInfo", response.data);
            _this.$store.commit("upOpenId", response.data.openid);
            _this.openid = response.data.openid;

            _this.cookie.set("uid", response.data.id, 999);

            _this.pay();
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

        }).catch(function(error) {
          //alert(error);
        });
      },
      pay() {

        var _this = this;

        this.log("get_order", this.cookie.get("uid"));

        this.$ajax.get(`/webapp/pay_order_video`, {
          params: {
            uuid: _this.cookie.get("uid"),
            vid: this.$route.query.id
          }
        }).then(response => {

          if (response.data.code == "500" && !_this.is_order_err) {
            _this.pay();
          }

          this.pay_ing = false;

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
                  //_this.initVideo();


                  _this.$ajax.get(`/webapp/pay_web_notify`, {
                    params: {
                      key: response.data.data.out_trade_no
                    }
                  }).then(response => {

                    document.getElementById("goto").href = "/play?id=" + _this.$route.query.id + "&openid=" + _this.$route.query.openid;
                    document.getElementById("goto").click();

                  }).catch(function(error) {
                  });

                } else {
                  _this.goBack();
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

      }
    }
  };
</script>

<style lang="scss">
    html, body {
        margin: 0px;
        padding: 0px;
        height: 100%;
    }

    .wx_nav {
        position: fixed;
        height: 1.3rem;
        background-color: #4D4D4D;
        width: 100%;
        color: #ffffff;
        .wx_nav_btn {
            position: absolute;
            left: 0px;
            img {
                width: 0.8rem;
                height: 0.8rem;
                margin-top: 0.25rem;
                margin-left: 0.25rem;
            }

        }
        .wx_nav_title {
            margin-left: 1.3rem;
            div:first-child {
                font-size: 0.5rem;
                height: 0.7rem;
                line-height: 0.7rem;
            }
            div:last-child {
                font-size: 0.4rem;
                height: 0.4rem;
                line-height: 0.4rem;
                color: #AAAAAA;
            }
        }
    }

    .box {
        height: 100%;
        width: 100%;
        position: relative;
        .box_info {
            position: absolute;
            top: 2rem;
            width: 100%;
            text-align: center;
            div:first-child {
                margin-top: 0.6rem;
                font-size: 0.55rem;
            }
            div:last-child{
                margin-top: 0.1rem;
                font-size: 0.5rem;
            }
        }
        .box_shou {
            position: absolute;
            background-color: #ffffff;
            height: 1rem;
            width: 100%;
            line-height: 1rem;
            top: 7rem;
            div {
                font-size: 0.4rem;
            }
            div:first-child {
                float: left;
                margin-left: 0.3rem;
                color: #AAAAAA;
            }
            div:last-child {
                float: right;
                margin-right: 0.3rem;
            }
        }

        .box_btn_t {
            position: relative;
            width: 50%;
            top: 7.5rem;
            margin: auto;
            .box_btn {
                background-color: #1AAD19;
                height: 1.2rem;
                line-height: 1.2rem;
                color: #ffffff;
                text-align: center;
                font-size: 0.5rem;
                letter-spacing: 0.05rem;
                border-radius: 0.1rem;
                margin-right: 0.2rem;
                margin-left: 0.2rem;
            }
        }


        .cover {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgb(0, 0, 0, 0.6);
        }


        .pannel1 {
            background-color: #fff;
            position: fixed;
            margin:auto;
            left:0;
            right:0;
            top:0;
            bottom:0;
            width: 7.5rem;
            height: 7.5rem;
            z-index: 999999;

            border-radius: 0.3rem;


            .close{
                position: absolute;
                width: 0.6rem;
                height: 0.6rem;
                top:0.3rem;
                left: 0.3rem;

                img{
                    width: 100%;
                    height: 100%;
                }
            }
            .btn {
                position: absolute;
                margin: auto;
                bottom:0.5rem;
                left: 0;
                right: 0;
                background-color: #1AAD19;
                width: 4rem;
                height: 1.2rem;
                line-height: 1.2rem;
                color: #ffffff;
                text-align: center;
                font-size: 0.5rem;
                letter-spacing: 0.05rem;
                border-radius: 0.1rem;
            }

        }

    }
</style>