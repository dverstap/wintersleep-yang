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
package ietf_interfaces.interfaces_state.interface_;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaInterfaceMetaDataTest {

    @Test
    public void test() {
        InterfaceMetaData itf = new InterfaceMetaData();
        assertEquals("ietf-interfaces", itf.getYangModule());
        assertEquals("ietf-interfaces", itf.admin_status.getYangModule());
        assertEquals("ietf-interfaces:interface/admin-status", itf.admin_status.getJsonFullPath().toString());
        assertEquals("admin-status", itf.admin_status.getJsonPathFromRoot().toString());
    }

}
