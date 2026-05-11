package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.db.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Migration file to seed reference data. */
@SuppressWarnings({"checkstyle:linelength", "checkstyle:typename", "checkstyle:membername"})
@Slf4j
public class V3__SeedReferenceData extends BaseJavaMigration {

  private static final String FILE_PATH = "db/data/claims.yaml";

  @Override
  public void migrate(Context context) throws Exception {

    Connection connection = context.getConnection();
    ClaimsFile file = loadFile();
    assertSeedFileIsValid(file);
    connection.setAutoCommit(false);

    try {

      // ---------- 1. insert claims ----------
      Map<String, Long> claimIdsByKey = new HashMap<>();

      String insertClaimSql =
          "INSERT INTO claims (ufn, client, category, concluded, fee_type, escaped,"
              + " counsel_payment, claimed, submission_id, provider_user_id) VALUES (?, ?, ?, ?, ?,"
              + " ?, ?, ?, ?, ?)";

      try (PreparedStatement ps =
          connection.prepareStatement(insertClaimSql, Statement.RETURN_GENERATED_KEYS)) {

        for (ClaimRow c : file.claims) {
          ps.setString(1, c.ufn);
          ps.setString(2, c.client);
          ps.setString(3, c.category);
          ps.setDate(4, Date.valueOf(c.concluded));
          ps.setString(5, c.feeType);
          ps.setBoolean(6, c.escaped);
          ps.setString(7, c.counselPayment);
          ps.setBigDecimal(8, c.claimed);
          ps.setObject(9, c.submissionId);
          ps.setObject(10, c.providerUserId);
          ps.executeUpdate();

          try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();

            String claimKey = c.ufn + "|" + c.client;
            claimIdsByKey.put(claimKey, rs.getLong(1));
          }
        }
      }

      // ---------- 2. insert claim_evidence ----------
      Map<String, Long> evidenceIdsByKey = new HashMap<>();

      String insertEvidenceSql = "INSERT INTO claim_evidence (claim_id, file_key) VALUES (?, ?)";

      try (PreparedStatement ps =
          connection.prepareStatement(insertEvidenceSql, Statement.RETURN_GENERATED_KEYS)) {

        for (ClaimEvidenceRow e : file.claim_evidence) {
          String claimKey = e.claimUfn + "|" + e.client;
          long claimId = claimIdsByKey.get(claimKey);
          ps.setLong(1, claimId);
          ps.setString(2, e.fileIdString);
          ps.executeUpdate();

          try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();
            evidenceIdsByKey.put(claimKey + ":" + e.fileIdString, rs.getLong(1));
          }
        }
      }

      // ---------- 3. insert line_items ----------
      Map<String, Long> lineItemIdsByKey = new HashMap<>();

      String insertLineItemSql = "INSERT INTO line_items (claim_id, description) VALUES (?, ?)";

      try (PreparedStatement ps =
          connection.prepareStatement(insertLineItemSql, Statement.RETURN_GENERATED_KEYS)) {

        for (LineItemRow li : file.line_items) {

          String claimKey = li.claimUfn + "|" + li.client;
          String lineItemKey = claimKey + "|" + li.description;

          long claimId = claimIdsByKey.get(claimKey);

          ps.setLong(1, claimId);
          ps.setString(2, li.description);
          ps.executeUpdate();

          try (ResultSet rs = ps.getGeneratedKeys()) {
            rs.next();
            lineItemIdsByKey.put(lineItemKey, rs.getLong(1));
          }
        }
      }

      // ---------- 4. insert join rows ----------
      String insertJoinSql =
          "INSERT INTO line_item_claim_evidence (line_item_id, claim_evidence_id) VALUES (?, ?)";

      try (PreparedStatement ps = connection.prepareStatement(insertJoinSql)) {

        for (LineItemEvidenceRow link : file.line_item_claim_evidence) {

          String claimKey = link.claimUfn + "|" + link.client;
          String lineItemKey = claimKey + "|" + link.lineItemDescription;

          Long lineItemId = lineItemIdsByKey.get(lineItemKey);
          Long evidenceId = evidenceIdsByKey.get(claimKey + ":" + link.evidenceFileIdString);

          ps.setLong(1, lineItemId);
          ps.setLong(2, evidenceId);
          ps.executeUpdate();
        }
      }

      connection.commit();

    } catch (Exception ex) {
      connection.rollback();
      throw ex;
    } finally {
      connection.setAutoCommit(true);
    }
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

  private void assertSeedFileIsValid(ClaimsFile file) {

    // ---- 1. claims must have unique lookup keys ----
    Set<String> claimKeys = new HashSet<>();

    for (ClaimRow c : file.claims) {
      String key = c.ufn + "|" + c.client;
      if (!claimKeys.add(key)) {
        throw new IllegalStateException(
            "Duplicate claim lookup key in seed file: ufn=" + c.ufn + ", client=" + c.client);
      }
    }

    // ---- 2. claim_evidence must reference existing claims ----
    if (file.claim_evidence != null) {
      for (ClaimEvidenceRow e : file.claim_evidence) {
        String key = e.claimUfn + "|" + e.client;
        if (!claimKeys.contains(key)) {
          throw new IllegalStateException(
              "claim_evidence references unknown claim: ufn="
                  + e.claimUfn
                  + ", client="
                  + e.client);
        }
      }
    }

    // ---- 3. line_items must reference existing claims ----
    if (file.line_items != null) {
      for (LineItemRow li : file.line_items) {
        String key = li.claimUfn + "|" + li.client;
        if (!claimKeys.contains(key)) {
          throw new IllegalStateException(
              "line_items references unknown claim: ufn=" + li.claimUfn + ", client=" + li.client);
        }
      }
    }

    // ---- 4. line_item_claim_evidence must reference existing claims ----
    if (file.line_item_claim_evidence != null) {
      for (LineItemEvidenceRow link : file.line_item_claim_evidence) {
        String key = link.claimUfn + "|" + link.client;
        if (!claimKeys.contains(key)) {
          throw new IllegalStateException(
              "line_item_claim_evidence references unknown claim: ufn="
                  + link.claimUfn
                  + ", client="
                  + link.client);
        }
      }
    }
  }

  static class ClaimsFile {
    public List<ClaimRow> claims;
    public List<ClaimEvidenceRow> claim_evidence;
    public List<LineItemRow> line_items;
    public List<LineItemEvidenceRow> line_item_claim_evidence;
  }

  static class ClaimRow {
    public String ufn;
    public String client;
    public String category;
    public LocalDate concluded;
    public String feeType;
    public boolean escaped;
    public String counselPayment;
    public BigDecimal claimed;
    public UUID submissionId;
    public UUID providerUserId;
  }

  static class ClaimEvidenceRow {
    public String claimUfn;
    public String client;
    public String fileIdString;
  }

  static class LineItemRow {
    public String client;
    public String claimUfn;
    public String description;
  }

  static class LineItemEvidenceRow {
    public String client;
    public String claimUfn;
    public String evidenceFileIdString;
    public String lineItemDescription;
  }
}
