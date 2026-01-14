package rox.todd.common.exception;

/**
 * A class to represent exceptions that can occur at the API layer.
 */
public class ApiException extends HomeyException {
  private static final long serialVersionUID = 1L;
  public static enum ApiError implements ExceptionReason { NOT_ALLOWED }

  public ApiException(String technicalMessage) {
    super(technicalMessage);
  }
  
  public ApiException(String technicalMessage, String userMessage) {
    super(technicalMessage, userMessage);
  }
  
  public ApiException(String technicalMessage, String userMessage, Exception e) {
    super(technicalMessage, userMessage, e);
  }
}