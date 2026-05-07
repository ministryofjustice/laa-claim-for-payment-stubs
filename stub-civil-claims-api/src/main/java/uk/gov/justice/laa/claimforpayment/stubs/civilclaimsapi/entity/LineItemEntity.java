package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

/** Represents a line item in a claim, which can be associated with multiple pieces of evidence. **/
@Entity
public class LineItemEntity {

  @Id private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "claim_id")
  private ClaimEntity claim;

  @ManyToMany
  @JoinTable(
      name = "line_item_claim_evidence",
      joinColumns = @JoinColumn(name = "line_item_id"),
      inverseJoinColumns = @JoinColumn(name = "claim_evidence_id"))
  private Set<ClaimEvidenceEntity> evidences = new HashSet<>();
}
