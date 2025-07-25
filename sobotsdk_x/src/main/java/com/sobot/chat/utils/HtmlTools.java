package com.sobot.chat.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.HttpUtils.FileCallBack;
import com.sobot.chat.widget.LinkMovementClickMethod;
import com.sobot.chat.widget.emoji.InputHelper;
import com.sobot.chat.widget.html.SobotCustomTagHandler;
import com.sobot.chat.widget.rich.EmailSpan;
import com.sobot.chat.widget.rich.MyURLSpan;
import com.sobot.chat.widget.rich.PhoneSpan;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTools {

    private static HtmlTools instance;

    public static HtmlTools getInstance(Context context) {
        if (instance == null) {
            instance = new HtmlTools(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Regular expression to match all IANA top-level domains for WEB_URL. List
     * accurate as of 2011/07/18. List taken from:
     * http://data.iana.org/TLD/tlds-alpha-by-domain.txt This pattern is
     * auto-generated by frameworks/ex/common/tools/make-iana-tld-pattern.py
     */
    public static final String TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL = "(?:"
            + "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])"
            + "|(?:biz|b[abdefghijmnorstvwyz])"
            + "|(?:cat|com|coop|c[acdfghiklmnoruvxyz])"
            + "|d[ejkmoz]"
            + "|(?:edu|e[cegrstu])"
            + "|f[ijkmor]"
            + "|(?:gov|g[abdefghilmnpqrstuwy])"
            + "|h[kmnrtu]"
            + "|(?:info|int|i[delmnoqrst])"
            + "|(?:jobs|j[emop])"
            + "|k[eghimnprwyz]"
            + "|l[abcikrstuvy]"
            + "|(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])"
            + "|(?:name|net|n[acefgilopruz])"
            + "|(?:org|om)"
            + "|(?:pro|p[aefghklmnrstwy])"
            + "|qa"
            + "|r[eosuw]"
            + "|s[abcdeghijklmnortuvyz]"
            + "|(?:tel|travel|t[cdfghjklmnoprtvwz])"
            + "|u[agksyz]"
            + "|v[aceginu]"
            + "|w[fs]"
            + "|(?:\u03b4\u03bf\u03ba\u03b9\u03bc\u03ae|\u0438\u0441\u043f\u044b\u0442\u0430\u043d\u0438\u0435|\u0440\u0444|\u0441\u0440\u0431|\u05d8\u05e2\u05e1\u05d8|\u0622\u0632\u0645\u0627\u06cc\u0634\u06cc|\u0625\u062e\u062a\u0628\u0627\u0631|\u0627\u0644\u0627\u0631\u062f\u0646|\u0627\u0644\u062c\u0632\u0627\u0626\u0631|\u0627\u0644\u0633\u0639\u0648\u062f\u064a\u0629|\u0627\u0644\u0645\u063a\u0631\u0628|\u0627\u0645\u0627\u0631\u0627\u062a|\u0628\u06be\u0627\u0631\u062a|\u062a\u0648\u0646\u0633|\u0633\u0648\u0631\u064a\u0629|\u0641\u0644\u0633\u0637\u064a\u0646|\u0642\u0637\u0631|\u0645\u0635\u0631|\u092a\u0930\u0940\u0915\u094d\u0937\u093e|\u092d\u093e\u0930\u0924|\u09ad\u09be\u09b0\u09a4|\u0a2d\u0a3e\u0a30\u0a24|\u0aad\u0abe\u0ab0\u0aa4|\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe|\u0b87\u0bb2\u0b99\u0bcd\u0b95\u0bc8|\u0b9a\u0bbf\u0b99\u0bcd\u0b95\u0baa\u0bcd\u0baa\u0bc2\u0bb0\u0bcd|\u0baa\u0bb0\u0bbf\u0b9f\u0bcd\u0b9a\u0bc8|\u0c2d\u0c3e\u0c30\u0c24\u0c4d|\u0dbd\u0d82\u0d9a\u0dcf|\u0e44\u0e17\u0e22|\u30c6\u30b9\u30c8|\u4e2d\u56fd|\u4e2d\u570b|\u53f0\u6e7e|\u53f0\u7063|\u65b0\u52a0\u5761|\u6d4b\u8bd5|\u6e2c\u8a66|\u9999\u6e2f|\ud14c\uc2a4\ud2b8|\ud55c\uad6d|xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-3e0b707e|xn\\-\\-45brj9c|xn\\-\\-80akhbyknj4f|xn\\-\\-90a3ac|xn\\-\\-9t4b11yi5a|xn\\-\\-clchc0ea0b2g2a9gcd|xn\\-\\-deba0ad|xn\\-\\-fiqs8s|xn\\-\\-fiqz9s|xn\\-\\-fpcrj9c3d|xn\\-\\-fzc2c9e2c|xn\\-\\-g6w251d|xn\\-\\-gecrj9c|xn\\-\\-h2brj9c|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|xn\\-\\-j6w193g|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-kprw13d|xn\\-\\-kpry57d|xn\\-\\-lgbbat1ad8j|xn\\-\\-mgbaam7a8h|xn\\-\\-mgbayh7gpa|xn\\-\\-mgbbh1a71e|xn\\-\\-mgbc0a9azcg|xn\\-\\-mgberp4a5d4ar|xn\\-\\-o3cw4h|xn\\-\\-ogbpf8fl|xn\\-\\-p1ai|xn\\-\\-pgbs0dh|xn\\-\\-s9brj9c|xn\\-\\-wgbh1c|xn\\-\\-wgbl6a|xn\\-\\-xkc2al3hye2a|xn\\-\\-xkc2dl3a5ee0h|xn\\-\\-yfro4i67o|xn\\-\\-ygbi2ammx|xn\\-\\-zckzah|xxx)"
            + "|y[et]" + "|z[amw]))";
    /**
     * Good characters for Internationalized Resource Identifiers (IRI). This
     * comprises most common used Unicode characters allowed in IRI as detailed
     * in RFC 3987. Specifically, those two byte Unicode characters are not
     * included.
     */
    public static final String GOOD_IRI_CHAR = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    /**
     * Regular expression pattern to match most part of RFC 3987
     * Internationalized URLs, aka IRIs. Commonly used Unicode characters are
     * added.
     */
    public static Pattern WEB_URL3 = Pattern
            .compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    public static final Pattern WEB_URL2 = Pattern
            .compile("(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?");

    public static final Pattern WEB_URL = Pattern
            .compile("((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "((?:(?:["
                    + GOOD_IRI_CHAR
                    + "]["
                    + GOOD_IRI_CHAR
                    + "\\-]{0,64}\\.)+" // named host
                    + TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
                    + "|(?:(?:25[0-5]|2[0-4]" // or ip address
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
                    + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9])))"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:["
                    + GOOD_IRI_CHAR
                    + "\\;\\/\\?\\:\\@\\&\\=\\#\\~" // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); // and finally, a word boundary or end of
    // input. This is to stop foo.sure from
    // matching as foo.su

    public static final Pattern EMAIL_ADDRESS = Pattern
            .compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");


    /**
     * 电话号码正则表达式
     * 默认为"d{3}-\d{8}|\d{3}-\d{7}|\d{4}-\d{8}|\d{4}-\d{7}|1+[34578]+\d{9}|\+\d{2}1+[34578]+\d{9}|400\d{7}|400-\d{3}-\d{4}|\d{12}|\d{11}|\d{10}|\d{8}|\d{7}"
     * 例如：82563452、01082563234、010-82543213、031182563234、0311-82563234
     * 、+8613691080322、4008881234、400-888-1234
     */
    public static Pattern PHONE_NUMBER = Pattern.compile("\\d{3}-\\d{8}|\\d{3}-\\d{7}|\\d{4}-\\d{8}|\\d{4}-\\d{7}|1+[34578]+\\d{9}|\\+\\d{2}1+[34578]+\\d{9}|400\\d{7}|400-\\d{3}-\\d{4}|\\d{12}|\\d{11}|\\d{10}|\\d{8}|\\d{7}");

    public static Pattern getPhoneNumberPattern() {
        return PHONE_NUMBER;
    }

    public static void setPhoneNumberPattern(Pattern phoneNumberPattern) {
        PHONE_NUMBER = phoneNumberPattern;
    }

    public static Pattern getWebUrl() {
        return WEB_URL3;
    }

    public static void setWebUrl(Pattern webUrlPattern) {
        WEB_URL3 = webUrlPattern;
    }

    //public static final Pattern PHONE_NUMBER = Pattern.compile("^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$");

    public static final Pattern EMOJI = Pattern
            .compile("\\[(([\u4e00-\u9fa5]+)|([a-zA-z]+))\\]");
    public static final Pattern EMOJI_NUMBERS = Pattern
            .compile("\\[[(0-9)]+\\]");


    private String textImagePath;
    private Context context;

    private HtmlTools(Context context) {
        super();
        this.context = context.getApplicationContext();
    }

    public void loadPic(final TextView textView, String source, final String htmlContent,
                        String fileString, final int color) {
        // 启动新线程下载

        final File file = new File(fileString);

        HttpUtils.getInstance().download(source, file, null, new FileCallBack() {

            @Override
            public void onResponse(File result) {
                setRichText(textView, htmlContent, color);
            }

            @Override
            public void onError(Exception e, String msg, int responseCode) {
                LogUtils.i(" 文本图片的下载失败", e);
            }

            @Override
            public void inProgress(int progress) {
                LogUtils.i(" 文本图片的下载进度" + progress);
            }
        });
    }

    /**
     * 设置富文本
     *
     * @param widget
     * @param content
     * @param color   要显示的颜色
     */
    public void setRichText(TextView widget, String content, int color, boolean showBottomLine) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        content = content.trim();
        while (!TextUtils.isEmpty(content) && content.length() > 5 && "<br/>".equals(content.substring(0, 5))) {
            content = content.substring(5, content.length());
        }
        if (!TextUtils.isEmpty(content) && content.length() > 5 && "<br/>".equals(content.substring(content.length() - 5, content.length()))) {
            content = content.substring(0, content.length() - 5);
        }
        widget.setMovementMethod(LinkMovementClickMethod.getInstance());
        widget.setFocusable(false);
        Spanned span = formatRichTextWithPic(widget, content.replace("&", "&amp;").replace("\n", "<br/>"), color);
        // 显示表情
        span = InputHelper.displayEmoji(context.getApplicationContext(), span);
        // 显示链接
        parseLinkText(context, widget, span, color, showBottomLine);
    }

    /**
     * 设置富文本
     *
     * @param widget
     * @param content
     * @param color   要显示的颜色
     */
    public void setRichText(TextView widget, String content, int color) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        content = content.trim();
        if (content.contains("&nbsp;")) {
            content = content.replaceAll("&nbsp;", " ");
        }
        if (content.contains("\n")) {
            content = content.replaceAll("\n", "<br/>");
        }
        if (content.contains("<p>")) {
            content = content.replaceAll("<p>", "").replaceAll("</p>", "<br/>").replaceAll("\n", "<br/>");
        }
        if (content.startsWith("<br/>") && content.length() >= 5) {
            content = content.substring(5);// 去掉开头的<br/>
        }

        while (content.length() > 5 && "<br/>".equals(content.substring(content.length() - 5, content.length()))) {
            content = content.substring(0, content.length() - 5);// 去掉结尾的<br/>
        }

        widget.setMovementMethod(LinkMovementClickMethod.getInstance());
        Spanned span = formatRichTextWithPic(widget, content.replace("&", "&amp;").replace("\n", "<br/>"), color);
        // 显示表情
        span = InputHelper.displayEmoji(context.getApplicationContext(), span);
        // 显示链接
        parseLinkText(context, widget, span, color, false);
    }

    /**
     * 获取处理后的富文本
     *
     * @param content
     */
    public String getRichContent(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        if (content.contains("<p>")) {
            content = content.replaceAll("<p>", "").replaceAll("</p>", "<br/>").replaceAll("\n", "<br/>");
        }
        while (content.length() > 5 && "<br/>".equals(content.substring(content.length() - 5, content.length()))) {
            content = content.substring(0, content.length() - 5);
        }
        if (!TextUtils.isEmpty(content) && content.length() > 0 && "\n".equals(content.substring(content.length() - 1, content.length()))) {
            for (int i = 0; i < content.length(); i++) {
                int aa = content.lastIndexOf("\n");
                if (aa == (content.length() - 1)) {
                    content = content.substring(0, content.length() - 1);
                } else {
                    break;
                }
            }
        }
        return content;
    }

    /**
     * 设置富文本
     * RichTextMessageHolder 专用的，不处理结尾的<br/>
     *
     * @param widget
     * @param content
     * @param color   要显示的颜色
     */
    public void setRichTextViewText(TextView widget, String content, int color) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (content.contains("\n")) {
            content = content.replaceAll("\n", "<br/>");
        }
        widget.setMovementMethod(LinkMovementClickMethod.getInstance());
        Spanned span = formatRichTextWithPic(widget,content.replace("&", "&amp;").replace("\n", "<br/>"), color);
        // 显示表情
        span = InputHelper.displayEmoji(context.getApplicationContext(), span);
        // 显示链接
        parseLinkText(context, widget, span, color, false);
    }

    /**
     * 获取带图片的富文本如果本地没有就开启下载
     *
     * @param textView
     * @param htmlContent
     * @param color
     * @return
     */
    public Spanned formatRichTextWithPic(final TextView textView, final String htmlContent, final int color) {
        return Html.fromHtml(("<span>"+htmlContent+"</span>").replace("span", "sobotspan"), new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                if (!TextUtils.isEmpty(source)) {
                    textImagePath = CommonUtils.getSDCardRootPath(context);
                    Drawable drawable = null;
                    String fileString = textImagePath
                            + String.valueOf(source.hashCode());
                    if (new File(fileString).exists()) {
                        LogUtils.i(" 网络下载 文本中的图片信息  " + fileString + "  eixts");
                        // 获取本地文件返回Drawable
                        drawable = Drawable.createFromPath(fileString);
                        if (drawable != null) {
                            // 设置图片边界
                            LogUtils.i(" 图文并茂中 图片的 大小 width： "
                                    + drawable.getIntrinsicWidth() + "--height:"
                                    + drawable.getIntrinsicWidth());
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 4,
                                    drawable.getIntrinsicHeight() * 4);
                        }
                        return drawable;
                    } else {
                        LogUtils.i(fileString + " Do not eixts");
                        if (source.startsWith("https://") || source.startsWith("http://")) {
                            loadPic(textView, source, htmlContent, fileString, color);
                            return drawable;
                        } else
                            return null;
                    }
                }
                return null;
            }

        }, new SobotCustomTagHandler(context, textView.getTextColors()));
    }

    /**
     * 显示超链接
     *
     * @param context
     * @param widget
     * @param spanhtml
     * @param color    要显示的颜色
     */
    public static void parseLinkText(Context context, TextView widget, Spanned spanhtml, int color, boolean showLine) {
        CharSequence text = spanhtml;
        if (text instanceof Spannable) {
            Spannable sp = (Spannable) spanhtml;

            // 检查出所有EMAIL地址
            Matcher m = EMAIL_ADDRESS.matcher(sp);
            while (m.find()) {
                int start = m.start();
                int e = m.end();
                if (sp.getSpans(start, e, URLSpan.class).length == 0) {
                    sp.setSpan(new EmailSpan(context.getApplicationContext(), m.group(), color), start, e,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // 检查出所有链接
            m = getWebUrl().matcher(sp);
            while (m.find()) {
                int start = m.start();
                int e = m.end();
                if (sp.getSpans(start, e, URLSpan.class).length == 0) {
                    sp.setSpan(new MyURLSpan(context.getApplicationContext(), m.group(), color, true), start, e,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // 检查出所有电话
            m = getPhoneNumberPattern().matcher(sp);
            while (m.find()) {
                int start = m.start();
                int e = m.end();
                if (sp.getSpans(start, e, URLSpan.class).length == 0) {
                    sp.setSpan(new PhoneSpan(context.getApplicationContext(), m.group(), color), start, e,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            int end = text.length();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            URLSpan[] htmlurls = spanhtml != null ? spanhtml.getSpans(0, end,
                    URLSpan.class) : new URLSpan[]{};

            if (urls.length == 0 && htmlurls.length == 0) {
                widget.setText(sp);
                return;
            }

            SpannableStringBuilder style = new SpannableStringBuilder(text);
            for (URLSpan url : htmlurls) {
                style.removeSpan(url);// 只需要移除之前的URL样式，再重新设置
                MyURLSpan myURLSpan = new MyURLSpan(context.getApplicationContext(), url.getURL(), color, showLine);
                style.setSpan(myURLSpan, spanhtml.getSpanStart(url),
                        spanhtml.getSpanEnd(url),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            widget.setText(style);
        }
    }

    public static boolean isHasPatterns(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (getWebUrl().matcher(url.toString()).matches()) {
            return true;
        } else {
            return false;
        }
    }

    public String getHTMLStr(String htmlStr) {
        if (TextUtils.isEmpty(htmlStr)) {
            return "";
        }

        //先将换行符保留，然后过滤标签
        Pattern p_enter = Pattern.compile("<br/>", Pattern.CASE_INSENSITIVE);
        Matcher m_enter = p_enter.matcher(htmlStr);
        htmlStr = m_enter.replaceAll("\n");

        //过滤html标签
        Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        return m_html.replaceAll("");
    }
}