package com.sobot.chat.utils;

import java.util.HashMap;
import java.util.Map;

//特殊字符还原
public class HtmlUnescaper {
    public static String htmlUnescape(String htmlString) {
        htmlString = htmlString.replace("&amp;", "&");
        htmlString = htmlString.replace("&mid=", "&#38;mid=");//雷霆超链接url里把&mid当参数用了，实际这个在html是数字符号|的意思
        htmlString = htmlString.replace("&times=", "&#38;times=");
        htmlString = htmlString.replace("&para=", "&#38;para=");
        htmlString = htmlString.replace("&image=", "&#38;image=");
        htmlString = htmlString.replace("&and=", "&#38;and=");
        htmlString = htmlString.replace("&uml=", "&#38;uml=");
        htmlString = htmlString.replace("&beta=", "&#38;beta=");
        return htmlString;
    }
}

