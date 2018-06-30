package com.tony.stepcounter.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.view.View;

/**
 * 系统对字符串操作提供的帮助类
 *
 * @author lantian
 *
 */
public final class StringUtils {
    /**
     * 构造函数
     * <p>
     * 不允许实例化
     */
    private StringUtils() {
    }

    /**
     * 判读一个字符串是否为null
     *
     * @param value
     *            要判断的字符串
     * @param isTrim
     *            是否对要判断的字符串进行trim
     * @return 字符串是否为null
     */
    public static boolean isNull(String value, boolean isTrim) {
        boolean result = false;
        if (null == value) {
            result = true;
        } else if ("".equals(value)) {
            result = true;
        } else if (isTrim) {
            if ("".equals(value.trim())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 判读一个字符串是否为null
     * <p>
     * 在判断之前首先对要判断的字符串进行trim
     *
     * @param value
     *            要判断的字符串
     * @return 字符串是否为null
     */
    public static boolean isNull(String value) {
        return isNull(value, true);
    }

    /**
     * 对一个字符串进行trim操作
     *
     * @param value
     *            需要进行trim操作的字符串
     * @param isTransferDefault
     *            如果要进行trim操作的字符串为null是否用一个默认值替代
     * @param nullDefault
     *            如果要进行trim操作的字符串为null，默认的替代值
     * @return trim后的结果
     */
    public static String trim(String value, boolean isTransferDefault,
                              String nullDefault) {
        if (isNull(value)) {
            if (isTransferDefault) {
                return nullDefault;
            }
            return value;
        }
        return value.trim();

    }

    /**
     * 对一个字符串进行trim操作，但是如果要进行trim操作的字符串为null的情况下，不用默认值替代
     *
     * @param value
     *            需要进行trim操作的字符串
     * @return trim后的结果
     */
    public static String trimNoDefaultValue(String value) {
        return trim(value, false, null);
    }

    /**
     * 对一个字符串进行trim操作，但是如果要进行trim操作的字符串为null的情况下，用默认值替代
     *
     * @param value
     *            需要进行trim操作的字符串
     * @param nullDefault
     *            如果要进行trim操作的字符串为null，默认的替代值
     * @return trim后的结果
     */
    public static String trim(String value, String nullDefault) {
        return trim(value, true, nullDefault);
    }

    /**
     * 对一个字符串进行trim操作，但是如果要进行trim操作的字符串为null的情况下，用长度为0的空格来替代
     *
     * @param value
     *            需要进行trim操作的字符串
     * @return trim后的结果
     */
    public static String trim(String value) {
        return trim(value, true, "");
    }

    /**
     * 将一个映射(Map)对象转化为字符串输出
     *
     * @param map
     *            要转化为字符串输出的映射(Map)对象
     * @return 转化后的字符串
     */
    @SuppressWarnings("unchecked")
    public static String mapToString(Map map) {
        if (null == map) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        int i = 0;

        String separator = "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";

        for (Object key : map.keySet()) {
            if (i == 0 && !map.isEmpty()) {
                sb.append(separator);
            }
            sb.append(key.toString()).append("=\n\t\t ").append(
                    map.get(key).toString()).append(separator);
        }

        return sb.toString();
    }

    /**
     * 将一个列表(List)对象转化为字符串输出
     *
     * @param list
     *            要转化为字符串输出的列表(List)对象
     * @return 转化后的字符串
     */
    @SuppressWarnings("unchecked")
    public static String listToString(List list) {
        if (null == list) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty(&quot;&quot;)        = true
     * StringUtils.isEmpty(&quot; &quot;)       = false
     * StringUtils.isEmpty(&quot;bob&quot;)     = false
     * StringUtils.isEmpty(&quot;  bob  &quot;) = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the
     * String. That functionality is available in isBlank().
     * </p>
     *
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        } else {
            return str.trim().length() <= 0;
        }
    }

    /**
     *
     * @param  view 试图
     * @return isShown
     */
    public static boolean isShown(View view) {
        if (view == null) {
            return false;
        }

        return view.getVisibility() == View.VISIBLE;
    }

    /**
     * 连接字符串
     *
     * @param delimiter
     *            TODO
     * @param tokens
     *            TODO
     *
     * @return TODO
     */
    public static String join(String delimiter, Collection<?> tokens) {
        if (tokens == null || delimiter == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;

        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(token);
        }

        return sb.toString();
    }

    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty(&quot;&quot;)        = false
     * StringUtils.isNotEmpty(&quot; &quot;)       = true
     * StringUtils.isNotEmpty(&quot;bob&quot;)     = true
     * StringUtils.isNotEmpty(&quot;  bob  &quot;) = true
     * </pre>
     *
     * @param str
     *            the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 去头尾空格,如果为null或""返回defaultString
     *
     * @param paramTarget TODO
     * @param defaultString TODO
     *
     * @return TODO
     */
    public static String trimToDefault(String paramTarget, String defaultString) {
        String target = paramTarget;

        if ((target == null) || ((target = target.trim()).length() == 0)) {
            return defaultString;
        } else {
            return target;
        }
    }

    /**
     * TODO
     */
    private static final int NUM_4 = 4;
    /**
     * TODO
     */
    private static final int NUM_3 = 3;
    /**
     * TODO
     */
    private static final int NUM_7 = 7;

    /**
     * ...字符串
     *
     * @param str
     *            for 传入的需要...的字符串
     * @param offset
     *            for 偏移量
     * @param maxWidth
     *            for 最大的宽度
     * @return ...后的字符串
     */
    public static String abbreviate(String str, int offset, int maxWidth) {

        int i = offset;
        final String omit = "...";
        if (str == null) {
            return null;
        }
        if (maxWidth < NUM_4) {
            throw new IllegalArgumentException();
        }

        if (str.length() <= (maxWidth - 2)) {
            return str;
        }

        if (i > str.length()) {
            i = str.length();
        }
        if ((str.length() - i) < (maxWidth - NUM_3)) {
            i = str.length() - (maxWidth - NUM_3);
        }
        if (i <= NUM_4) {
            return str.substring(0, maxWidth - NUM_3) + omit;
        }
        if (maxWidth < NUM_7) {
            throw new IllegalArgumentException();
        }
        if ((i + (maxWidth - NUM_3)) < str.length()) {
            return omit + abbreviate(str.substring(i), maxWidth - NUM_3);
        }
        return omit + str.substring(str.length() - (maxWidth - NUM_3));
    }

    /**
     * @param str
     *            for 生成...
     * @param maxWidth
     *            for 最大的宽度
     * @return ...后的字符串
     */
    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    /**
     * 把字符串转换成boolean,
     * @param s 要转换的字符串
     * @return 只有字符串为1或true(大小写无关)时返回true
     */
    public static boolean parseBoolean(String s) {
        if (isEmpty(s)) {
            return false;
        }

        try {
            return "true".equalsIgnoreCase(s) || "1".equals(s);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 把字符串转换成int
     * @param s 要转换的字符串
     * @param defaultValue 转换失败后的默认值
     * @return 字符串的int值
     */
    public static int parseInt(String s, int defaultValue) {
        if (isEmpty(s)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}