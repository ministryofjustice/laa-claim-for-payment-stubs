package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception;

/**
 * The exception thrown when line item not found.
 */
public class LineItemNotFoundException extends RuntimeException {

  /**
   * Constructor for LineItemNotFoundException.
   *
   * @param message the error message
   */
  public LineItemNotFoundException(String message) {
    super(message);
  }
}
