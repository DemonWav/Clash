package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class EnumArrayTest {

    private static final String one = "ONE";
    private static final String two = "TwO";
    private static final String three = "Three With Space";

    @Test
    public void enumArrayTest() {
        final String[] args = {
            "-a", one + ", " + two + ", " + three
        };
        final Bean init = Clash.init(Bean.class, args);
        System.out.println(init);
        Assert.assertEquals(new Bean(new Bean.BeanEnum[] {Bean.BeanEnum.ONE, Bean.BeanEnum.two, Bean.BeanEnum.THREE_WITH_SPACE}),
                            init
        );
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

        private enum BeanEnum {
            ONE, two, THREE_WITH_SPACE
        }
    }
}
