package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.Objects;
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
        final String[] args = new String[] {
            "-b", String.valueOf(b),
            "--short=" + s,
            "-i", String.valueOf(i),
            "-st", st,
            "--boolean=" + boo
        };

        Assert.assertEquals(new Bean(b, s, i, l, st, boo), Clash.init(Bean.class, args));
    }

    private static class Bean {
        @Argument(shortName = "b", longNames = "byte")
        private final byte b;
        @Argument(shortName = "s", longNames = "short")
        private final short s;
        @Argument(shortName = "i", longNames = "integer")
        private final int i;
        @Argument(shortName = "l", longNames = "long", defaultValue = "" + BasicTest.l)
        private final long l;
        @Argument(shortName = "st", longNames = "string")
        private final String st;
        @Argument(shortName = "boo", longNames = "boolean")
        private final Boolean boo;
        @Argument(shortName = "wat", required = false)
        private final Object wat;

        private Bean(byte b, short s, int i, long l, String st, Boolean boo) {
            this.b = b;
            this.s = s;
            this.i = i;
            this.l = l;
            this.st = st;
            this.boo = boo;
            this.wat = null;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Bean{");
            sb.append("b=").append(b);
            sb.append(", s=").append(s);
            sb.append(", i=").append(i);
            sb.append(", l=").append(l);
            sb.append(", st='").append(st).append('\'');
            sb.append(", boo=").append(boo);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Bean bean = (Bean) o;
            return b == bean.b &&
                s == bean.s &&
                i == bean.i &&
                l == bean.l &&
                Objects.equals(st, bean.st) &&
                Objects.equals(boo, bean.boo) &&
                Objects.equals(wat, bean.wat);
        }

        @Override
        public int hashCode() {
            return Objects.hash(b, s, i, l, st, boo, wat);
        }
    }
}
