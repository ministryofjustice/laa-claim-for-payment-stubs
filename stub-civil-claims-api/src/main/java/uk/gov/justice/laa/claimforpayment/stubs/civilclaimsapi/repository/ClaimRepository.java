package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;

/** Repository for managing claim entities. */
@Repository
public interface ClaimRepository extends JpaRepository<ClaimEntity, Long> {

  Page<ClaimEntity> findByProviderUserId(UUID providerUserId, Pageable pageable);
}
