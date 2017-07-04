package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class EnumArrayTest extends BaseEnumTest {

    @Test
    public void enumArrayTest() {
        final String[] args = {
            "-a", one + ", " + two + ", " + three
        };
        Assert.assertEquals(new Bean(new BeanEnum[] {BeanEnum.ONE, BeanEnum.two, BeanEnum.THREE_WITH_SPACE}), Clash.init(Bean.class, args));
    }

    private static class Bean {
        @Argument(shortNames = "a")
        private final BeanEnum[] array;

        private Bean(BeanEnum[] array) {
            this.array = array;
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
            return Arrays.equals(array, bean.array);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }
    }
}
