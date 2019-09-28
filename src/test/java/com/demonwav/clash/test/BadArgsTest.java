package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.ClashException;
import lombok.Data;
import lombok.Value;
import org.junit.Test;

public class BadArgsTest {

    @Test(expected = ClashException.Dashes.class)
    public void dashTest() {
        @Value
        class Bean {
            @Argument(shortNames = "-a")
            String s;
        }
        Clash.init(Bean.class, null);
    }

    @Test(expected = ClashException.Whitespace.class)
    public void whitespaceTest() {
        @Value
        class Bean {
            @Argument(shortNames = "hello world")
            String s;
        }
        Clash.init(Bean.class, null);
    }
}
