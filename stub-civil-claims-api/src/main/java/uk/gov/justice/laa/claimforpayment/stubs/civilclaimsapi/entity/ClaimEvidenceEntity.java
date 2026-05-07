package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

/** Represents evidence associated with a claim. **/
@Entity
public class ClaimEvidenceEntity {

  @Id private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "claim_id")
  private ClaimEntity claim;

  @ManyToMany(mappedBy = "evidences")
  private Set<LineItemEntity> lineItems = new HashSet<>();
}
