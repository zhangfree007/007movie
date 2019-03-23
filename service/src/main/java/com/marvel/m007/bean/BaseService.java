package com.marvel.m007.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class BaseService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    protected JdbcTemplate jdbcTemplate;

}
