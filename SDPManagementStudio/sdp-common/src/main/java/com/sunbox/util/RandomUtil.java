package com.sunbox.util;

import java.util.Random;

/**
 * Created by Dan on 2015/9/26.
 */
public class RandomUtil {

    //字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
    public static final String RANDOM_CODES = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";

    public static String generateRandom(int num, boolean onlyNum){
        StringBuilder verifyCode = new StringBuilder(num);
        Random rand = new Random(System.currentTimeMillis());
        int rlength = RANDOM_CODES.length();
        if(onlyNum){
            for(int i=0;i<num;i++){
                verifyCode.append((char)(int)(Math.random()*10+48));
            }
        }else {
            for(int i=0;i<num;i++){
                verifyCode.append(RANDOM_CODES.charAt(rand.nextInt(rlength-1)));
            }
        }
       //System.out.println(verifyCode.toString());
        return verifyCode.toString();
       /* Random random = new Random();
        if(onlyNum){
            for(int i=0;i<num;i++){
                rd+=""+ random.nextInt(9);
            }
        }else {
            for(int i=0;i<num;i++){
                int flag = random.nextInt(2);
                if(flag==0){
                    rd+=""+(char)(int)(Math.random()*10+48);
                }else{
                    rd+=""+(char)(int)(Math.random()*26+65);
                }
            }
        }
        System.out.println(rd);
        return rd;*/
    }

    /**
     * 生成指定长度的随机数
     * @param size
     * @return
     */
    public static String getRandomBySize(int size){
        StringBuilder str=new StringBuilder();//定义变长字符串
        Random random=new Random();
        //随机生成数字，并添加到字符串
        for(int i=0;i<size;i++){
            str.append(random.nextInt(10));
        }
        //将字符串转换为数字并输出
        return str.toString();
    }

    public static void main(String[] args){
        generateRandom(6,true);
        generateRandom(6,false);
       /* System.out.println((int)'a');
        System.out.println((int)'z');
        System.out.println((int)'A');
        System.out.println((int)'Z');

        System.out.println((int)'0');
        System.out.println((int)'9');

        System.out.println((char)(int)(Math.random()*26+97));
        System.out.println((char)(int)(Math.random()*10+48));
        System.out.println((char)(int)(Math.random()*26+65));

        System.out.println((char) (int) (Math.random() * 10 + 65));*/

      /*  Random random = new Random();
       for(int i=0;i<3;i++){
           System.out.println( random.nextInt(3));

       }
        int r = random.nextInt(3);
        int rr=48;
        if(r==0){
            rr=48;
        }else if(r==1){
            rr=65;
        }else if(r==2){
            rr=97;
        }
        System.out.println((char)(int)(Math.random()*26+rr));*/

        /*int rr = (int)(Math.random(3)*10);
        int addnum = 48;
        if(rr>=0 && rr<3){

        }
*/
    }
}
