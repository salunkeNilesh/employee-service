package com.org.constants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class Constant {

    public static final String SERVICE_NAME = "Employee_Service";

    public static final BigDecimal SERVICE_ID = BigDecimal.valueOf(1);


}
