package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception;

/**
 * The exception thrown when claim evidence not found.
 */
public class ClaimEvidenceNotFoundException extends RuntimeException {

  /**
   * Constructor for ClaimEvidenceNotFoundException.
   *
   * @param message the error message
   */
  public ClaimEvidenceNotFoundException(String message) {
    super(message);
  }
}
