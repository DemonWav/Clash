package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.ClashException;
import org.junit.Test;

public class BadArgsTest {

    @Test(expected = ClashException.Dashes.class)
    public void dashTest() {
        class Bean {
            @Argument(shortNames = "-a")
            private String s;
        }
        Clash.init(Bean.class, null);
    }

    @Test(expected = ClashException.Whitespace.class)
    public void whitespaceTest() {
        class Bean {
            @Argument(shortNames = "hello world")
            private String s;
        }
        Clash.init(Bean.class, null);
    }
}
