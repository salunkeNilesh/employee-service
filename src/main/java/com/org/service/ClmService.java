package com.org.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.org.config.ClmServiceEndpoints;
import com.org.config.RibbonConfiguration;
import com.org.domain.LogMessageRequestVO;
import com.org.domain.LogMessageResponseVO;
import com.org.utils.RestTemplateUtil;
import com.org.utils.WebClientUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.apache.http.HttpStatus;

import java.io.ObjectStreamException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

@Service
public class ClmService {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Autowired
    private LoadBalancerClient lba;

    private ClmServiceEndpoints clmServiceEndpoints;

    public static final Logger logger = LoggerFactory.getLogger(ClmService.class);

    @Autowired
    ClmService(ClmServiceEndpoints clmServiceEndpoints) {
        this.clmServiceEndpoints = clmServiceEndpoints;
    }

    public String getRandomApplicationName() {
        String[] applicationName = {"Employee_Service", "Employee_Service500", "Employee_Service204", "Employee_Service404", "Employee_ServiceTimeoutOver"};
        int rnd = new Random().nextInt(applicationName.length);
        return applicationName[rnd];

    }

    @HystrixCommand(fallbackMethod = "getDefaultLog")
    public LogMessageResponseVO logMessage(LogMessageRequestVO requestVO) {

        String appName = getRandomApplicationName();
        logger.info("Random Application name : {}", appName);
        requestVO.setApplicationName(appName);

        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        Long pid = Long.parseLong(processName.split("@")[0]);
        String threadName = Thread.currentThread().getName();

        Map<String, Object> messageMap = requestVO.getMessage();
        messageMap.put("level", "INFO");
        messageMap.put("processName", pid);
        messageMap.put("pid", pid);
        messageMap.put("thread", threadName);

        //String URI = clmServiceEndpoints.getBaseUrl() + clmServiceEndpoints.getPostLogMessageUrl();
        ServiceInstance servInstance = lba.choose("clm");

        String URI = servInstance.getUri().toString() + "/" + clmServiceEndpoints.getPostLogMessageUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<LogMessageRequestVO> entity = new HttpEntity<LogMessageRequestVO>(requestVO, headers);

        logger.info("Store log for new employee creation/updation : ");
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

        String appName = getRandomApplicationName();
        logger.info("Random Application name : {}", appName);
        requestVO.setApplicationName(appName);

        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        Long pid = Long.parseLong(processName.split("@")[0]);
        String threadName = Thread.currentThread().getName();

        Map<String, Object> messageMap = requestVO.getMessage();
        messageMap.put("level", "INFO");
        messageMap.put("processName", pid);
        messageMap.put("pid", pid);
        messageMap.put("thread", threadName);


        WebClient webClient = WebClientUtil.getWebClient(clmServiceEndpoints.getBaseUrl());

        logger.info("Store log for new employee deletion : ");
        logger.info("{} :", requestVO);
        LogMessageResponseVO responseVO = webClient.post().uri(clmServiceEndpoints.getPostLogMessageUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestVO), LogMessageRequestVO.class)
                .retrieve()
                .bodyToMono(LogMessageResponseVO.class).block();
        logger.info("delete log stored successfully");
        return responseVO;

    }


    //Hystreix fall back method
    public LogMessageResponseVO getDefaultLog(LogMessageRequestVO requestVO, Throwable throwable) {
        LogMessageResponseVO responseVO = new LogMessageResponseVO();
        logger.info("HystrixCommand method Exception : {}  ***", throwable.getMessage());
        throwable.printStackTrace();

        try {
            if (throwable.getMessage().startsWith(String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR))
                    || throwable.getMessage().startsWith(String.valueOf(HttpStatus.SC_NOT_FOUND))
                    || throwable.getMessage().startsWith(String.valueOf(HttpStatus.SC_NO_CONTENT))) {

                responseVO = RetryLogService(requestVO);
            }
        } catch (Exception e) {

        }
        logger.info("FallBack :: for request : {}", requestVO.toString());
        logger.info("Fallback response :: {} ", responseVO.toString());
        return responseVO;
    }


    @SneakyThrows
    public LogMessageResponseVO RetryLogService(LogMessageRequestVO requestVO) {
        String URI = clmServiceEndpoints.getBaseUrl() + clmServiceEndpoints.getPostLogMessageUrl();
        int maxRetry = clmServiceEndpoints.getMaxRetryLimit();
        int retryInterval = clmServiceEndpoints.getRetryTimeInterval();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<LogMessageRequestVO> entity = new HttpEntity<LogMessageRequestVO>(requestVO, headers);

        logger.info("Retry log : ");
        logger.info("{} : {}", URI, requestVO);
        logger.info("Max Retry Limit : {}", maxRetry);
        logger.info("Retry time interval : {}", retryInterval);


        RestTemplate restTemplate = restTemplateUtil.getRestTemplate();
        LogMessageResponseVO resposneVO = null;

        for (int i = 1; i <= maxRetry; i++) {

            try {
                resposneVO = restTemplate.postForObject(URI, requestVO, LogMessageResponseVO.class);
                logger.info("log stored successfully in retry");
                break;
            } catch (RestClientException e) {
                logger.info("Failed in retry no : {}", i);
                logger.info("Exception message {} ", e.getMessage());
                logger.info("Wait for {} millisecond", retryInterval);
                Thread.sleep(retryInterval);
            }
        }

        if (resposneVO == null)
            resposneVO = defaultForRetryLog(requestVO);

        return resposneVO;

    }

    public LogMessageResponseVO defaultForRetryLog(LogMessageRequestVO requestVO) {
        LogMessageResponseVO responseVO = new LogMessageResponseVO();
        logger.info("Not worked in retry. Returning default response {}", responseVO.toString());
        return responseVO;

    }

}
