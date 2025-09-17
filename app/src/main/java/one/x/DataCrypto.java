package one.x;

import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DataCrypto {
    private final Builder mBuilder;

    private DataCrypto(Builder builder) {
        mBuilder = builder;
    }

    public static DataCrypto getDefault(String key, String salt, byte[] iv) {
        try {
            return Builder.getDefaultBuilder(key, salt, iv).build();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String encrypt(String data)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
               InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException,
        BadPaddingException, IllegalBlockSizeException {
        if (data == null)
            return null;
        SecretKey secretKey = getSecretKey(hashTheKey(mBuilder.getKey()));
        byte[] dataBytes = data.getBytes(mBuilder.getCharsetName());
        Cipher cipher = Cipher.getInstance(mBuilder.getAlgorithm());
        cipher.init(
            Cipher.ENCRYPT_MODE, secretKey, mBuilder.getIvParameterSpec(), mBuilder.getSecureRandom());
        return Base64.encodeToString(cipher.doFinal(dataBytes), mBuilder.getBase64Mode());
    }

    public String encryptOrNull(String data) {
        try {
            return encrypt(data);
        } catch (Exception e) {
            return "";
        }
    }

    private String decrypt(String data)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException,
        NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        String d;
        if (data == null)
            return null;
        byte[] dataBytes = Base64.decode(data, mBuilder.getBase64Mode());
        SecretKey secretKey = getSecretKey(hashTheKey(mBuilder.getKey()));
        Cipher cipher = Cipher.getInstance(mBuilder.getAlgorithm());
        cipher.init(
            Cipher.DECRYPT_MODE, secretKey, mBuilder.getIvParameterSpec(), mBuilder.getSecureRandom());
        try {
            byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
            d = new String(dataBytesDecrypted);
        } catch (Exception e) {
            d = "";
        }
        return d;
    }

    public String decryptOrNull(String data) {
        try {
            return decrypt(data);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSecretKey(char[] key)
    throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(mBuilder.getSecretKeyType());
        KeySpec spec = new PBEKeySpec(key, mBuilder.getSalt().getBytes(mBuilder.getCharsetName()),
                                      mBuilder.getIterationCount(), mBuilder.getKeyLength());
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), mBuilder.getKeyAlgorithm());
    }

    private char[] hashTheKey(String key)
    throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(mBuilder.getDigestAlgorithm());
        messageDigest.update(key.getBytes(mBuilder.getCharsetName()));
        return Base64.encodeToString(messageDigest.digest(), Base64.NO_PADDING).toCharArray();
    }

    private static class Builder {
        private byte[] mIv;
        private int mKeyLength;
        private int mBase64Mode;
        private int mIterationCount;
        private String mSalt;
        private String mKey;
        private String mAlgorithm;
        private String mKeyAlgorithm;
        private String mCharsetName;
        private String mSecretKeyType;
        private String mDigestAlgorithm;
        private String mSecureRandomAlgorithm;
        private SecureRandom mSecureRandom;
        private IvParameterSpec mIvParameterSpec;

        public static Builder getDefaultBuilder(String key, String salt, byte[] iv) {
            return new Builder()
                   .setIv(iv)
                   .setKey(key)
                   .setSalt(salt)
                   .setKeyLength(128)
                   .setKeyAlgorithm()
                   .setCharsetName()
                   .setIterationCount(1)
                   .setDigestAlgorithm("SHA1")
                   .setBase64Mode()
                   .setAlgorithm()
                   .setSecureRandomAlgorithm("SHA1PRNG")
                   .setSecretKeyType();
        }

        private DataCrypto build() throws NoSuchAlgorithmException {
            setSecureRandom(SecureRandom.getInstance(getSecureRandomAlgorithm()));
            setIvParameterSpec(new IvParameterSpec(getIv()));
            return new DataCrypto(this);
        }

        private String getCharsetName() {
            return mCharsetName;
        }

        private Builder setCharsetName() {
            mCharsetName = "UTF8";
            return this;
        }

        private String getAlgorithm() {
            return mAlgorithm;
        }

        private Builder setAlgorithm() {
            mAlgorithm = "AES/CBC/PKCS5Padding";
            return this;
        }

        private String getKeyAlgorithm() {
            return mKeyAlgorithm;
        }

        private Builder setKeyAlgorithm() {
            mKeyAlgorithm = "AES";
            return this;
        }

        private int getBase64Mode() {
            return mBase64Mode;
        }

        private Builder setBase64Mode() {
            mBase64Mode = Base64.DEFAULT;
            return this;
        }

        private String getSecretKeyType() {
            return mSecretKeyType;
        }

        private Builder setSecretKeyType() {
            mSecretKeyType = "PBKDF2WithHmacSHA1";
            return this;
        }

        private String getSalt() {
            return mSalt;
        }

        private Builder setSalt(String salt) {
            mSalt = salt;
            return this;
        }

        private String getKey() {
            return mKey;
        }

        private Builder setKey(String key) {
            mKey = key;
            return this;
        }

        private int getKeyLength() {
            return mKeyLength;
        }

        public Builder setKeyLength(int keyLength) {
            mKeyLength = keyLength;
            return this;
        }

        private int getIterationCount() {
            return mIterationCount;
        }

        public Builder setIterationCount(int iterationCount) {
            mIterationCount = iterationCount;
            return this;
        }

        private String getSecureRandomAlgorithm() {
            return mSecureRandomAlgorithm;
        }

        public Builder setSecureRandomAlgorithm(String secureRandomAlgorithm) {
            mSecureRandomAlgorithm = secureRandomAlgorithm;
            return this;
        }

        private byte[] getIv() {
            return mIv;
        }

        public Builder setIv(byte[] iv) {
            mIv = iv;
            return this;
        }

        private SecureRandom getSecureRandom() {
            return mSecureRandom;
        }

        public void setSecureRandom(SecureRandom secureRandom) {
            mSecureRandom = secureRandom;
        }

        private IvParameterSpec getIvParameterSpec() {
            return mIvParameterSpec;
        }

        public void setIvParameterSpec(IvParameterSpec ivParameterSpec) {
            mIvParameterSpec = ivParameterSpec;
        }

        private String getDigestAlgorithm() {
            return mDigestAlgorithm;
        }

        public Builder setDigestAlgorithm(String digestAlgorithm) {
            mDigestAlgorithm = digestAlgorithm;
            return this;
        }
    }
}
