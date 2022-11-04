package com.nuonuo.qlvbpush;

/**
 * @author ：leo
 * @version 1
 * @创建时间 on 2022/11/3 21:10
 * @描述
 */
class NV21 {

    public static byte[] rotation(int angel, byte[] data, int imageWidth, int imageHeight) {
        // 后置 90度 angel = 90;
        //前置angel=270;
        if (angel == 90)
            return NV21_rotate_to_90(data, new byte[data.length], imageHeight, imageWidth);
        if (angel == 180)
            return NV21_rotate_to_180(data, new byte[data.length], imageWidth, imageHeight);
        if (angel == 270)
            return NV21_rotate_to_270(data, new byte[data.length], imageHeight, imageWidth);
        return data;
    }

    public static byte[] NV21_rotate_to_270(byte[] nv21_data, byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int i = 0;

        // Rotate the Y luma
        for (int x = width - 1; x >= 0; x--) {
            int offset = 0;
            for (int y = 0; y < height; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }

        // Rotate the U and V color components
        i = y_size;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i++;
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }
        return nv21_rotated;
    }

    public static byte[] NV21_rotate_to_180(byte[] nv21_data, byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        int i = 0;
        int count = 0;

        for (i = y_size - 1; i >= 0; i--) {
            nv21_rotated[count] = nv21_data[i];
            count++;
        }

        for (i = buffser_size - 1; i >= y_size; i -= 2) {
            nv21_rotated[count++] = nv21_data[i - 1];
            nv21_rotated[count++] = nv21_data[i];
        }
        return nv21_rotated;
    }

    public static byte[] NV21_rotate_to_90(byte[] nv21_data, byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;

        // Rotate the Y luma
        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset -= width;
            }
        }

        // Rotate the U and V color components
        i = buffser_size - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i--;
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return nv21_rotated;
    }
}
