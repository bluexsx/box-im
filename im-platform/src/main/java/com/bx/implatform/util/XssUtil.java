package com.bx.implatform.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XssUtil {

    private XssUtil() {
    }

    private static final String XSS_PATTERN = "((?i)<script.*?>)|((?i)alert\\s*\\()|((?i)prompt\\s*\\()|((?i)document\\.cookie)|((?i)location\\.href)|((?i)window\\.location)|((?i)onerror\\s*\\()|((?i)eval\\s*\\()|((?i)window\\.open\\s*\\()|((?i)innerHTML)|((?i)onclick\\s*\\()|((?i)onmouseover\\s*\\()|((?i)onsubmit\\s*\\()|((?i)onload\\s*\\()|((?i)onfocus\\s*\\()|((?i)onblur\\s*\\()|((?i)onkeyup\\s*\\()|((?i)onkeydown\\s*\\()|((?i)onkeypress\\s*\\()|((?i)onmouseout\\s*\\()|((?i)src=)|((?i)href=)|((?i)style=)|((?i)background=)|((?i)expression\\s*\\()|((?i)XMLHttpRequest\\s*\\()|((?i)ActiveXObject\\s*\\()|((?i)iframe)|((?i)document\\.write\\s*\\()|((?i)document\\.writeln\\s*\\()|((?i)setTimeout\\s*\\()|((?i)setInterval\\s*\\()|((?i)onreadystatechange\\s*\\()|((?i)appendChild\\s*\\()|((?i)createTextNode\\s*\\()|((?i)createElement\\s*\\()|((?i)getElementsByTagName\\s*\\()|((?i)getElementsByClassName\\s*\\()|((?i)querySelector\\s*\\()|((?i)querySelectorAll\\s*\\()|((?i)document\\.location)|((?i)document\\.body\\.innerHTML)|((?i)document\\.forms)|((?i)document\\.images)|((?i)document\\.links)|((?i)document\\.URL)|((?i)document\\.domain)|((?i)document\\.referrer)|((?i)history\\.back\\s*\\()";
    private static final Pattern PATTERN = Pattern.compile(XSS_PATTERN);

    public static boolean checkXss(String inputString) {
        if (inputString != null) {
            Matcher matcher = PATTERN.matcher(inputString);
            return matcher.find();
        }
        return false;
    }
}
