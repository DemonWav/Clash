package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class EnumTest extends BaseEnumTest {

    @Test
    public void enumTest() {
        final String[] args = {
            "-1", one,
            "-2", two,
            "-3", three
        };
        Assert.assertEquals(new Bean(BeanEnum.ONE, BeanEnum.two, BeanEnum.THREE_WITH_SPACE), Clash.init(Bean.class, args));
    }

    private static final class Bean {
        @Argument(shortNames = "1")
        private final BeanEnum one;
        @Argument(shortNames = "2")
        private final BeanEnum two;
        @Argument(shortNames = "3")
        private final BeanEnum three;

        private Bean(BeanEnum one, BeanEnum two, BeanEnum three) {
            this.one = one;
            this.two = two;
            this.three = three;
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
            return one == bean.one &&
                two == bean.two &&
                three == bean.three;
        }

        @Override
        public int hashCode() {
            return Objects.hash(one, two, three);
        }
    }
}
