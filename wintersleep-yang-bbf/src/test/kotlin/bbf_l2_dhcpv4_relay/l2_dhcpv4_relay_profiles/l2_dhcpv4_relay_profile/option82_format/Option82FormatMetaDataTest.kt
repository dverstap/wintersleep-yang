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
package bbf_l2_dhcpv4_relay.l2_dhcpv4_relay_profiles.l2_dhcpv4_relay_profile.option82_format

import org.junit.Assert.assertEquals
import org.junit.Test
import urn.bbf.yang.bbf_l2_dhcpv4_relay.Suboptions
import urn.bbf.yang.bbf_l2_dhcpv4_relay.Suboptions.*
import javax.json.Json


class Option82FormatMetaDataTest {

    @Test(expected = IllegalArgumentException::class)
    fun testConvertEnumListWithIllegalConstant() {
        val metaData = Option82FormatMetaData()
        val json = Json.createObjectBuilder()
                .add("suboptions", Json.createArrayBuilder()
                        .add("illegal-enum-constant")
                        .build())
                .build()
        metaData.suboptions.getValue(json)
    }

    @Test
    fun testConvertEnumList() {
        val metaData = Option82FormatMetaData()
        val json = Json.createObjectBuilder()
                .add("suboptions", Json.createArrayBuilder()
                        .add("circuit-id")
                        .add("remote-id")
                        .add("access-loop-characteristics")
                        .build())
                .build()
        val value = metaData.suboptions.getValue(json)
        assertEquals(arrayListOf(circuit_id, remote_id, access_loop_characteristics),
                value)
    }

    @Test
    fun testConvertEmptyEnumList() {
        val metaData = Option82FormatMetaData()
        val json = Json.createObjectBuilder()
                .add("suboptions", Json.createArrayBuilder().build())
                .build()
        val value = metaData.suboptions.getValue(json)
        assertEquals(emptyList<Suboptions>(), value)
    }

}
