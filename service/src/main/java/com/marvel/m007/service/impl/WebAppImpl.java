package com.marvel.m007.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.marvel.m007.bean.*;
import com.marvel.m007.service.WebAppService;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WebAppImpl extends BaseService implements WebAppService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OkHttpCli okHttpCli;

    public Map login(String cc, String uuid) {
        if (!uuid.equals("")) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM tb_user_info t WHERE t.id = ?", uuid);

            /*if (cc != null && !cc.equals("")) {
                jdbcTemplate.update("UPDATE tb_user_info SET channel_code = ? WHERE id = ?", cc, uuid);
            }*/

            if (list.size() > 0) {
                list.get(0).put("vlist", jdbcTemplate.queryForList("SELECT * FROM tb_user_pay t WHERE t.uid = ? AND (t.pay_status & 2 = 2 OR t.pay_status & 4 = 4)", uuid));
                return list.get(0);
            }
        } else {
            uuid = UUID.randomUUID().toString().replaceAll("-", "");
        }


        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ua = request.getHeader("User-Agent");

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.indexOf(",") > -1) {
            String[] ips = ip.split(",");
            ip = ips[ips.length - 1].trim();
        }

        Date dd = new Date();


        jdbcTemplate.update("INSERT INTO tb_user_info (id,create_date,ua,ip,`type`,`status`,channel_code) VALUES (?,?,?,?,?,?,?)",
                uuid, dd, ua, ip, 1, 1, cc == "" ? "无" : cc);

        Map map = new HashMap();
        map.put("id", uuid);
        map.put("create_date", dd);
        map.put("ua", ua);
        map.put("ip", ip);
        map.put("type", 1);
        map.put("status", 1);
        map.put("channel_code", cc);
        map.put("vlist", new ArrayList<>());

        return map;
    }


    @Override
    public String payOrderVideo(String uuid, String vid) {

        Map uMap = jdbcTemplate.queryForMap("SELECT u.*,p.app_key,p.app_secret,p.`type`,p.pay_api_url,p.`class` FROM tb_user_info u LEFT JOIN tb_pay_config p ON u.sub_mch_id = p.sub_mch_id WHERE u.id = ?", uuid);
        if (uMap.get("type") != null && !uMap.get("type").toString().equals("1")) {
            Map pMap = jdbcTemplate.queryForMap("SELECT * FROM tb_pay_config p WHERE p.type = 1 LIMIT 1");
            uMap.put("sub_mch_id", pMap.get("sub_mch_id"));
            uMap.put("app_key", pMap.get("app_key"));
            uMap.put("app_secret", pMap.get("app_secret"));
            uMap.put("pay_api_url", pMap.get("pay_api_url"));
            logger.info("new sub_mch_id # uid {}", uuid);
        }

        Map rmap = new HashMap();

        Map vMap = jdbcTemplate.queryForMap("SELECT * FROM tb_video_ss t WHERE t.id = ?", vid);

        Map<String, String> pMap = new HashMap();
        String key = UUID.randomUUID().toString().replaceAll("-", "");

        int ii = 0;
        try {
            pMap.put("app_key", uMap.get("app_key").toString());
            pMap.put("sub_mch_id", uMap.get("sub_mch_id").toString());
            pMap.put("openid", uMap.get("openid").toString());
            double d = Double.valueOf(vMap.get("pay_money").toString());
            ii = (int) (d * 100);
            pMap.put("total_fee", ii + "");
            pMap.put("out_trade_no", key);
            pMap.put("subject", "007视频");
            pMap.put("notify_url", "http://" + WebUtil.getHost() + "/webapp/pay_notify");
            pMap.put("extra", key);

            sign(pMap, uMap.get("app_secret").toString());
        } catch (Exception e) {

        }



        String apiok = "";
        try {


            OkHttpClient client = new OkHttpClient
                    .Builder()
                    .build();
            FormBody.Builder builder = new FormBody.Builder();

            if (pMap != null && pMap.keySet().size() > 0) {
                for (String key_f : pMap.keySet()) {
                    builder.add(key_f, pMap.get(key_f));
                }
            }
            Request request = new Request.Builder().url(uMap.get("pay_api_url") + "/api/v2/wechat/mp").post(builder.build()).build();

            StringBuffer sbHtml = new StringBuffer();
            Call call = null;

            call = client.newCall(request);
            try (Response ok_response = call.execute()) {
                if (!ok_response.isSuccessful()) throw new IOException("Unexpected code " + ok_response);
                apiok = ok_response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                call.clone();
                client.clone();
            }
        } catch (Exception e) {

        }




        logger.info("wechat back #" + apiok);
        try {
            JSONObject jo = JSON.parseObject(apiok);

            if (jo.get("code").toString().equals("200")) {
                jdbcTemplate.update("INSERT INTO tb_user_pay (id,uid,order_date,pay_status,vid,pay_money,sub_mch_id,channel_code) VALUES (?,?,?,?,?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE id = ?,uid = ?,order_date = ?,sub_mch_id = ?,channel_code = ?",
                        key, uuid, new Date(), 1, vid, ii / 100, uMap.get("sub_mch_id").toString(), uMap.get("channel_code"),
                        key, uuid, new Date(), uMap.get("sub_mch_id").toString(), uMap.get("channel_code"));//状态2为已支付

                return apiok;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        rmap.put("code", 500);
        rmap.put("message", "订单创建失败");
        rmap.put("sub_mch_id", uMap.get("sub_mch_id"));
        return JSON.toJSONString(rmap);

    }


    private void sign(Map map, String app_secret) {

        Collection<String> keyset = map.keySet();
        List<String> list = new ArrayList<String>(keyset);

        //对key键值按字典升序排序
        Collections.sort(list);

        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str = str + (list.get(i) + "=" + map.get(list.get(i)) + "&");
        }
        str = str.substring(0, str.length() - 1) + app_secret;
        logger.info(str);

        String sign = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            sign = new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
        }

        sign = sign.toUpperCase();

        map.put("sign", sign);

    }


    @Override
    public void payWebNotify(String key) {


        //最後支付時間監控
        redisUtil.set("CHECK_LEST_PAY_OK_TIME", new Date().getTime());

        Object rObj = redisUtil.get(GLOBAL.REDIS.PAY_OK_KEY + key);
        if (rObj == null) {

            jdbcTemplate.update("UPDATE tb_user_pay SET pay_status = pay_status | 4,pay_date = ? WHERE id = ?", new Date(), key);

        }

    }

    @Override
    public void payOk(String key) {

        Object rObj = redisUtil.get(GLOBAL.REDIS.PAY_OK_KEY + key);
        if (rObj == null) {
            redisUtil.set(GLOBAL.REDIS.PAY_OK_KEY + key, key, 30 * 60);


            //最後支付時間監控
            redisUtil.set("CHECK_LEST_PAY_OK_TIME", new Date().getTime());

            jdbcTemplate.update("UPDATE tb_user_pay SET pay_status = pay_status | 2,pay_date = ? WHERE id = ?", new Date(), key);

        }

    }


    @Override
    public List getBoughList(String uuid) {
        return jdbcTemplate.queryForList("SELECT * FROM tb_user_pay up LEFT JOIN tb_video_info vi\n" +
                "ON up.vid = vi.id\n" +
                "WHERE up.uid = ?  AND (up.pay_status & 2 = 2 OR up.pay_status & 4 = 4)   ORDER BY vi.`sort` DESC ", uuid);
    }


    @Override
    public Map getApiDomain(String domain) {
        Map map = new HashMap();
        map.put("apidomain", "/");
        return map;
    }

    @Override
    public String upUserOpenId(String uuid, String openid) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM tb_user_info t WHERE t.openid = ?", openid);
        if (list.size() > 0) {
            if (!uuid.equals(list.get(0).get("id").toString())) {
                jdbcTemplate.update("DELETE FROM tb_user_info WHERE id = ?", uuid);
            }
            return list.get(0).get("id").toString();
        }

        jdbcTemplate.update("UPDATE tb_user_info SET openid = ? WHERE id = ?", openid, uuid);
        return uuid;
    }

    @Override
    public int upUserSmi(String uuid, String smi) {
        return jdbcTemplate.update("UPDATE tb_user_info SET sub_mch_id = ? WHERE id = ?", smi, uuid);
    }

    @Override
    public Map getAppkey() {
        Map pcMap = new HashMap();
        List tList = jdbcTemplate.queryForList("SELECT * FROM tb_pay_config t WHERE t.`type` = 1 LIMIT 1;");
        if (tList.size() > 0) {
            pcMap = (Map) tList.get(0);
        }
        return pcMap;
    }

    @Override
    public List getVideossList(String begin, String sk) {
        List rList = new ArrayList();

        String where = "";
        if (sk != null && !sk.equals("")) {
            where = "  AND t.title LIKE '%" + sk + "%'";
        } else {
            return rList = jdbcTemplate.queryForList("SELECT * FROM tb_video_ss t WHERE t.`status` = 1 ORDER BY rand() LIMIT 10");
        }


        String c = jdbcTemplate.queryForMap("SELECT count(1) as c FROM tb_video_ss t WHERE t.`status` = 1" + where).get("c").toString();
        if (Integer.valueOf(begin) < Integer.valueOf(c) - 1) {
            rList = jdbcTemplate.queryForList("SELECT * FROM tb_video_ss t WHERE  t.`status` = 1 " + where + " ORDER BY t.`sort` DESC LIMIT ?,10 ", Integer.valueOf(begin));
        } else {
            rList = jdbcTemplate.queryForList("SELECT * FROM tb_video_ss t WHERE t.`status` = 1 ORDER BY rand() LIMIT 10");
        }
        return rList;
    }

    @Override
    public List getBoughVssList(String uuid) {
        return jdbcTemplate.queryForList("SELECT * FROM tb_user_pay up LEFT JOIN tb_video_ss vi\n" +
                "ON up.vid = vi.id\n" +
                "WHERE up.uid = ?  AND (up.pay_status & 2 = 2 OR up.pay_status & 4 = 4)  ORDER BY vi.`sort` DESC ", uuid);
    }

    @Override
    public Map getVideoss(String id) {
        return jdbcTemplate.queryForMap("SELECT * FROM tb_video_ss t WHERE t.id = ?", id);
    }

}
