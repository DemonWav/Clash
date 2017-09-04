package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Creator;
import java.util.Objects;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class CreatorTest {

    private static final String string = UUID.randomUUID().toString();

    @Test
    public void creatorTest() {
        final String[] args = {};
        Assert.assertEquals(new Bean(string), Clash.init(Bean.class, args));
    }

    private static final class Bean {
        @Argument(shortNames = "r", defaultCreator = Create.class)
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

        private static class Create implements Creator<String> {
            @Override
            public String createDefault(String fieldName) {
                return string;
            }
        }
    }
}
