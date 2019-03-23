<template>
    <div class="pay_pannel_box" id="pay_pannel_box">
        <div class="box_info">
            <div>微信支付</div>
            <div></div>
        </div>

        <div class="pannel1">
            <div class="close" @click="close()"><img src="/assets/img/close-cross.png"></div>


            <div class="btn" @click="f_pay()">确认支付</div>
        </div>

        <div class="cover"></div>
    </div>
</template>

<script>
  export default {
    name: "PayPannel",
    methods: {
      f_pay(){
        this.$parent.pay();
      },
      close(){
        this.$parent.close_show_pay_pannel();
      }
    }
  };
</script>

<style scoped lang="scss">
    .pay_pannel_box {
        z-index: 99999;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: #fff;
        .box_info {
            color: #000;
            text-align: center;
            margin-top: 2rem;
            font-size: 0.5rem;
            div:first-child {
                margin-top: 0.6rem;
                font-size: 0.55rem;
            }
            div:last-child{
                margin-top: 0.1rem;
                font-size: 0.5rem;
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