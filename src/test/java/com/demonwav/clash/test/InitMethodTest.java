package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Init;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.junit.Assert;
import org.junit.Test;

public class InitMethodTest {

    private static final String firstName = "First";
    private static final String lastName = "Last";
    private static final String fullName = firstName + " " + lastName;

    @Test
    public void initMethodTest() {
        final String[] args = {
            "-f", firstName,
            "-l", lastName
        };
        Assert.assertEquals(new Bean(firstName, lastName, fullName), Clash.init(Bean.class, args));
    }

    @Value
    @AllArgsConstructor
    private static class Bean {
        @Argument(shortNames = "f")
        String firstName;
        @Argument(shortNames = "l")
        String lastName;
        @NonFinal String fullName;

        @Init
        private void init() {
            fullName = firstName + " " + lastName;
        }
    }
}
