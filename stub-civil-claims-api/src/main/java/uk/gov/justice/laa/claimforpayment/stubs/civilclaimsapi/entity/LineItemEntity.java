package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a line item in a claim, which can be associated with multiple pieces of evidence. *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LINE_ITEMS")
public class LineItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String category;

  private LocalDate date;

  @ManyToOne(optional = false)
  @JoinColumn(name = "claim_id")
  private ClaimEntity claim;

  @ManyToMany
  @JoinTable(
      name = "line_item_claim_evidence",
      joinColumns = @JoinColumn(name = "line_item_id"),
      inverseJoinColumns = @JoinColumn(name = "claim_evidence_id"))
  @Builder.Default
  private Set<ClaimEvidenceEntity> evidenceItems = new HashSet<>();
}
