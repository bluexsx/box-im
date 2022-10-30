package com.lx.implatform.util;

public class FileUtil {

    /**
     * 获取文件后缀
     *
     * @param fileName  文件名
     * @return boolean
     */
    public static String getFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extension;
    }

    /**
     * 判断文件是否图片类型
     *
     * @param fileName  文件名
     * @return  boolean
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        String[] imageExtension = new String[]{"jpeg", "jpg", "bmp", "png","webp"};
        for (String e : imageExtension){
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }

        return false;
    }
}
