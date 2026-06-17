package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds reference data on application startup. */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MemberName")
public class ClaimsSeeder {

  private final DataSource dataSource;

  private static final String FILE_PATH = "db/data/claims.yaml";

  private static final AtomicBoolean HAS_RUN = new AtomicBoolean(false);

  @Bean
  @Profile("!prod")
  public ApplicationRunner seederRunner(ClaimsSeeder seeder) {
    return args -> seeder.seed();
  }

  /** Seeds the claims data on application startup. */
  public void seed() throws Exception {

    if (!HAS_RUN.compareAndSet(false, true)) {
      return;
    }

    log.info("Starting claims data seeding...");

    try (Connection connection = dataSource.getConnection()) {

      connection.setAutoCommit(false);

      try {
        clearTables(connection);

        ClaimsFile file = loadFile();
        assertSeedFileIsValid(file);

        Map<String, Long> claimIdsByKey = insertClaims(connection, file);
        Map<String, Long> evidenceIdsByKey = insertEvidence(connection, file, claimIdsByKey);
        Map<String, Long> lineItemIdsByKey = insertLineItems(connection, file, claimIdsByKey);

        insertJoinRows(connection, file, lineItemIdsByKey, evidenceIdsByKey);

        connection.commit();
        log.info("Seeding completed successfully");

      } catch (Exception ex) {
        connection.rollback();
        log.error("Seeding failed, rolled back", ex);
        throw ex;
      }
    }
  }

  // ✅ Delete everything (safe re-run)
  private void clearTables(Connection connection) throws SQLException {
    log.info("Clearing existing data...");

    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DELETE FROM line_item_claim_evidence");
      stmt.executeUpdate("DELETE FROM claim_evidence");
      stmt.executeUpdate("DELETE FROM line_items");
      stmt.executeUpdate("DELETE FROM claims");
    }
  }

  private Map<String, Long> insertClaims(Connection connection, ClaimsFile file)
      throws SQLException {
    Map<String, Long> claimIdsByKey = new HashMap<>();

    String sql =
        "INSERT INTO claims (ufn, client, category, concluded, fee_type, escaped, counsel_payment,"
            + " claimed, submission_id, provider_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
          claimIdsByKey.put(c.ufn + "|" + c.client, rs.getLong(1));
        }
      }
    }
    return claimIdsByKey;
  }

  private Map<String, Long> insertEvidence(
      Connection connection, ClaimsFile file, Map<String, Long> claimIds) throws SQLException {

    Map<String, Long> evidenceIds = new HashMap<>();
    String sql = "INSERT INTO claim_evidence"
        + " (claim_id, file_key, file_size, date_added) "
        + "VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (ClaimEvidenceRow e : file.claim_evidence) {
        String key = e.claimUfn + "|" + e.client;
        ps.setLong(1, claimIds.get(key));
        ps.setString(2, e.fileIdString);
        ps.setLong(3, e.fileSize);
        ps.setTimestamp(4, Timestamp.from(e.dateAdded));
        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
          rs.next();
          evidenceIds.put(key + ":" + e.fileIdString, rs.getLong(1));
        }
      }
    }
    return evidenceIds;
  }

  private Map<String, Long> insertLineItems(
      Connection connection, ClaimsFile file, Map<String, Long> claimIds) throws SQLException {

    Map<String, Long> lineItems = new HashMap<>();
    String sql = "INSERT INTO line_items (claim_id, title, category, date) VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      for (LineItemRow li : file.line_items) {
        String key = li.claimUfn + "|" + li.client;
        String lineKey = key + "|" + li.title;

        ps.setLong(1, claimIds.get(key));
        ps.setString(2, li.title);
        ps.setString(3, li.category);
        ps.setDate(4, Date.valueOf(li.date));
        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
          rs.next();
          lineItems.put(lineKey, rs.getLong(1));
        }
      }
    }
    return lineItems;
  }

  private void insertJoinRows(
      Connection connection,
      ClaimsFile file,
      Map<String, Long> lineItems,
      Map<String, Long> evidenceIds)
      throws SQLException {

    String sql =
        "INSERT INTO line_item_claim_evidence (line_item_id, claim_evidence_id) VALUES (?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {

      for (LineItemEvidenceRow link : file.line_item_claim_evidence) {
        String claimKey = link.claimUfn + "|" + link.client;
        String lineKey = claimKey + "|" + link.lineItemTitle;

        ps.setLong(1, lineItems.get(lineKey));
        ps.setLong(2, evidenceIds.get(claimKey + ":" + link.evidenceFileIdString));
        ps.executeUpdate();
      }
    }
  }

  private ClaimsFile loadFile() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.registerModule(new JavaTimeModule());

    InputStream is =
        Optional.ofNullable(System.getenv("CLAIMS_SEED_FILE"))
            .map(
                path -> {
                  try {
                    return java.nio.file.Files.newInputStream(java.nio.file.Path.of(path));
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                })
            .orElseGet(
                () ->
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_PATH));

    try (is) {
      return mapper.readValue(is, ClaimsFile.class);
    }
  }

  private void assertSeedFileIsValid(ClaimsFile file) {
    Set<String> keys = new HashSet<>();

    for (ClaimRow c : file.claims) {
      String key = c.ufn + "|" + c.client;
      if (!keys.add(key)) {
        throw new IllegalStateException("Duplicate claim: " + key);
      }
    }
  }

  /** DTO for the claims data file. */
  public static class ClaimsFile {
    public List<ClaimRow> claims;
    public List<ClaimEvidenceRow> claim_evidence;
    public List<LineItemRow> line_items;
    public List<LineItemEvidenceRow> line_item_claim_evidence;
  }

  /** DTO for a claim row. */
  public static class ClaimRow {
    public String ufn;
    public String client;
    public String category;
    public String feeType;
    public String counselPayment;
    public LocalDate concluded;
    public boolean escaped;
    public BigDecimal claimed;
    public UUID submissionId;
    public UUID providerUserId;
  }

  /** DTO for claim evidence. */
  public static class ClaimEvidenceRow {
    public String claimUfn;
    public String client;
    public String fileIdString;
    public Long fileSize;
    public Instant dateAdded;
  }

  /** DTO for a line item row. */
  public static class LineItemRow {
    public String client;
    public String claimUfn;
    public String title;
    public String category;
    public LocalDate date;
  }

  /** DTO for the link between line items and claim evidence. */
  public static class LineItemEvidenceRow {
    public String client;
    public String claimUfn;
    public String evidenceFileIdString;
    public String lineItemTitle;
    public LocalDate lineItemDate;
  }
}
