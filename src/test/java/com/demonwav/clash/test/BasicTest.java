package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class BasicTest {

    private static final byte b = (byte) 145;
    private static final short s = (short) -234;
    private static final int i = 1234;
    private static final long l = 4363564232L;
    private static final String st = "Hi! I'm a String.";
    private static final Boolean boo = false;

    @Test
    public void basicTest() {
        final String[] args = {
            "-b", String.valueOf(b),
            "--short=" + s,
            "-i", String.valueOf(i),
            "-st", st,
            "--boolean=" + boo
        };

        Assert.assertEquals(new Bean(b, s, i, l, st, boo, null), Clash.init(Bean.class, args));
    }

    @Value
    private static class Bean {
        @Argument(shortNames = "b", longNames = "byte")
        byte b;
        @Argument(shortNames = "s", longNames = "short")
        short s;
        @Argument(shortNames = "i", longNames = "integer")
        int i;
        @Argument(shortNames = "l", longNames = "long", defaultValue = "" + BasicTest.l)
        long l;
        @Argument(shortNames = "st", longNames = "string")
        String st;
        @Argument(shortNames = "boo", longNames = "boolean")
        Boolean boo;
        @Argument(shortNames = "wat", required = false)
        Object wat;
    }
}
