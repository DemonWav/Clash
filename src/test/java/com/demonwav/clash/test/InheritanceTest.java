package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Init;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
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
        Assert.assertEquals(new BeanTwo(one, wat + one, two, wat + two), Clash.init(BeanTwo.class, args));
    }

    @Data
    private static class BeanOne {
        @Argument(shortNames = "o")
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

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static final class BeanTwo extends BeanOne {
        @Argument(shortNames = "t")
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
    }
}
