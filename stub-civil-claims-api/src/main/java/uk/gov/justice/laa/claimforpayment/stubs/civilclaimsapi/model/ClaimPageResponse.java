package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Represents a paginated response containing a list of claims along with pagination metadata such
 * as current.
 */
@Schema(name = "ClaimPageResponse", description = "Paged list of claims")
public record ClaimPageResponse(
    @Schema(description = "Claims in this page") List<Claim> claims,
    @Schema(description = "Current page index") int page,
    @Schema(description = "Maximum number of claims per page") int limit,
    @Schema(description = "Total number of claims across all pages") long total,
    @Schema(description = "Total number of pages available") int totalPages) {}
