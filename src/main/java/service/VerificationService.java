package service;

import java.util.Random;

public class VerificationService {
    private static String generatedCode;

    public static String generateCode() {
        generatedCode = String.valueOf(new Random().nextInt(900000) + 100000); // 6 chiffres
        return generatedCode;
    }

    public static boolean verifyCode(String code) {
        return generatedCode != null && generatedCode.equals(code);
    }
}
