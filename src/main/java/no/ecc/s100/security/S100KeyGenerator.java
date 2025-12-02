package no.ecc.s100.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import no.ecc.s100.utility.Hex;

/**
 * AES128 키를 랜덤으로 생성하는 유틸리티 클래스
 */
public class S100KeyGenerator {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;

    /**
     * 새로운 AES128 키를 랜덤으로 생성합니다.
     * 
     * @return 생성된 SecretKey
     * @throws GeneralSecurityException 키 생성 중 오류 발생 시
     */
    public static SecretKey generateKey() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * 새로운 AES128 키를 랜덤으로 생성하고 SecureRandom을 사용하여 초기화합니다.
     * 
     * @param secureRandom 사용할 SecureRandom 인스턴스
     * @return 생성된 SecretKey
     * @throws GeneralSecurityException 키 생성 중 오류 발생 시
     */
    public static SecretKey generateKey(SecureRandom secureRandom) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }

    /**
     * 새로운 AES128 키를 랜덤으로 생성하고 바이트 배열로 반환합니다.
     * 
     * @return 생성된 키의 바이트 배열
     * @throws GeneralSecurityException 키 생성 중 오류 발생 시
     */
    public static byte[] generateKeyBytes() throws GeneralSecurityException {
        return generateKey().getEncoded();
    }

    /**
     * 새로운 AES128 키를 랜덤으로 생성하고 16진수 문자열로 반환합니다.
     * 
     * @return 생성된 키의 16진수 문자열 표현
     * @throws GeneralSecurityException 키 생성 중 오류 발생 시
     */
    public static String generateKeyHex() throws GeneralSecurityException {
        return Hex.toString(generateKey().getEncoded());
    }

    /**
     * 새로운 AES128 키를 랜덤으로 생성하고 SecureRandom을 사용하여 초기화한 후 
     * 16진수 문자열로 반환합니다.
     * 
     * @param secureRandom 사용할 SecureRandom 인스턴스
     * @return 생성된 키의 16진수 문자열 표현
     * @throws GeneralSecurityException 키 생성 중 오류 발생 시
     */
    public static String generateKeyHex(SecureRandom secureRandom) throws GeneralSecurityException {
        return Hex.toString(generateKey(secureRandom).getEncoded());
    }

}

