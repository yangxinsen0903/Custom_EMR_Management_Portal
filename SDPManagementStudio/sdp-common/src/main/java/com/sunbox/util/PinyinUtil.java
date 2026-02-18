package com.sunbox.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
    public static String getPingYin(String src) {

        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();

        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                // 判断是否为汉字字符
                if (java.lang.Character.toString(t1[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    t4 += t2[0];
                } else
                    t4 += java.lang.Character.toString(t1[i]);
            }
            // System.out.println(t4);
            return t4;
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return t4;
    }
    public static void main(String[] args) {
        // System.out.println(getPingYin("系统编码,组织oucode,组织orgcode,流水号,收款机号,销售日期,报表日期,收款员ID,收款员编码,欠款金额,应收金额,实收金额,支付金额,找零金额,交易类型,购物类型,会员卡号,顾客类型,备注,标志,班次,流水标识,交款单号,创建时间,修改时间,审核时间,交易时间,海信记账日期,海信组织编码,班结时间,购物编号,云平台Userid"));
        String data=  getPingYin("系统编码,组织oucode,组织orgcode,流水号,收款机号,销售日期,报表日期,收款员ID,收款员编码,欠款金额,应收金额,实收金额,支付金额,找零金额,交易类型,购物类型,会员卡号,顾客类型,备注,标志,班次,流水标识,交款单号,创建时间,修改时间,审核时间,交易时间,海信记账日期,海信组织编码,班结时间,购物编号,云平台Userid");
        String []datas=data.split(",");
        for(int i=0;i<datas.length;i++){
            System.out.println(datas[i].toString());
        }
    }
}
