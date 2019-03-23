package com.marvel.m007.web;

import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {

    public ContextListener() {
    }

    public void contextDestroyed(ServletContextEvent arg0)  {
    }

    public void contextInitialized(ServletContextEvent arg0)  {
        WebApplicationContextUtils.getRequiredWebApplicationContext(arg0.getServletContext())
                .getAutowireCapableBeanFactory().autowireBean(this);

        System.out.println("监听器初始化。");
    }

}
