package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Input model for creating a line item. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = LineItemRequestBody.LineItemRequestBodyBuilder.class)
@Schema(name = "LineItemRequestBody", description = "Input model for creating a line item")
public class LineItemRequestBody {

  @Schema(description = "The title of the line item")
  @JsonProperty("title")
  private String title;

  @Schema(description = "The category of the line item")
  @JsonProperty("category")
  private String category;

  @Schema(description = "The date of the line item")
  @JsonProperty("date")
  private LocalDate date;
}
