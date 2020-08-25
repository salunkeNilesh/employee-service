package com.org.config;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "clm.service")
public class ClmServiceEndpoints {
    private String baseUrl;
    private String postLogMessageUrl;

}
