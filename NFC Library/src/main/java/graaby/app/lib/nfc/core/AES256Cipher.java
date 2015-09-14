
package graaby.app.lib.nfc.core;

import android.nfc.Tag;
import android.util.Base64;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Cipher implements Serializable {

    private Cipher encipher = null;
    private Cipher decipher = null;


    protected AES256Cipher(byte[] tagID, byte[] keyBytes, Boolean isTagIDAlreadyIV) {

        byte[] iv;
        try {
            if (!isTagIDAlreadyIV) {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.reset();
                digest.update(tagID);
                ByteBuffer b = ByteBuffer.allocate(16);
                b.put(digest.digest(), 0, 16);
                iv = b.array();
            } else {
                iv = tagID;
            }
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");

            encipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            encipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            decipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static String getIVStringFromTag(Tag tag) {

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(tag.getId());
            ByteBuffer b = ByteBuffer.allocate(16);
            b.put(digest.digest(), 0, 16);
            byte[] iv = b.array();
            return Base64.encodeToString(iv, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public byte[] encryptData(byte[] textBytes) throws IllegalBlockSizeException,
            BadPaddingException {
        return encipher.doFinal(textBytes);
    }

    public String encryptDataToString(byte[] textBytes) throws IllegalBlockSizeException,
            BadPaddingException {
        byte[] encrypted = encryptData(textBytes);
        return Base64.encodeToString(encrypted, Base64.URL_SAFE);
    }

    public byte[] decryptData(byte[] textBytes) throws IllegalBlockSizeException,
            BadPaddingException {
        return decipher.doFinal(textBytes);
    }
}
