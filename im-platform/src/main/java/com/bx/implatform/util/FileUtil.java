package com.bx.implatform.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;

public final class FileUtil {

    private static final String OCTET_STREAM = "application/octet-stream";

    /**
     * 浏览器可能当页面/脚本执行的后缀
     */
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "html", "htm", "shtml", "xhtml", "svg", "svgz", "xml", "xsl", "xsd",
        "js", "mjs", "cjs", "css", "wasm", "htc"
    );

    /**
     * Content-Type 中出现这些片段则视为危险（兼容带 charset 等参数）
     */
    private static final Set<String> DANGEROUS_MIME_KEYWORDS = Set.of(
        "text/html", "application/xhtml", "javascript", "image/svg", "text/xml", "application/xml"
    );

    private FileUtil() {
    }

    public static String getFileExtension(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String excludeExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase(Locale.ROOT);
        return Set.of("jpeg", "jpg", "bmp", "png", "webp", "gif").contains(extension);
    }

    /**
     * 危险后缀或危险 Content-Type → application/octet-stream；其余沿用客户端类型。
     */
    public static String resolveContentType(String fileName, String clientContentType) {
        if (isDangerousExtension(fileName) || isDangerousMime(clientContentType)) {
            return OCTET_STREAM;
        }
        return StringUtils.isNotBlank(clientContentType) ? clientContentType : OCTET_STREAM;
    }

    private static boolean isDangerousExtension(String fileName) {
        return DANGEROUS_EXTENSIONS.contains(getFileExtension(fileName).toLowerCase(Locale.ROOT));
    }

    private static boolean isDangerousMime(String mime) {
        if (StringUtils.isBlank(mime)) {
            return false;
        }
        String m = mime.toLowerCase(Locale.ROOT);
        return DANGEROUS_MIME_KEYWORDS.stream().anyMatch(m::contains);
    }
}
