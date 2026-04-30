package uk.gov.justice.laa.authx.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TestJwtBuilder {

  private static final KeyPair TRUSTED_KEY_PAIR = generateKeyPair();
  private static final KeyPair UNTRUSTED_KEY_PAIR = generateKeyPair();

  private String issuer;
  private Set<String> audience = new HashSet<>();
  private Instant issuedAt = Instant.now();
  private Instant expiresAt = Instant.now().plusSeconds(300);
  private final Map<String, Object> claims = new HashMap<>();

  private TestJwtBuilder() {}

  // ---------- entry point ----------
  public static TestJwtBuilder builder() {
    return new TestJwtBuilder();
  }

  // ---------- fluent API ----------

  public static TestJwtBuilder withIssuer(String issuer) {
    return builder().withIssuerInternal(issuer);
  }

  private TestJwtBuilder withIssuerInternal(String issuer) {
    this.issuer = issuer;
    return this;
  }

  public TestJwtBuilder withAudience(String aud) {
    this.audience.add(aud);
    return this;
  }

  public TestJwtBuilder withClaim(String name, Object value) {
    this.claims.put(name, value);
    return this;
  }

  public TestJwtBuilder expired() {
    this.expiresAt = Instant.now().minusSeconds(10);
    this.issuedAt = Instant.now().minusSeconds(60);
    return this;
  }

  // ---------- terminal operation ----------

  private String signWith(PrivateKey privateKey) {
    try {
      JWTClaimsSet.Builder builder =
          new JWTClaimsSet.Builder()
              .issuer(issuer)
              .issueTime(Date.from(issuedAt))
              .expirationTime(Date.from(expiresAt));

      if (!audience.isEmpty()) {
        builder.audience(new ArrayList<>(audience));
      }

      claims.forEach(builder::claim);

      SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), builder.build());

      jwt.sign(new RSASSASigner(privateKey));
      return jwt.serialize();

    } catch (JOSEException e) {
      throw new IllegalStateException(e);
    }
  }

  public String signed() {
    return signWith(TRUSTED_KEY_PAIR.getPrivate());
  }

  public String signedWithDifferentKey() {
    return signWith(UNTRUSTED_KEY_PAIR.getPrivate());
  }

  // ---------- test key material ----------

  private static KeyPair generateKeyPair() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      return gen.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static KeyPair getKeyPair() {
    return TRUSTED_KEY_PAIR;
  }
}
