package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Initializer;
import java.util.UUID;
import lombok.Data;
import lombok.Value;
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

    @Value
    private static class Bean {
        @Argument(shortNames = "r", initializer = Init.class, defaultValue = asdf)
        String res;

        private static class Init implements Initializer<String> {
            @Override
            public String initialize(final String fieldName, final String value) {
                return value + result;
            }
        }
    }
}
