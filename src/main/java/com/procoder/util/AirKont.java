package com.procoder.util;

public class AirKont {

    public static Byte[] toObjectArray(byte[] arr) {
        Byte[] result = new Byte[arr.length];

        int i = 0;
        for(byte b: arr) {
            result[i++] = b;
        }

        return result;
    }


    public static byte[] toPrimitiveArray(Byte[] arr) {
        byte[] result = new byte[arr.length];

        int i = 0;
        for(Byte b: arr) {
            result[i++] = b;
        }

        return result;
    }

}
