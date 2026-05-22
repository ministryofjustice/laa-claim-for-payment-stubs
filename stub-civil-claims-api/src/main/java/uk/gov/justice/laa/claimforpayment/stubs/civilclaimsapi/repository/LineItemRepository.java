package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;

/** Repository for managing line item entities. */
@Repository
public interface LineItemRepository extends JpaRepository<LineItemEntity, Long> {}
