package com.wiqer.rpc.serialize.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.SecureRandom;

public final class EFID {
    volatile int mostSigBits;
    volatile int  leastSigBits;
    volatile static int efIdCount=159651;//随便打的
    static String  process=digits(getProcessID()&0Xffff,2);
    volatile static byte[] randomBytes;
    static {
        EFID.numberGenerator=  new SecureRandom();
        EFID.randomBytes=new byte[8];
        EFID.numberGenerator.nextBytes(randomBytes);
    }
    static SecureRandom numberGenerator;

    private EFID(byte[] data) {
        int msb = this.mostSigBits;
        int lsb = this.leastSigBits;
        int count = this.efIdCount++;
        assert data.length == 8 : "data must be 16 bytes in length";
        for (int i=0; i<4; i++)
            msb = msb+(msb << 1) | (data[i] & 0xff);
        for (int i=0; i<4; i++)
            lsb = msb+(lsb << 1) | (data[i] & 0xff);
        this.mostSigBits = msb+ count>>4;
        this.leastSigBits = lsb+ count<<4;
    }
    @Override
    public String toString() {
        int count =EFID.efIdCount;
        return (
                //digits(mostSigBits >> 16, 8) + "-" +
                process +
                digits(Thread.currentThread().getId()&0xff, 2)+"-"
                        +
                //digits(mostSigBits >> 4, 2) + //"-" +
                digits(mostSigBits, 4) + "-" +
                //digits(leastSigBits >> 4, 3).substring(0,2)+ //"-" +
                digits(leastSigBits, 4)
                        +
               digits(count, 4)
        );
    }

    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
    public static EFID randomID() {
        SecureRandom ng = numberGenerator;

        byte[] randomBytes = EFID.randomBytes;
        //此处会加快速度，快3倍
        ng.nextBytes(randomBytes);
        randomBytes[3]  &= 0x0f;  /* clear version        */
        randomBytes[3]  |= 0x40;  /* set to version 4     */
        randomBytes[7]  &= 0x3f;  /* clear variant        */
        randomBytes[7]  |= 0x80;  /* set to IETF variant  */
        return new EFID(randomBytes);
    }
    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                .intValue();
    }

}
