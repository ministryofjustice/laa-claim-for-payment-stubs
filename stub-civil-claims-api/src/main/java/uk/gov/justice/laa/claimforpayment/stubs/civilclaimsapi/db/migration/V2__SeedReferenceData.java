package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;

/** Migration file to seed reference data. */
@SuppressWarnings({"checkstyle:linelength", "checkstyle:typename"})
@Slf4j
public class V2__SeedReferenceData extends BaseJavaMigration {

  private static final String FILE_PATH = "db/data/claims.yaml";

  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    ClaimsFile file = loadFile();
    insertClaims(connection, file);
  }

  private ClaimsFile loadFile() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.registerModule(new JavaTimeModule());

    String externalPath = System.getenv("CLAIMS_SEED_FILE");
    InputStream is;

    if (externalPath != null && !externalPath.isBlank()) {
      is = java.nio.file.Files.newInputStream(java.nio.file.Path.of(externalPath));
    } else {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_PATH);
    }

    try (is) {
      return mapper.readValue(is, ClaimsFile.class);
    }
  }

  private void insertClaims(Connection connection, ClaimsFile file) throws Exception {
    String sql =
        "INSERT INTO claims (ufn, client, category, concluded, fee_type, escaped, counsel_payment,"
            + " claimed, submission_id, provider_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {

      for (Claim c : file.getClaims()) {
        ps.setString(1, c.getUfn());
        ps.setString(2, c.getClient());
        ps.setString(3, c.getCategory());
        ps.setDate(4, Date.valueOf(c.getConcluded()));
        ps.setString(5, c.getFeeType());
        ps.setBoolean(6, c.getEscaped());
        ps.setString(7, c.getCounselPayment());
        ps.setBigDecimal(8, c.getClaimed());
        ps.setObject(9, c.getSubmissionId());
        ps.setObject(10, c.getProviderUserId());
        ps.addBatch();
      }

      ps.executeBatch();
    }
  }

  /** Represents a claims file. */
  public static class ClaimsFile {
    private List<Claim> claims;

    public List<Claim> getClaims() {
      return claims;
    }

    public void setClaims(List<Claim> claims) {
      this.claims = claims;
    }
  }
}
