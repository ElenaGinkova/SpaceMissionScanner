package space.algorithm;

import space.exception.CipherException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RijndaelTest {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KEY_SIZE_IN_BITS = 128;
    private static SecretKey secretKey;
    private static Rijndael rijndael;

    @BeforeAll
    public static void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        secretKey = keyGenerator.generateKey();
        rijndael = new Rijndael(secretKey);
    }

    @Test
    public void encryptAndDecryptOutput() throws CipherException, IOException {
        String plainText = "California, USA";
        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes;
        String decryptedOutputStr;

        ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();
        try (var in = new ByteArrayInputStream(plainTextBytes)) {
            rijndael.encrypt(in, encryptedOutput);
            encryptedBytes = encryptedOutput.toByteArray();
            assertTrue(encryptedBytes.length > 0, "Encrypted bytes should not be empty");
        }
        try (ByteArrayOutputStream decryptedOutput = new ByteArrayOutputStream()) {
            rijndael.decrypt(new ByteArrayInputStream(encryptedBytes), decryptedOutput);
            decryptedOutputStr =  decryptedOutput.toString(StandardCharsets.UTF_8);
        }
        assertEquals(plainText, decryptedOutputStr);
    }

    @Test
    public void noSecretKeyGiven() {
        assertThrows(IllegalArgumentException.class, () -> new Rijndael(null));
    }
}
