package com.cloud.util;

import java.io.*;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/7/4 10:32
 */
public class ObjectUtil {
    public static Object deeplyCopy(Serializable obj) {
        try {
            return bytes2object(object2bytes(obj));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static byte[] object2bytes(Serializable obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            baos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object bytes2object(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
