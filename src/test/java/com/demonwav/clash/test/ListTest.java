package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

public class ListTest {

    private static final String one = "one";
    private static final String two = "two";

    private static final List<String> list = new ArrayList<String>() {{
        add(one);
        add(two);
    }};

    @Test
    public void listTest() {
        final String[] args = {
            "-l", "[" + one + "," + two + "]"
        };

        Assert.assertEquals(new Bean(list), Clash.init(Bean.class, args));
    }

    @Data
    private static final class Bean {
        @Argument(shortNames = "l")
        private final List<String> list;
    }
}
