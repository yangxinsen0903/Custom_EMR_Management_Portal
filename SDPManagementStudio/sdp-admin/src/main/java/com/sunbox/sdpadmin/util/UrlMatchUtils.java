package com.sunbox.sdpadmin.util;

public class UrlMatchUtils {

    public static boolean wildcardStarMatch(String pattern, String str) {
        int strLength = str.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0, patternLength = pattern.length(); patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {// 通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardStarMatch(pattern.substring(patternIndex + 1), str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }

    public static void main(String[] args) {
//        /app/**   匹配(Matches) /app/foo,/app/foo/bar/dir/,
        System.out.println(wildcardStarMatch("/app/**","/app/foo/"));
        System.out.println(wildcardStarMatch("/app/**","/app/foo/bar/dir/file.pdf"));

    }
}
