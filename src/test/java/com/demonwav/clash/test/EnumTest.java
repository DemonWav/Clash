package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class EnumTest extends BaseEnumTest {

    @Test
    public void enumTest() {
        final String[] args = {
            "-1", one,
            "-2", two,
            "-3", three
        };
        Assert.assertEquals(new Bean(BeanEnum.ONE, BeanEnum.two, BeanEnum.THREE_WITH_SPACE), Clash.init(Bean.class, args));
    }

    @Value
    private static class Bean {
        @Argument(shortNames = "1")
        BeanEnum one;
        @Argument(shortNames = "2")
        BeanEnum two;
        @Argument(shortNames = "3")
        BeanEnum three;
    }
}
