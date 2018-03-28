/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-model
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
package org.wintersleep.yang.model

import org.junit.Assert.*
import org.junit.Test
import javax.json.JsonObject
import javax.json.spi.JsonProvider

class YangLongParameterTest {

    companion object {
        const val SEVENTY_SEVEN: Long = 77
        const val MINUS_ONE: Long = -1
    }

    @Test
    fun testCast() {
        assertEquals(SEVENTY_SEVEN, 77.toLong())
        assertEquals(0xFFL, 255.toLong())
    }

    @Test
    fun testGetValueOk() {
        assertEquals(SEVENTY_SEVEN, param().getValue(obj(77)))
    }

    @Test
    fun testGetValueNok() {
        try {
            param().getValue(obj(77, "unknown-leaf"))
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("m:container/leaf: Could not find 'leaf' in '{\"unknown-leaf\":77}'.", e.message)
        }
    }

    @Test
    fun testFindValueOk() {
        assertEquals(SEVENTY_SEVEN, param().findValue(obj(77)))
    }

    @Test
    fun testFindValueNok() {
        assertNull(param().findValue(obj(77, "unknown-leaf")))
    }

    private fun obj(i: Long, name: String = "leaf"): JsonObject = JsonProvider.provider().createObjectBuilder().add(name, i).build()

    private fun param() = YangLongParameter(
            YangContainerMetaData(null, "m", "m", "container"),
            "m", "n", "leaf")

}
