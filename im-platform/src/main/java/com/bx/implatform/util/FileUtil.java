package com.bx.implatform.util;

public final class FileUtil {


    /**
     * 获取文件后缀
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     *  去除文件扩展名
     *
     * @param fileName 文件名
     * @return
     */
    public static String excludeExtension(String fileName) {
        return fileName.substring(0,fileName.lastIndexOf("."));
    }

    /**
     * 判断文件是否图片类型
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        String[] imageExtension = new String[]{"jpeg", "jpg", "bmp", "png", "webp", "gif"};
        for (String e : imageExtension) {
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }

        return false;
    }
}
