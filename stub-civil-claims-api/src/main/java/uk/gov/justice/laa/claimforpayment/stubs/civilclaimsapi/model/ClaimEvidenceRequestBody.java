package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Request body for adding evidence to a claim. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = ClaimEvidenceRequestBody.ClaimEvidenceRequestBodyBuilder.class)
@Schema(
    name = "ClaimEvidenceRequestBody",
    description = "Input model for adding evidence to a claim or line item")
public class ClaimEvidenceRequestBody implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @Schema(description = "The file key for the evidence")
  @JsonProperty("fileKey")
  private String fileKey;

  @NotNull
  @Schema(description = "The file size for the evidence")
  @JsonProperty("fileSize")
  private Long fileSize;
}
