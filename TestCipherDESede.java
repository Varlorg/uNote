
import java.util.Base64;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TestCipherDESede {

    public static void main(String[] args) {

        final String message = "Mon message a traiter";

        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("DESede");
            keyGen.init(168);
            SecretKey cle = keyGen.generateKey();
            System.out.println("cle : " + new String(cle.getEncoded()));

            String passwd = "toto";
            // decode the base64 encoded string
            byte[] decodedKey = Base64.getDecoder().decode(passwd);
            // rebuild key using SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DESede");
            System.out.println("passwd : " + new String(cle.getEncoded()));

            byte[] enc = encrypter(message, cle);
            System.out.println("texte encrypte : " + new String(enc));
            byte[] enc_pw = encrypter(message, originalKey);
            System.out.println("texte encrypte pw : " + new String(enc_pw));

            String dec = decrypter(enc, cle);
            System.out.println("texte decrypte : " + dec);
            //String dec_pw = decrypter(enc_pw, cle);
            //System.out.println("texte decrypte pw : " + new String(dec_pw));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypter(final String message, SecretKey cle)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
                              InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
                       Cipher cipher = Cipher.getInstance("DESede");
                       cipher.init(Cipher.ENCRYPT_MODE, cle);
                       byte[] donnees = message.getBytes();

                       return cipher.doFinal(donnees);
    }

    public static String decrypter(final byte[] donnees, SecretKey cle)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
                              InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
                       Cipher cipher = Cipher.getInstance("DESede");
                       cipher.init(Cipher.DECRYPT_MODE, cle);

                       return new String(cipher.doFinal(donnees));
    }
}
