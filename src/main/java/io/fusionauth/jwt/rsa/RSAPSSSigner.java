/*
 * Copyright (c) 2020, FusionAuth, All Rights Reserved
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

package io.fusionauth.jwt.rsa;

import io.fusionauth.jwt.InvalidKeyLengthException;
import io.fusionauth.jwt.JWTSigningException;
import io.fusionauth.jwt.MissingPrivateKeyException;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.Algorithm;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.security.CryptoProvider;
import io.fusionauth.security.DefaultCryptoProvider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Objects;

/**
 * This class can sign a JWT using an RSA Private key.
 *
 * @author Daniel DeGroff
 */
public class RSAPSSSigner implements Signer {
  private final Algorithm algorithm;

  private final CryptoProvider cryptoProvider;

  private final String kid;

  private final RSAPrivateKey privateKey;

  private RSAPSSSigner(Algorithm algorithm, RSAPrivateKey privateKey, String kid, CryptoProvider cryptoProvider) {
    Objects.requireNonNull(algorithm);
    Objects.requireNonNull(cryptoProvider);
    Objects.requireNonNull(privateKey);

    this.algorithm = algorithm;
    this.cryptoProvider = cryptoProvider;
    this.kid = kid;

    this.privateKey = privateKey;
    int keyLength = this.privateKey.getModulus().bitLength();
    if (keyLength < 2048) {
      throw new InvalidKeyLengthException("Key length of [" + keyLength + "] is less than the required key length of 2048 bits.");
    }
  }

  private RSAPSSSigner(Algorithm algorithm, String privateKey, String kid, CryptoProvider cryptoProvider) {
    Objects.requireNonNull(algorithm);
    Objects.requireNonNull(cryptoProvider);
    Objects.requireNonNull(privateKey);

    this.algorithm = algorithm;
    this.cryptoProvider = cryptoProvider;
    this.kid = kid;
    PEM pem = PEM.decode(privateKey);
    if (pem.privateKey == null) {
      throw new MissingPrivateKeyException("The provided PEM encoded string did not contain a private key.");
    }

    this.privateKey = pem.getPrivateKey();
    int keyLength = this.privateKey.getModulus().bitLength();
    if (keyLength < 2048) {
      throw new InvalidKeyLengthException("Key length of [" + keyLength + "] is less than the required key length of 2048 bits.");
    }
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(String privateKey) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(String privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(String privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(String privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, kid, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey The private key.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(RSAPrivateKey privateKey) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey The private key.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(RSAPrivateKey privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey     The private key.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(RSAPrivateKey privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-256 hash.
   *
   * @param privateKey     The private key.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA256Signer(RSAPrivateKey privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS256, privateKey, kid, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(String privateKey) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(String privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(String privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(String privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, kid, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey The private key.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(RSAPrivateKey privateKey) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey The private key.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(RSAPrivateKey privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey     The private key.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(RSAPrivateKey privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-384 hash.
   *
   * @param privateKey     The private key.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA384Signer(RSAPrivateKey privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS384, privateKey, kid, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(String privateKey) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(String privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(String privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey     The private key PEM expected to be in PKCS#1 or PKCS#8 format.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(String privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, kid, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey The private key.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(RSAPrivateKey privateKey) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, null, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey The private key.
   * @param kid        The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(RSAPrivateKey privateKey, String kid) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, kid, new DefaultCryptoProvider());
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey     The private key.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(RSAPrivateKey privateKey, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, null, cryptoProvider);
  }

  /**
   * Build a new RSA signer using a SHA-512 hash.
   *
   * @param privateKey     The private key.
   * @param kid            The key identifier. This will be used by the JWTEncoder to write the 'kid' header.
   * @param cryptoProvider The crypto provider used to get the RSA signature Algorithm.
   * @return a new RSA signer.
   */
  public static RSAPSSSigner newSHA512Signer(RSAPrivateKey privateKey, String kid, CryptoProvider cryptoProvider) {
    return new RSAPSSSigner(Algorithm.PS512, privateKey, kid, cryptoProvider);
  }

  @Override
  public Algorithm getAlgorithm() {
    return algorithm;
  }

  @Override
  public String getKid() {
    return kid;
  }

  public byte[] sign(String message) {
    Objects.requireNonNull(message);

    try {
      Signature signature = cryptoProvider.getSignatureInstance("RSASSA-PSS");
      signature.setParameter(new PSSParameterSpec(algorithm.getName(), "MGF1", new MGF1ParameterSpec(algorithm.getName()), algorithm.getSaltLength(), 1));
      signature.initSign(privateKey);
      signature.update(message.getBytes(StandardCharsets.UTF_8));
      return signature.sign();
    } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | InvalidAlgorithmParameterException e) {
      throw new JWTSigningException("An unexpected exception occurred when attempting to sign the JWT", e);
    }
  }
}
