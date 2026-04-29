package uk.gov.justice.laa.stubs.claimforpayment.stubs.civilclaimsapi.model;

import java.util.Date;
import java.util.UUID;

public record TestClaim(
        String ufn,
        String client,
        String category,
        Date concluded,
        String fee_type,
        BigDecimal claimed,
        UUID submission_id,
        UUID provider_user_id
) {}