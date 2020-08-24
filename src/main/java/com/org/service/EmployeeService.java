package com.org.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.entity.Employee;
import com.org.models.AddEmployeeRequestVO;
import com.org.models.AddEmployeeResponseVO;
import com.org.models.EditEmployeeRequestVO;
import com.org.models.EditEmployeeResponseVO;
import com.org.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Optional;

@Service
public class EmployeeService {


    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    DataSource dataSource;

    JdbcTemplate jdbcTemplate;

    ObjectMapper mapper = new ObjectMapper();

    public static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    EmployeeService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    public AddEmployeeResponseVO addEmployee(AddEmployeeRequestVO requestVO) {
        Employee emp = mapper.convertValue(requestVO, Employee.class);
        emp.setEmpEmail(emp.getEmpFirstName() + "." + emp.getEmpLastName() + "@org.com");
        AddEmployeeResponseVO responseVO = mapper.convertValue(employeeRepository.save(emp), AddEmployeeResponseVO.class);
        logger.info("AddEmployeeRequestVO : {}", requestVO);
        logger.info("AddEmployeeResposneVO : {}", responseVO);
        return responseVO;
    }

    public void deleteEmployee(int empId) {
        if (employeeRepository.existsById(empId)) {
            employeeRepository.deleteById(empId);
            logger.info("employee record deleted with : {}", empId);
        } else {
            logger.info("No records found with emp_id : {}", empId);
        }

    }

    public EditEmployeeResponseVO editEmployee(int empId, EditEmployeeRequestVO requestVO) {
        EditEmployeeResponseVO responseVO=null;
        Optional<Employee> emp =  employeeRepository.findById(empId);
            if (!emp.isPresent()){
                //Handle exception
            }else{
                Employee employee = new Employee();
                employee.setEmpId(empId);
                employee.setEmpFirstName(requestVO.getEmpFirstName());
                employee.setEmpLastName(requestVO.getEmpLastName());
                employee.setEmpAddress(requestVO.getEmpAddress());
                employee.setEmpDepartment(requestVO.getEmpDepartment());
                employee.setEmpMobileNumber(requestVO.getEmpMobileNumber().toString());
                employee.setEmpEmail(requestVO.getEmpEmail());
                employee.setEmpAdditionalInfo(requestVO.getEmpAdditionalInfo());
                employeeRepository.save(employee);
                logger.info("Employee with id: {}, updated. {}",empId,employee);
                responseVO = mapper.convertValue(employee,EditEmployeeResponseVO.class);
            }
                return responseVO;
    }

}
