package com.github.walterfan.msa.common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;


public class Encryptor {
     
    private static byte[] IV_BYTES = "1234567890123456".getBytes();
    
    public static final String ENC_CBC_NOPADDING = "AES/CBC/NoPadding";
    
    private static final String ENC_ALGORITHM = "AES";

    private String algorithm = ENC_ALGORITHM;
    
    private static final int ENC_KEY_LEN = 16;

    public static final String ENC_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";


    private AlgorithmParameterSpec ivParamSpec = null;

    private SecretKeySpec keySpec = null;
    

    public Encryptor() {
        
    }

    public Encryptor(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] encode(byte[] bytes, byte[] kbytes) throws Exception {
       /* if(aesKey.getBytes().length % ENC_KEY_LEN != 0) {
            throw new Exception("invalid AES Key length(128, 192, or 256 bits)");
        }*/
        
        SecretKeySpec keySpec = new SecretKeySpec(kbytes, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        if(ENC_CBC_NOPADDING.equals(algorithm)) {
            AlgorithmParameterSpec paraSpec = new IvParameterSpec(IV_BYTES);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paraSpec);
        }
        else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }
        return cipher.doFinal(bytes);
    }

    public byte[] decode(byte[] encryptedBytes, byte[] kbytes) throws Exception {
    /*    if(aesKey.getBytes().length % ENC_KEY_LEN != 0) {
            throw new Exception("invalid AES Key length(128, 192, or 256 bits)");
        }*/
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(kbytes, algorithm);
        if(ENC_CBC_NOPADDING.equals(algorithm)) {
            AlgorithmParameterSpec paraSpec = new IvParameterSpec(IV_BYTES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paraSpec);
        }
        else {
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        }
        
        return cipher.doFinal(encryptedBytes);
    }

   public  byte[] makeKey() throws NoSuchAlgorithmException {
    	KeyGenerator keygen = KeyGenerator.getInstance(algorithm);    
    	SecretKey skey = keygen.generateKey();
    	byte[] raw = skey.getEncoded();
    	//SecretKeySpec skeySpec = new SecretKeySpec(raw, algorithm);
    	return raw;
    }

    //------------------------- new implementation ---------------//


    public static byte[] makeKeyBySHA1(String key, int len) {
        byte[] seed = EncodeUtils.sha1(key);
        //assert (seed.length == SHA1_LEN);
        byte[] raw = new byte[len];
        System.arraycopy(seed, 0, raw, 0, len);
        return raw;
    }


    public static byte[] makeKeyBySHA2(String key, int len) {
        byte[] seed = EncodeUtils.sha2(key);
        byte[] raw = new byte[len];
        System.arraycopy(seed, 0, raw, 0, len);
        return raw;
    }


    public Encryptor(String algorithm, byte[] keyBytes,  byte[] ivBytes) {
        this.algorithm = algorithm;
        this.keySpec =  new SecretKeySpec(keyBytes, algorithm);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public Encryptor(String algorithm, String strKey,  String strIv) {
        byte[] keyBytes = makeKeyBySHA2(strKey, 16);
        byte[] ivBytes = makeKeyBySHA2(strIv, 16);
        this.algorithm = algorithm;
        this.keySpec =  new SecretKeySpec(keyBytes, ENC_ALGORITHM);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public Encryptor( String strKey,  String strIv) {
        byte[] keyBytes = makeKeyBySHA2(strKey, 16);
        byte[] ivBytes = makeKeyBySHA2(strIv, 16);
        this.algorithm = ENC_CBC_PKCS5PADDING;
        this.keySpec =  new SecretKeySpec(keyBytes, ENC_ALGORITHM);
        this.ivParamSpec = new IvParameterSpec(ivBytes);

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public SecretKeySpec getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(SecretKeySpec keySpec) {
        this.keySpec = keySpec;
    }

    public AlgorithmParameterSpec getIvParamSpec() {
        return ivParamSpec;
    }

    public void setIvParamSpec(AlgorithmParameterSpec ivParamSpec) {
        this.ivParamSpec = ivParamSpec;
    }

    public byte[] encrypt(byte[] inputBytes) throws Exception {

        Cipher cipher = Cipher.getInstance(algorithm);

        if(null == ivParamSpec)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        else
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

        //byte[] cipherText = new byte[cipher.getOutputSize(inputBytes.length)];
        //int ctLength = cipher.update(inputBytes, 0, inputBytes.length, cipherText, 0);
        return cipher.doFinal(inputBytes);

    }

    /*
    *     Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, AES_ALGORITHM));
            return cipher.doFinal(bytes);
    * */
    public byte[] decrypt(byte[] cipherText)  throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);

        if(null == ivParamSpec)
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        else
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
        //int ctLength = cipherText.length;
        //byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
        //int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
        return cipher.doFinal(cipherText);
        //return plainText;
    }

    public SecretKeySpec generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
        SecretKey skey = keygen.generateKey();
        byte[] raw = skey.getEncoded();
        return new SecretKeySpec(raw, algorithm);
    }


 
}
