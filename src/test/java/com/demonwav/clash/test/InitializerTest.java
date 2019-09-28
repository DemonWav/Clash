package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Initializer;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class InitializerTest {

    private static final String result = "asdf";

    @Test
    public void initTest() {
        final String[] args = {
            "--result=" + result
        };

        Assert.assertEquals(new Bean(result + result), Clash.init(Bean.class, args));
    }

    @Value
    private static final class Bean {
        @Argument(shortNames = "r", longNames = "result", initializer = Init.class)
        String res;

        private static class Init implements Initializer<String> {
            @Override
            public String initialize(String fieldName, String value) {
                return value + value;
            }
        }
    }
}
