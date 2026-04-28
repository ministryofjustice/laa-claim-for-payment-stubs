package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.support;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Utility class for generating test JWT keys.
 */
public final class TestJwtKeys {

  /** Generates a test RSA key pair for JWT signing and verification. */
  public static RSAKey rsa() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair keyPair = kpg.generateKeyPair();

      return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
          .privateKey((RSAPrivateKey) keyPair.getPrivate())
          .keyUse(KeyUse.SIGNATURE)
          .keyID("test-key")
          .build();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate test RSA key", e);
    }
  }
}
