package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class ListTest {

    private static final String one = "one";
    private static final String two = "two";

    private static final List<String> list = new ArrayList<String>() {{
        add(one);
        add(two);
    }};

    @Test
    public void listTest() {
        final String[] args = {
            "-l", "[" + one + "," + two + "]"
        };

        Assert.assertEquals(new Bean(list), Clash.init(Bean.class, args));
    }

    private static final class Bean {
        @Argument(shortNames = "l")
        private final List<String> list;

        private Bean(List<String> list) {
            this.list = list;
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
            return Objects.equals(list, bean.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(list);
        }
    }
}
