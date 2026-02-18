package com.sunbox.util;

import java.util.Random;

/**
 * Created by qiwei on 2016/2/24.
 */
public class ValidCode {
        private Random random;
        private String table;
        public ValidCode()
        {
            random = new Random();
            table = "1357294860";
        }
        public String genCode(long id)
        {
            String ret = null,num = String.format("%05d",id);
            int key = random.nextInt(10),seed = random.nextInt(100);
            Caesar caesar = new Caesar(table,seed);num = caesar.encode(key,num);
            ret = num + String.format("%01d",key) + String.format("%02d",seed);
            return ret;
        }
    public String genCode()
    {
        return genCode(System.currentTimeMillis());
    }
    public static void main(final String[] args) {
        ValidCode vc=new ValidCode();
        System.out.println(vc.genCode(System.currentTimeMillis()));
    }
}
