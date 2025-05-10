package app.varlorg.unote;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Note
{
    private int id;
    private String titre;
    private String noteContent;
    private String dateCreation;
    private String dateModification;
    private String passwordHash;
    private String password;
    private boolean ciphered = false;
    private boolean selected = false;

    public Note(String t, String c, String passwordHash, boolean ciphered)
    {
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String           date = df.format(Calendar.getInstance().getTime());

        this.dateCreation     = date;
        this.dateModification = date;
        this.titre            = t;
        this.noteContent      = c;
        this.passwordHash = passwordHash;
        this.password = null;
        this.ciphered = ciphered;
    }

    public Note(String t, String c)
    {
        this( t,  c, null, false);
    }

    public Note()
    {
        this("", "");
    }

    public int getId()
    {
        return(this.id);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitre()
    {
        return(this.titre);
    }

    public void setTitre(String titre)
    {
        this.titre = titre;
    }

    public String getNote()
    {
        return(this.noteContent);
    }

    public String getNoteHead(int nbChar)
    {
        int max = nbChar;
        int min = Math.min(max, this.noteContent.length());

        if (max == 0){
            return "";
        }
        else if (max < this.noteContent.length())
        {
            return(this.noteContent.substring(0, min) + "...");
        }
        else
        {
            return(this.noteContent.substring(0, min));
        }
    }

    public void setNote(String c)
    {
        this.noteContent = c;
    }

    public String getDateCreationFormated()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date             d   = null;

        try {
            d = sdf.parse(this.dateCreation);
        } catch (ParseException e) {
            Log.e(BuildConfig.APPLICATION_ID, "exception getDateCreationFormated", e);
        }
        sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
        return(sdf.format(d));
    }

    public String getDateCreation()
    {
        return(this.dateCreation);
    }

    public void setDateCreation(String dc)
    {
        this.dateCreation = dc;
    }

    public String getDateModificationFormated()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date             d   = null;

        try {
            d = sdf.parse(this.dateModification);
        } catch (ParseException e) {
            Log.e(BuildConfig.APPLICATION_ID, "exception getDateModificationFormated", e);
        }
        sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
        return(sdf.format(d));
    }

    public String getDateModification()
    {
        return(this.dateModification);
    }

    public void setDateModification(String dc)
    {
        this.dateModification = dc;
    }
    public String getPassword()
    {
        return(this.password);
    }

    public void setPassword(String pw)
    {
        // pw is null for removing password
        this.password = pw;
    }
    public String getHashPassword()
    {
        return(this.passwordHash);
    }

    public void setHashPassword(String pw)
    {
        // pw is null for removing password
        this.passwordHash = pw;
    }
    public boolean isSelected()
    {
        return(this.selected);
    }
    public void setSelected(boolean checked)
    {
        this.selected  = checked;
    }
    public void setCiphered(boolean ciphered)
    {
        this.ciphered  = ciphered;
    }
    public boolean isCiphered()
    {
        return this.ciphered;
    }
/*
    public static void setKey(String myKey)
    {
    MessageDigest sha = null;
        try {
    key = myKey.getBytes("UTF-8");
    sha = MessageDigest.getInstance("SHA-1");
    key = sha.digest(key);
    key = Arrays.copyOf(key, 16);
    secretKey = new SecretKeySpec(key, "AES");
}
        catch (NoSuchAlgorithmException e) {
    e.printStackTrace();
}
        catch (UnsupportedEncodingException e) {
    e.printStackTrace();
}
}

public static String encrypt(String strToEncrypt, String secret)
{
    try
    {
        setKey(secret);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    }
    catch (Exception e)
    {
        System.out.println("Error while encrypting: " + e.toString());
    }
    return null;
}

public static String decrypt(String strToDecrypt, String secret)
{
    try
    {
        setKey(secret);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }
    catch (Exception e)
    {
        System.out.println("Error while decrypting: " + e.toString());
    }
    return null;
}*/

/*****
    public String encrypt( String key) throws Exception {
        String plainText= this.noteContent;
        byte[] clean = plainText.getBytes();

        // Generating IV.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key.getBytes("UTF-8"));
        byte[] keyBytes = new byte[16];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted part.
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    public String decrypt( String key) throws Exception {
        String encryptedIvText = this.noteContent;
        int ivSize = 16;
        int keySize = 16;

        byte[] encryptedIvTextBytes =  Base64.getDecoder().decode(encryptedIvText);
        // Extract IV.
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

        // Hash key.
        byte[] keyBytes = new byte[keySize];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        System.arraycopy(md.digest(), 0, keyBytes, 0, keyBytes.length);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Decrypt.
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

        return new String(decrypted);
    }
****/
    /*
    public String encrypt(String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] iv = generateIv();
        SecretKey secretKey = deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] encryptedBytes = cipher.doFinal(this.noteContent.getBytes(StandardCharsets.UTF_8));

        // Encode and combine salt, IV, and ciphertext for storage
        byte[] combinedData = new byte[SALT_SIZE + IV_SIZE + encryptedBytes.length];
        System.arraycopy(salt, 0, combinedData, 0, SALT_SIZE);
        System.arraycopy(iv, 0, combinedData, SALT_SIZE, IV_SIZE);
        System.arraycopy(encryptedBytes, 0, combinedData, SALT_SIZE + IV_SIZE, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combinedData);
    }

    public String decrypt(String password) throws Exception {
        byte[] combinedData = Base64.getDecoder().decode(this.noteContent);

        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedBytes = new byte[combinedData.length - SALT_SIZE - IV_SIZE];

        System.arraycopy(combinedData, 0, salt, 0, SALT_SIZE);
        System.arraycopy(combinedData, SALT_SIZE, iv, 0, IV_SIZE);
        System.arraycopy(combinedData, SALT_SIZE + IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

        SecretKey secretKey = deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] generateIv() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);
        return iv;
    }*/
}
