package com.org.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.org.config.ClmServiceEndpoints;
import com.org.domain.LogMessageRequestVO;
import com.org.domain.LogMessageResponseVO;
import com.org.utils.RestTemplateUtil;
import com.org.utils.WebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Map;

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


    @HystrixCommand(fallbackMethod = "getDefaultLog")
    public LogMessageResponseVO logMessage(LogMessageRequestVO requestVO) {

        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        Long pid = Long.parseLong(processName.split("@")[0]);
        String threadName = Thread.currentThread().getName();

        Map<String, Object> messageMap = requestVO.getMessage();
        messageMap.put("level", "INFO");
        messageMap.put("processName", pid);
        messageMap.put("pid", pid);
        messageMap.put("thread", threadName);

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

    @HystrixCommand(fallbackMethod = "getDefaultLog")
    public LogMessageResponseVO addDeleteLog(LogMessageRequestVO requestVO) {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        Long pid = Long.parseLong(processName.split("@")[0]);
        String threadName = Thread.currentThread().getName();

        Map<String, Object> messageMap = requestVO.getMessage();
        messageMap.put("level", "INFO");
        messageMap.put("processName", pid);
        messageMap.put("pid", pid);
        messageMap.put("thread", threadName);


        WebClient webClient = WebClientUtil.getWebClient(clmServiceEndpoints.getBaseUrl());

        LogMessageResponseVO responseVO = webClient.post().uri(clmServiceEndpoints.getPostLogMessageUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestVO), LogMessageRequestVO.class)
                .retrieve()
                .bodyToMono(LogMessageResponseVO.class).block();
        logger.info("delete log stored successfully");
        return responseVO;

    }


    //Hystreix fall back method
    public LogMessageResponseVO getDefaultLog(LogMessageRequestVO requestVO) {
        LogMessageResponseVO responseVO = new LogMessageResponseVO();
        logger.info("FallBack :: Returning default logMessage for : {}", requestVO.toString());
        logger.info("Fallback response :: {} ", responseVO.toString());
        return responseVO;
    }

}
