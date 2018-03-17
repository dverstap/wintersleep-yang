/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-bbf
 * %%
 * Copyright (C) 2017 - 2018 Davy Verstappen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
