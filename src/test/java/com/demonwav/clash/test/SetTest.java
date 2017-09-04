package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class SetTest {

    private static final String one = "one";
    private static final String two = "two";

    private static final Set<String> set = new HashSet<String>() {{
        add(one);
        add(two);
    }};

    @Test
    public void setTest() {
        final String[] args = {
            "-s", "[" + one + "," + two + "]"
        };

        Assert.assertEquals(new Bean(set), Clash.init(Bean.class, args));
    }

    private static final class Bean {
        @Argument(shortNames = "s")
        private final Set<String> set;

        private Bean(Set<String> set) {
            this.set = set;
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
            return Objects.equals(set, bean.set);
        }

        @Override
        public int hashCode() {
            return Objects.hash(set);
        }
    }
}
