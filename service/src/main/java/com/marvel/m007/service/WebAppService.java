package com.marvel.m007.service;

import java.util.List;
import java.util.Map;

public interface WebAppService {

    public Map login(String cc, String uuid);

    public String payOrderVideo(String uuid, String vid);

    public void payWebNotify(String key);

    public void payOk(String key);

    public List getBoughList(String uuid);


    public Map getApiDomain(String domain);

    public String upUserOpenId(String uuid, String openid);

    public int upUserSmi(String uuid, String smi);



    public Map getAppkey();



    public List getVideossList(String begin,String sk);

    public List getBoughVssList(String uuid);

    public Map getVideoss(String id );
}
