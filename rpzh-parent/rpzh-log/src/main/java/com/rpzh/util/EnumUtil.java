package com.rpzh.util;

/**
 * @author wbl
 * enum util
 */
public class EnumUtil {
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        if (c != null && string != null) {
            try {
                return Enum.valueOf(c, string.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}