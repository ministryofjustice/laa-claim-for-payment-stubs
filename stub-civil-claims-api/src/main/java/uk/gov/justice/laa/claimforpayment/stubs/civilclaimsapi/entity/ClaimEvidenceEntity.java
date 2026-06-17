package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Represents evidence associated with a claim. * */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CLAIM_EVIDENCE")
public class ClaimEvidenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "claim_id")
  private ClaimEntity claim;

  private String fileKey;

  private Long fileSize;

  @CreationTimestamp
  private Instant dateAdded;

  @ManyToMany(mappedBy = "evidenceItems")
  @Builder.Default
  private Set<LineItemEntity> lineItems = new HashSet<>();
}
