package com.example.userexample.common;//package com.example.userexample.common;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//@ControllerAdvice   // 전역 설정을 위한 annotaion
//@RestController
//public class ExceptionAdvisor {
//
//    @ExceptionHandler(AccessDeniedException.class )
//    public ResponseEntity handleAccessDeniedException(final AccessDeniedException ex) {
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
//    }
//}
