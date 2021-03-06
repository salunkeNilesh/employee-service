package com.org.models;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class AddEmployeeResponseVO {

    private int empId;
    private String empFirstName;
    private String empLastName;
    private String empAddress;
    private String empDepartment;
    private String empEmail;
    private BigInteger empMobileNumber;
    private String empAdditionalInfo;

}
