package com.org.service;

import com.org.domain.LogMessageRequestVO;
import com.org.domain.LogMessageResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class ClmService {

    @Autowired
    RestTemplate restTemplate;

    public static final Logger logger= LoggerFactory.getLogger(ClmService.class);

    ClmService(RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }

    public LogMessageResponseVO logMessage(LogMessageRequestVO requestVO){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<LogMessageRequestVO> entity = new HttpEntity<LogMessageRequestVO>(requestVO,headers);

        logger.info("Store log for new employee creation/updation: ");
        logger.info("http://localhost:8081/logMessage : {}",requestVO);
        HttpEntity response = restTemplate.exchange(
                "http://localhost:8081/logMessage", HttpMethod.POST, entity, LogMessageResponseVO.class);
        logger.info("log stored successfully");

        return (LogMessageResponseVO) response.getBody();
    }



}
