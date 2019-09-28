package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class EnumArrayTest extends BaseEnumTest {

    @Test
    public void enumArrayTest() {
        final String[] args = {
            "-a", one + ", " + two + ", " + three
        };
        Assert.assertEquals(new Bean(new BeanEnum[] {BeanEnum.ONE, BeanEnum.two, BeanEnum.THREE_WITH_SPACE}), Clash.init(Bean.class, args));
    }

    @Value
    private static final class Bean {
        @Argument(shortNames = "a")
        BeanEnum[] array;
    }
}
