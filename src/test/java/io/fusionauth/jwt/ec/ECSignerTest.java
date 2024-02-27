/*
 * Copyright (c) 2017-2019, FusionAuth, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package io.fusionauth.jwt.ec;

import io.fusionauth.jwks.JSONWebKeyBuilder;
import io.fusionauth.jwt.BaseJWTTest;
import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.InvalidKeyTypeException;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.pem.PEMEncoder;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.security.BCFIPSCryptoProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Daniel DeGroff
 */
public class ECSignerTest extends BaseJWTTest {
  @Test
  public void test_invalidKey() {
    // RSA private key cannot be used for an EC signer
    try {
      ECSigner.newSHA256Signer(readFile("rsa_private_key_2048.pem"));
      fail("Expected exception.");
    } catch (InvalidKeyTypeException e) {
      assertEquals(e.getMessage(), "Expecting a private key of type [ECPrivateKey], but found [RSAPrivateCrtKeyImpl].");
    }

    try {
      ECSigner.newSHA256Signer(PEM.decode(readFile("rsa_private_key_2048.pem")).privateKey);
      fail("Expected exception.");
    } catch (InvalidKeyTypeException e) {
      assertEquals(e.getMessage(), "Expecting a private key of type [ECPrivateKey], but found [RSAPrivateCrtKeyImpl].");
    }
  }

  @Test(invocationCount = 1000)
  public void round_trip_raw1() throws Exception {
    // Generate a key-pair and sign and verify a message
    KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
    ECGenParameterSpec parameterSpec = new ECGenParameterSpec("secp384r1");
    g.initialize(parameterSpec);
    KeyPair pair = g.generateKeyPair();
    System.out.println(new PEMEncoder().encode(pair.getPublic()));
    System.out.println(new PEMEncoder().encode(pair.getPrivate()));
    System.out.println(JWTUtils.generateJWS_kid_S256(new JSONWebKeyBuilder().build(pair.getPublic())));

    // Instance of signature class with SHA256withECDSA algorithm
    Signature signature = Signature.getInstance("SHA384withECDSA");
    signature.initSign(pair.getPrivate());
    Signer signer = ECSigner.newSHA384Signer(pair.getPrivate());
    Verifier verifier = ECVerifier.newVerifier(pair.getPublic());

    for (int i = 0; i < 250; i++) {
      JWT jwt = new JWT()
          .setSubject("1234567890")
          .addClaim("name", "John Doe " + i)
          .addClaim("admin", true)
          .addClaim("iat", 1516239022);

      String encodedJWT = JWT.getEncoder().encode(jwt, signer, header
          -> header.set("kid", JWTUtils.generateJWS_kid_S256(new JSONWebKeyBuilder().build(pair.getPublic()))));

        JWT actual = JWT.getDecoder().decode(encodedJWT, verifier);
        assertEquals(actual.subject, jwt.subject);
        assertEquals(actual.getString("name"), jwt.getString("name"));
//      try {
//        System.out.println("Good " + encodedJWT);
//      } catch (InvalidJWTSignatureException e) {
//        System.out.println("Bad " + encodedJWT);
//      }
    }

//    for (int i = 0; i < 250; i++) {
//      // Sign a message
//      byte[] bytes = new byte[i+1];
//      new SecureRandom().nextBytes(bytes);
//      signature.update(bytes);
//      byte[] signatureBytes = signature.sign();
//
//      // Validation
//      Signature verifier = Signature.getInstance("SHA512withECDSA");
//      verifier.initVerify(pair.getPublic());
//      verifier.update(bytes);
//      boolean verified = verifier.verify(signatureBytes);
//      if (!verified) {
//        System.out.println("Public: " + new String(pair.getPublic().getEncoded(), StandardCharsets.UTF_8));
//        System.out.println("Private: " + new String(pair.getPrivate().getEncoded(), StandardCharsets.UTF_8));
//      }
//      assertTrue(verified);
//    }
  }

  @Test
  public void round_trip_raw2() throws Exception {
    // Use a real public / private key in PEM format to sign a verify a message
    ECPublicKey publicKey = PEM.decode(new String(Files.readAllBytes(Paths.get("src/test/resources/ec_public_key_p_256.pem")))).getPublicKey();
    ECPrivateKey privateKey = PEM.decode(new String(Files.readAllBytes(Paths.get("src/test/resources/ec_private_key_p_256.pem")))).getPrivateKey();

    // Instance of signature class with SHA256withECDSA algorithm
    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign(privateKey);

    // Sign a message
    String message = "text ecdsa with sha256";
    signature.update((message).getBytes(StandardCharsets.UTF_8));
    byte[] signatureBytes = signature.sign();

    // Validation
    Signature verifier = Signature.getInstance("SHA256withECDSA");
    verifier.initVerify(publicKey);
    verifier.update(message.getBytes(StandardCharsets.UTF_8));
    assertTrue(verifier.verify(signatureBytes));
  }

  @Test
  public void test_private_pem_parsing() {
    assertNotNull(ECSigner.newSHA256Signer(readFile("ec_private_key_p_256.pem")));
    assertNotNull(ECSigner.newSHA256Signer(readFile("ec_private_key_p_384.pem")));
    assertNotNull(ECSigner.newSHA256Signer(readFile("ec_private_key_p_521.pem")));

    assertNotNull(ECSigner.newSHA384Signer(readFile("ec_private_key_p_256.pem")));
    assertNotNull(ECSigner.newSHA384Signer(readFile("ec_private_key_p_384.pem")));
    assertNotNull(ECSigner.newSHA384Signer(readFile("ec_private_key_p_521.pem")));

    assertNotNull(ECSigner.newSHA512Signer(readFile("ec_private_key_p_256.pem")));
    assertNotNull(ECSigner.newSHA512Signer(readFile("ec_private_key_p_384.pem")));
    assertNotNull(ECSigner.newSHA512Signer(readFile("ec_private_key_p_521.pem")));

    // With kid
    assertEquals(ECSigner.newSHA256Signer(readFile("ec_private_key_p_256.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA256Signer(readFile("ec_private_key_p_384.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA256Signer(readFile("ec_private_key_p_521.pem"), "abc").getKid(), "abc");

    assertEquals(ECSigner.newSHA384Signer(readFile("ec_private_key_p_256.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA384Signer(readFile("ec_private_key_p_384.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA384Signer(readFile("ec_private_key_p_521.pem"), "abc").getKid(), "abc");

    assertEquals(ECSigner.newSHA512Signer(readFile("ec_private_key_p_256.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA512Signer(readFile("ec_private_key_p_384.pem"), "abc").getKid(), "abc");
    assertEquals(ECSigner.newSHA512Signer(readFile("ec_private_key_p_521.pem"), "abc").getKid(), "abc");

    // With provided crypto provider
    ECSigner.newSHA256Signer(readFile("ec_private_key_p_256.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA256Signer(readFile("ec_private_key_p_384.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA256Signer(readFile("ec_private_key_p_521.pem"), new BCFIPSCryptoProvider());

    ECSigner.newSHA384Signer(readFile("ec_private_key_p_256.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA384Signer(readFile("ec_private_key_p_384.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA384Signer(readFile("ec_private_key_p_521.pem"), new BCFIPSCryptoProvider());

    ECSigner.newSHA512Signer(readFile("ec_private_key_p_256.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA512Signer(readFile("ec_private_key_p_384.pem"), new BCFIPSCryptoProvider());
    ECSigner.newSHA512Signer(readFile("ec_private_key_p_521.pem"), new BCFIPSCryptoProvider());
  }
}
