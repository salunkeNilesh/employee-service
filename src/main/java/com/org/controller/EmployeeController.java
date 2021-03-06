package com.org.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.constants.Constant;
import com.org.domain.LogMessageRequestVO;
import com.org.models.AddEmployeeRequestVO;
import com.org.models.AddEmployeeResponseVO;
import com.org.models.EditEmployeeRequestVO;
import com.org.models.EditEmployeeResponseVO;
import com.org.repository.EmployeeRepository;
import com.org.service.ClmService;
import com.org.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmployeeController {


    @Autowired
    EmployeeRepository employeeRepository;

    EmployeeService employeeService;

    ClmService clmService;

    public static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    public EmployeeController(EmployeeService employeeService, ClmService clmService) {
        this.employeeService = employeeService;
        this.clmService = clmService;
    }


    @PostMapping(path = "/addEmployee")
    public @ResponseBody
    ResponseEntity<AddEmployeeResponseVO> addEmployee(@RequestBody AddEmployeeRequestVO requestVO) throws JsonProcessingException {
        logger.info("/addEmployee");
        AddEmployeeResponseVO responseVO = employeeService.addEmployee(requestVO);

        //--Call clm service to add log
        LogMessageRequestVO addLog = new LogMessageRequestVO();
        addLog.setApplicationId(Constant.SERVICE_ID);
        addLog.setApplicationName(Constant.SERVICE_NAME);

        Map<String, Object> message = new HashMap<>();
        message.put("message", "new employee record added");
        message.put("additionalInfo", responseVO.toString());
        addLog.setMessage(message);

        clmService.logMessage(addLog);
        //

        return new ResponseEntity<AddEmployeeResponseVO>(responseVO, HttpStatus.OK);
    }


    @DeleteMapping(path = "/employee/{empId}")
    public @ResponseBody
    ResponseEntity<String> deleteEmployee(@PathVariable int empId) {
        logger.info("/employee MethodType: delete ");
        employeeService.deleteEmployee(empId);
        //--Call clm service to add log
        LogMessageRequestVO addLog = new LogMessageRequestVO();
        addLog.setApplicationId(Constant.SERVICE_ID);
        addLog.setApplicationName(Constant.SERVICE_NAME);

        Map<String, Object> message = new HashMap<>();
        message.put("message", "employee record deleted");
        addLog.setMessage(message);

        clmService.addDeleteLog(addLog);
        //

        return new ResponseEntity("employee with empId : " + empId + " successfully deleted", HttpStatus.OK);
    }

    @PutMapping(path = "/employee/{empId}")
    public @ResponseBody
    ResponseEntity<EditEmployeeResponseVO> editEmployee(@PathVariable int empId, @RequestBody EditEmployeeRequestVO requestVO) throws JsonProcessingException {
        logger.info("/employee MethodType: put ");
        EditEmployeeResponseVO responseVO = null;

        if (!employeeRepository.existsById(empId)) {
            logger.info("employee record with empId : {} not found", empId);
            return ResponseEntity.notFound().build();
        } else {
            responseVO = employeeService.editEmployee(empId, requestVO);
        }

        //--Call clm service to add log
        LogMessageRequestVO addLog = new LogMessageRequestVO();
        addLog.setApplicationId(Constant.SERVICE_ID);
        addLog.setApplicationName(Constant.SERVICE_NAME);

        Map<String, Object> message = new HashMap<>();
        message.put("message", "employee record updated");
        message.put("additionalInfo", responseVO.toString());

        addLog.setMessage(message);

        clmService.logMessage(addLog);
        //
        return new ResponseEntity<EditEmployeeResponseVO>(responseVO, HttpStatus.OK);
    }
}
