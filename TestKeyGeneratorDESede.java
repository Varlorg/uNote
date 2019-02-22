
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class TestKeyGeneratorDESede {

    public static void main(String[] args) {

        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("DESede");
            keyGen.init(168);
            SecretKey cle = keyGen.generateKey();
            System.out.println("cle (" + cle.getAlgorithm() + "," + cle.getFormat()
                    + ") : " + new String(cle.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

