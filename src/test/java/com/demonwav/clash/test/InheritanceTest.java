package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Init;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class InheritanceTest {

    private static final String wat = "wat";
    private static final String one = "one";
    private static final String two = "two";

    @Test
    public void inheritanceTest() {
        final String[] args = {
            "-o", one,
            "-t", two
        };
        final BeanTwo init = Clash.init(BeanTwo.class, args);
        System.out.println(init);
        Assert.assertEquals(new BeanTwo(one, wat + one, two, wat + two), init);
    }

    private static class BeanTwo extends BeanOne {
        @Argument(shortName = "t")
        private final String two;
        private String watTwo;

        private BeanTwo(String one, String watOne, String two, String watTwo) {
            super(one, watOne);
            this.two = two;
            this.watTwo = watTwo;
        }

        @Init
        private void init() {
            watTwo = wat + two;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final BeanTwo beanTwo = (BeanTwo) o;
            return Objects.equals(two, beanTwo.two) &&
                Objects.equals(watTwo, beanTwo.watTwo) &&
                Objects.equals(one, beanTwo.one) &&
                Objects.equals(watOne, beanTwo.watOne);
        }

        @Override
        public int hashCode() {
            return Objects.hash(two, watTwo);
        }
    }

    private static class BeanOne {
        @Argument(shortName = "o")
        final String one;
        String watOne;

        private BeanOne(String one, String watOne) {
            this.one = one;
            this.watOne = watOne;
        }

        @Init
        private void init() {
            watOne = wat + one;
        }
    }
}
