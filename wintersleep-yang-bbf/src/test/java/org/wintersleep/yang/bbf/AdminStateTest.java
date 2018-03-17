package org.wintersleep.yang.bbf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdminStateTest {

    @Test
    public void test() {
        assertEnum("unknown", "unknown", 1, AdminState.unknown);
        assertEnum("locked", "locked", 2, AdminState.locked);
        assertEnum("shutting_down", "shutting-down", 3, AdminState.shutting_down);
        assertEnum("unlocked", "unlocked", 4, AdminState.unlocked);
    }

    private static void assertEnum(String expectedName, String expectedYangName, int expectedYangValue, AdminState enumConstant) {
        assertEquals(expectedName, enumConstant.name());
        assertEquals(expectedYangName, enumConstant.getYangName());
        assertEquals(expectedYangValue, enumConstant.getYangValue());
    }

}