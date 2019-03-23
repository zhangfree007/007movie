package com.marvel.m007.controller;


import com.alibaba.fastjson.JSON;
import com.marvel.m007.bean.BaseController;
import com.marvel.m007.bean.GLOBAL;
import com.marvel.m007.bean.RedisUtil;
import com.marvel.m007.bean.WebUtil;
import com.marvel.m007.service.WebAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/webapp")
public class WebAppController extends BaseController {

    @Autowired
    WebAppService webAppService;

    @Autowired
    RedisUtil redisUtil;

    @RequestMapping(value = "/login")
    @ResponseBody

    public String login(String cc, String uuid, HttpServletRequest request) {
        logger.info("host {}\tuid {}\tcc {}", request.getHeader("Host"), uuid, cc);
        return JSON.toJSONString(webAppService.login(cc, uuid));
    }

    @RequestMapping(value = "/up_user_info")
    @ResponseBody
    public String upUserInfo(String uuid) {
        return JSON.toJSONString(webAppService.login("", uuid));
    }



    @RequestMapping(value = "/get_bough_list")
    @ResponseBody
    public String getBoughList(String uuid) {
        return JSON.toJSONString(webAppService.getBoughList(uuid));
    }



    @RequestMapping(value = "/get_api_domain")
    @ResponseBody
    public String getApiDomain(HttpServletRequest request) {
        Object json = redisUtil.get(GLOBAL.REDIS.API_DOMAIN);
        if (json == null) {
            json = JSON.toJSONString(webAppService.getApiDomain(request.getHeader("Host")));
            redisUtil.set(GLOBAL.REDIS.API_DOMAIN, json, 30 * 60);
            return json.toString();
        } else {
            return json.toString();
        }
    }


    @RequestMapping(value = "/get_appkey")
    @ResponseBody
    public String getAppkey() {
        return JSON.toJSONString(webAppService.getAppkey());
    }

    @RequestMapping(value = "/up_user_openid")
    @ResponseBody
    public String upUserOpenId(String uid, String openid) {
        String id = webAppService.upUserOpenId(uid, openid);
        return JSON.toJSONString(webAppService.login("", id));
    }


    @RequestMapping(value = "/up_user_smi")
    @ResponseBody
    public String upUserSmi(String uid, String smi) {
        webAppService.upUserSmi(uid, smi);
        return JSON.toJSONString(webAppService.login("", uid));
    }


    @RequestMapping(value = "/pay_order_video")
    @ResponseBody
    public String payOrderVideo(String uuid, String vid) {
        return webAppService.payOrderVideo(uuid, vid);
    }


    @RequestMapping(value = "/pay_web_notify")
    @ResponseBody
    public String payWebNotify(String key) {
        try {
            webAppService.payWebNotify(key);
        } catch (Exception e) {
            logger.error("pay_notify error");
        }
        return "ok";
    }


    @RequestMapping(value = "/pay_notify")
    @ResponseBody
    public String payNotify(HttpServletRequest request) {
        logger.info(request.getParameter("out_trade_no"));
        logger.info(request.getParameter("order_sn"));
        logger.info(request.getParameter("status_text"));
        logger.info(request.getParameter("status"));
        logger.info(request.getParameter("attach"));

        try {
            webAppService.payOk(request.getParameter("out_trade_no"));
        } catch (Exception e) {
            logger.error("pay_notify error");
        }
        return "ok";
    }


    @RequestMapping(value = "/get_videoss_list")
    @ResponseBody
    public String getVideossList(String begin, String sk) {
        Object obj = redisUtil.get("getVideossList" + begin + sk);
        if (obj == null) {
            obj = JSON.toJSONString(webAppService.getVideossList(begin, sk));
            redisUtil.set("getVideossList" + begin + sk, obj.toString(), 10 * 60);
            return obj.toString();
        }
        return obj.toString();
    }


    @RequestMapping(value = "/get_bough_vss_list")
    @ResponseBody
    public String getBoughVssList(String uuid) {
        return JSON.toJSONString(webAppService.getBoughVssList(uuid));
    }


    @RequestMapping(value = "/get_videoss")
    @ResponseBody
    public String getVideoss(String id) {
        return JSON.toJSONString(webAppService.getVideoss(id));
    }


}
