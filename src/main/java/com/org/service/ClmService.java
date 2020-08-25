package com.org.service;

import com.org.config.ClmServiceEndpoints;
import com.org.domain.LogMessageRequestVO;
import com.org.domain.LogMessageResponseVO;
import com.org.utils.RestTemplateUtil;
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
    RestTemplateUtil restTemplateUtil;

    private ClmServiceEndpoints clmServiceEndpoints;

    public static final Logger logger = LoggerFactory.getLogger(ClmService.class);

    @Autowired
    ClmService(ClmServiceEndpoints clmServiceEndpoints) {
        this.clmServiceEndpoints = clmServiceEndpoints;
    }

    public LogMessageResponseVO logMessage(LogMessageRequestVO requestVO) {

        String URI = clmServiceEndpoints.getBaseUrl() + clmServiceEndpoints.getPostLogMessageUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<LogMessageRequestVO> entity = new HttpEntity<LogMessageRequestVO>(requestVO, headers);

        logger.info("Store log for new employee creation/updation: ");
        logger.info("{} : {}", URI, requestVO);

        RestTemplate restTemplate = restTemplateUtil.getRestTemplate();

        /*HttpEntity response = restTemplate.exchange(
                URI, HttpMethod.POST, entity, LogMessageResponseVO.class);
        logger.info("log stored successfully");
        return (LogMessageResponseVO) response.getBody();
        */

        LogMessageResponseVO resposneVO = restTemplate.postForObject(URI, requestVO, LogMessageResponseVO.class);
        logger.info("log stored successfully");
        return resposneVO;

    }


}
