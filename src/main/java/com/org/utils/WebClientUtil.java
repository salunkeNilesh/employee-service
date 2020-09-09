package com.org.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;


public class WebClientUtil {


    public static WebClient getWebClient(String baseUrl){
        WebClient webClient = WebClient.create(baseUrl);

        return webClient;
    }

}
