/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.common.utils;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

public class ByteArray {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String toHexString(byte[] data) {
        return data == null ? "" : Hex.toHexString(data);
    }

    public static byte[] fromHexString(String data) {
        if (data == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

    public static long toLong(byte[] b) {
        if (b == null || b.length == 0) {
            return 0;
        }
        return new BigInteger(1, b).longValue();
    }

    public static byte[] fromString(String str) {
        if (str == null) {
            return null;
        }

        return str.getBytes();
    }

    public static String toStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        return new String(byteArray);
    }

    public static byte[] fromLong(long val) {
        return ByteBuffer.allocate(8).putLong(val).array();
    }

    /**
     * Generate a subarray of a given byte array.
     *
     * @param input the input byte array
     * @param start the start index
     * @param end the end index
     * @return a subarray of <tt>input</tt>, ranging from <tt>start</tt> (inclusively) to <tt>end</tt>
     * (exclusively)
     */
    public static byte[] subArray(byte[] input, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, end - start);
        return result;
    }

    public static byte[] fromBytes21List(List<byte[]> list) {
        byte[] data = new byte[21*list.size()];
        for (int i=0; i< list.size();i++) {
            if (list.get(i).length != 21) {
                throw new IllegalArgumentException("should be 21 bytes for each element in the list");
            }
            System.arraycopy(list.get(i), 0, data, i*21, 21);
        }
        return data;
    }


    public static byte[] convertToTronAddress(byte[] address) {
        if (address.length == 20) {
            byte[] newAddress = new byte[21];
            byte[] temp = new byte[]{0x41};
            System.arraycopy(temp, 0, newAddress, 0, temp.length);
            System.arraycopy(address, 0, newAddress, temp.length, address.length);
            address = newAddress;
        }
        return address;
    }
}
