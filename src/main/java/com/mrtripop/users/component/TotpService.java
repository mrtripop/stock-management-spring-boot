package com.mrtripop.users.component;

import com.mrtripop.config.TotpProperties;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TotpService {
  private final TotpProperties totpProperties;

  public String generateSecret() {
    SecretGenerator secretGenerator = new DefaultSecretGenerator();
    return secretGenerator.generate();
  }

  public boolean validateCode(String secret, String code) {
    TimeProvider timeProvider = new SystemTimeProvider();
    CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, totpProperties.getDigits());
    DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    verifier.setAllowedTimePeriodDiscrepancy(1);
    return verifier.isValidCode(secret, code);
  }

  public String getQrCodeDataUri(String username, String secret) throws com.mrtripop.exception.ApplicationException {
    try {
      QrData data = new QrData.Builder()
          .label(username)
          .secret(secret)
          .issuer(totpProperties.getIssuer())
          .algorithm(HashingAlgorithm.SHA1)
          .digits(totpProperties.getDigits())
          .period(totpProperties.getPeriod())
          .build();
      QrGenerator generator = new ZxingPngQrGenerator();
      byte[] imageData = generator.generate(data);
      String imageDataBase64 = java.util.Base64.getEncoder().encodeToString(imageData);
      return "data:image/png;base64," + imageDataBase64;
    } catch (Exception e) {
      log.error("Failed to generate QR code data URI", e);
      throw new com.mrtripop.exception.ApplicationException(
          com.mrtripop.users.constant.ErrorCode.AUTH_TOKEN_INVALID,
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}