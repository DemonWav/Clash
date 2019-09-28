package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Creator;
import java.util.UUID;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class CreatorTest {

    private static final String string = UUID.randomUUID().toString();

    @Test
    public void creatorTest() {
        final String[] args = {};
        Assert.assertEquals(new Bean(string), Clash.init(Bean.class, args));
    }

    @Value
    private static class Bean {
        @Argument(shortNames = "r", defaultCreator = Create.class)
        String res;

        private static class Create implements Creator<String> {
            @Override
            public String createDefault(final String fieldName) {
                return string;
            }
        }
    }
}
