package com.demonwav.clash.test;

import com.demonwav.clash.Argument;
import com.demonwav.clash.Clash;
import com.demonwav.clash.Init;
import java.util.Objects;
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

    private static class Bean {
        @Argument(shortName = "f")
        private final String firstName;
        @Argument(shortName = "l")
        private final String lastName;
        private String fullName;

        private Bean(String firstName, String lastName, String fullName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = fullName;
        }

        @Init
        private void init() {
            fullName = firstName + " " + lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Bean bean = (Bean) o;
            return Objects.equals(firstName, bean.firstName) &&
                Objects.equals(lastName, bean.lastName) &&
                Objects.equals(fullName, bean.fullName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName, fullName);
        }
    }
}
