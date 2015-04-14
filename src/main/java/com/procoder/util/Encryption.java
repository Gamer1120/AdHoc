package com.procoder.util;

/**
 * Created by reneb_000 on 11-4-2015.
 */
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    public static final byte[] KEY = new byte[]{43, 93, 99, -108, 2, 7, 32, 16, -72, 121, 68, 127, 99, 125, -88, 21};


    /*
    public Encryption(){
        /*
        byte[] encrypted = Encryption.getEncrypted("Dit is een test".getBytes(), new SecretKeySpec(KEY, 0, KEY.length, "AES"));
        byte[] decrypted = Encryption.getDecrypted(encrypted, new SecretKeySpec(KEY, 0, KEY.length, "AES"));
        System.out.println(new String(decrypted));


        /*
        SecretKey sk = Encryption.generateKey();
        byte[] data = sk.getEncoded();
        for(int i=0; i<data.length;i++){
            System.out.print(data[i]+" ");
        }

        //System.out.println(data.length);
    }

    public static void main(String[] args){
        new Encryption();

    }*/




    public static byte[] getEncrypted(byte[] array, SecretKey key){
        try {
            SecretKey secretKey = key;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            return cipher.doFinal(array);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getDecrypted(byte[] array, SecretKey key){
        try {
            SecretKey secretKey = key;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            return cipher.doFinal(array);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static SecretKey generateKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}