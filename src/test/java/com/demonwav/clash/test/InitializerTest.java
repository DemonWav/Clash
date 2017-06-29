package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Initializer;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class InitializerTest {

    private static final String result = "asdf";

    @Test
    public void initTest() {
        final String[] args = new String[] {
            "--result=" + result
        };

        Assert.assertEquals(new Bean(result + result), Clash.init(Bean.class, args));
    }

    private static class Bean {
        @Argument(shortName = "r", longNames = "result", initializer = Init.class)
        private final String res;

        private Bean(String res) {
            this.res = res;
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
            return Objects.equals(res, bean.res);
        }

        @Override
        public int hashCode() {
            return Objects.hash(res);
        }

        private static class Init implements Initializer<String> {
            @Override
            public String initialize(String fieldName, String value) {
                return value + value;
            }
        }
    }
}
