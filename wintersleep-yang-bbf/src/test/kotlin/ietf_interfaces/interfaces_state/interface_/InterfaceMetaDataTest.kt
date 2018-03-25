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
package ietf_interfaces.interfaces_state.interface_

import org.junit.Assert.assertEquals
import org.junit.Test
import org.wintersleep.yang.model.YangNodeMetaData

class InterfaceMetaDataTest {

    @Test
    fun test() {
        val itf = InterfaceMetaData()
        assertEquals("ietf-interfaces", itf.yangModule)
        assertEquals("ietf-interfaces", itf.admin_status.yangModule)
        assertJsonPathFromRoot("admin-status", itf.admin_status)

//        println(itf.line.bbf_fast_line)
//        println(itf.line.bbf_fast_line.status)
//        println(itf.line.bbf_fast_line.status.downstream)
//        println(itf.line.bbf_fast_line.status.downstream.hlog)
//        println(itf.line.bbf_fast_line.status.downstream.hlog.hlog_sub_carrier_group_size)

        assertJsonPathFromRoot("bbf-fastdsl:line/bbf-fast:line/status/downstream/hlog/hlog-sub-carrier-group-size",
                itf.line.bbf_fast_line.status.downstream.hlog.hlog_sub_carrier_group_size)
        assertJsonPathFromRoot("bbf-fastdsl:line/bbf-fast:line/status/downstream/hlog/hlogps",
                itf.line.bbf_fast_line.status.downstream.hlog.hlogps)
    }

    private fun assertJsonPathFromRoot(expected: String, actual: YangNodeMetaData) {
        assertEquals(expected, actual.jsonPathFromRoot.toString())
    }

}
