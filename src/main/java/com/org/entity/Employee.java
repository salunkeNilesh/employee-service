package com.org.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigInteger;


@Getter
@Setter
@ToString
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "emp_id_generator",sequenceName = "emp_",initialValue = 0)
    private int empId;

    private String empFirstName;
    private String empLastName;
    private String empAddress;
    private String empDepartment;
    private String empEmail;
    private String empMobileNumber;
    private String empAdditionalInfo;


}
