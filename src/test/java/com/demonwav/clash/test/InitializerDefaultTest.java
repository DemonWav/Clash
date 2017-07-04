package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Initializer;
import java.util.Objects;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class InitializerDefaultTest {

    private static final String result = UUID.randomUUID().toString();
    private static final String asdf = "asdf";

    @Test
    public void initializerDefaultTest() {
        final String[] args = {};
        Assert.assertEquals(new Bean(asdf + result), Clash.init(Bean.class, args));
    }

    private static class Bean {
        @Argument(shortNames = "r", initializer = Init.class, defaultValue = asdf)
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
                return value + result;
            }
        }
    }
}
