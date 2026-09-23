package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception;

import org.springframework.http.HttpStatus;

/**
 * The exception thrown when simulating an error.
 */
public class StubException extends RuntimeException {

  private final HttpStatus status;

  /**
   * Constructor for StubException.
   *
   * @param status the error status
   * @param message the error message
   */
  public StubException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  /**
   * Get the error status.
   *
   * @return the error status
   */
  public HttpStatus getStatus() {
    return status;
  }
}
