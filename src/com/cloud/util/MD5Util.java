package com.cloud.util;

import java.security.MessageDigest;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/6/10 10:04
 */
public class MD5Util {

    public static String encrypt(String src){
        try {
            // 加密对象，指定加密方式
            MessageDigest md5 = MessageDigest.getInstance("md5");
            // 准备要加密的数据
            byte[] b = src.getBytes();
            // 加密
            byte[] digest = md5.digest(b);
            // 十六进制的字符
            char[] chars = new char[] { '0', '1', '2', '3', '4', '5',
                    '6', '7' , '8', '9', 'A', 'B', 'C', 'D', 'E','F' };
            StringBuffer sb = new StringBuffer();
            // 处理成十六进制的字符串(通常)
            for (byte bb : digest) {
                sb.append(chars[(bb >> 4) & 15]);
                sb.append(chars[bb & 15]);
            }
            // 加密后的字符串
            return sb.toString();
        }catch (Exception e){

        }
        return null;
    }
}
