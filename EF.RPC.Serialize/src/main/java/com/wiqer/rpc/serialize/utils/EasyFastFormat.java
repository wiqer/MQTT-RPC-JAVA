package com.wiqer.rpc.serialize.utils;

public class EasyFastFormat {
    static final  char[] EFNETBytes =new char[]{'0','1','2','3','4','5','6','7','8','9',
            'a','b','c','d','e','f','g','h','i','j','k','l',
            'm','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V',
            'W','X','Y','Z','-','_'};
    public static int uint32(long z) {
       // if(z<0) z=-z;
        return (int)(z&0x7fffffff);//(int)(z);
    }
    //保留n位
    public static int uintNBit(long z,int n) {
        // if(z<0) z=-z;
        return (int)(z&(1<<n-1));//(int)(z);
    }
    public static String digits(int val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[val&63];
            val >>>= 6;
        }while (digits>0);//; while (val != 0&&digits>0);
        return new String(buf);

    }
    public static String digits(Long val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[(int)(val&63)];
            val >>>= 6;
        }while (digits>0);//; while (val != 0&&digits>0);
        return new String(buf);

    }
    public static String digits64(Long val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[(int)(val&63)];
            val >>>= 6;
        }while (digits>0);//; while (val != 0&&digits>0);
        return new String(buf);

    }
    public static String digits64ZeroBreak(Long val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[(int)(val&63)];
            val >>>= 6;
        }while (digits>0);//; while (val != 0&&digits>0);
        String subId="";
        for (int i=buf.length-1;buf[i]=='0'&&i>=0;i--){
            buf[i]='\0';
        }
        return new String(buf);

    }
    public static String digits32(int val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[val&31];
            val >>>= 5;
        }while (digits>0);//; while (val != 0&&digits>0);
        return new String(buf);

    }
    public static String digits32(Long val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[(int)(val&31)];
            val >>>= 5;
        }while (digits>0);//; while (val != 0&&digits>0);
        return new String(buf);

    }
    //存储数据库时使用这个，mysql默认不区分大小写
    public static String digits32ZeroBreak(Long val, int digits) {
        char[] buf = new char[digits];
        do {
            buf[--digits] = EFNETBytes[(int)(val&31)];
            val >>>= 5;
        }while (digits>0);//; while (val != 0&&digits>0);
        String subId="";
        for (int i=buf.length-1;buf[i]=='0'&&i>=0;i--){
            buf[i]='\0';
        }
        return new String(buf);

    }
}
