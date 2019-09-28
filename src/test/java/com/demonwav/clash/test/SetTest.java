package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.Value;
import org.junit.Assert;
import org.junit.Test;

public class SetTest {

    private static final String one = "one";
    private static final String two = "two";

    private static final Set<String> set = new HashSet<String>() {{
        add(one);
        add(two);
    }};

    @Test
    public void setTest() {
        final String[] args = {
            "-s", "[" + one + "," + two + "]"
        };

        Assert.assertEquals(new Bean(set), Clash.init(Bean.class, args));
    }

    @Value
    private static final class Bean {
        @Argument(shortNames = "s")
        Set<String> set;
    }
}
