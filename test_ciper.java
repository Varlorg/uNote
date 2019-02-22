
import java.lang.Exception;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class test_ciper {
    public String encrypt(String string) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] stringBytes = string.getBytes("UTF-8");
        byte[] encryptedBytes = cipher.doFinal(stringBytes);
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT);
    }

    public String decrypt(String string) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] stringBytes = android.util.Base64.decode(string.getBytes(), android.util.Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(stringBytes);
        return new String(decryptedBytes,"UTF-8");
    }

    public void main() {

        String s = "aa";
        String e = encrypt(s);
    }
}
