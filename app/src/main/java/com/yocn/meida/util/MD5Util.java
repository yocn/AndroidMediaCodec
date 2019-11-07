package com.yocn.meida.util;

import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密类
 *
 * @author Neusoft.E3
 */
public class MD5Util {
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String byteArrayToHexString(byte[] aBytes) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < aBytes.length; i++) {
            resultSb.append(byteToHexString(aBytes[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte aBytes) {
        int n = aBytes;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String encode(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        String resultString = null;

        try {
            resultString = string;
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString
                    .getBytes()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultString;
    }

    /**
     * 计算文件前n个字节的md5
     *
     * @param inputFile
     * @param length    n个字节
     * @return md5
     */
    public static String fileMD5(String inputFile, int length) {
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;

        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream,
                    messageDigest);

            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[length];
            digestInputStream.read(buffer);

            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();

            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();

            // 同样，把字节数组转换成字符串
            return byteArrayToHexString(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String fileMD5(String inputFile) {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;

        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream,
                    messageDigest);

            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0)
                ;

            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();

            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();

            // 同样，把字节数组转换成字符串
            return byteArrayToHexString(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
