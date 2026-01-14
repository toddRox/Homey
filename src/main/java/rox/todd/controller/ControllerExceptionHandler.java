package rox.todd.controller;

import static rox.todd.common.util.Util.hasValue;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import rox.todd.common.exception.HomeyException;

/**
 * A global exception handler for the API.
 */
@RestControllerAdvice
public class ControllerExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    HomeyException be = e instanceof HomeyException b ? b : null;
    ErrorResponse er = be == null ? new ErrorResponse() : new ErrorResponse(be.getUserMessage());
    
    log.error(er.errorId + "", er.errorMsg, e);
    
    return ResponseEntity.status(HttpStatus.CONFLICT).body(er);
  }

  public static class ErrorResponse{
    public String errorId = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    public String errorMsg = "An error occurred.";
        
    public ErrorResponse() { }
    
    public ErrorResponse(String errorMsg) {
      if(hasValue(errorMsg)) this.errorMsg = errorMsg;
    }
  }
}
