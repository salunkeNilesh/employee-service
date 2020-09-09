package com.org.utils;

import com.org.constants.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class RestTemplateUtil {

    private RestTemplate restTemplate;
    public final Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

    @Value("${api.connect.timeout}")
    public int apiConnectTimeout;

    @Value("${api.read.timeout}")
    public int apiReadTimeout;

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        //connect timeout
        simpleClientHttpRequestFactory.setConnectTimeout(apiConnectTimeout);
        logger.info("api.connect.timeout: {} api.read.timeout: {}", apiConnectTimeout, apiReadTimeout);
        //read timeout
        simpleClientHttpRequestFactory.setReadTimeout(apiReadTimeout);

        return simpleClientHttpRequestFactory;

    }
    
    public RestTemplate getRestTemplate() {
        restTemplate = new RestTemplate(getClientHttpRequestFactory());
        return restTemplate;
    }

}
