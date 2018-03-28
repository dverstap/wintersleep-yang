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

class YangByteParameterTest {

    companion object {
        const val SEVENTY_SEVEN: Byte = 77
        const val MINUS_ONE: Byte = -1
    }

    @Test
    fun testCast() {
        assertEquals(SEVENTY_SEVEN, 77.toByte())
        assertEquals(MINUS_ONE, 255.toByte())
    }

    @Test
    fun testGetValueOk() {
        assertEquals(SEVENTY_SEVEN, param().getValue(obj(77)))
    }

    @Test
    fun testGetValueWhenNotPresent() {
        try {
            param().getValue(obj(77, "unknown-leaf"))
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("m:container/leaf: Could not find 'leaf' in '{\"unknown-leaf\":77}'.", e.message)
        }
    }

    @Test
    fun testGetValueWhenTooSmall() {
        try {
            param().getValue(obj(-129))
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("m:container/leaf: -129 is not in range [-128..127].", e.message)
        }
    }

    @Test
    fun testGetValueWhenTooBig() {
        try {
            param().getValue(obj(128))
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("m:container/leaf: 128 is not in range [-128..127].", e.message)
        }
    }

    @Test
    fun testGetValueWhenNotAnInteger() {
        try {
            param().getValue(obj(1.1))
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals("m:container/leaf: 1.1 is not an exact 32-bit integer.", e.message)
            assertEquals(e.cause!!.javaClass, ArithmeticException::class.java)
        }
    }

    @Test
    fun testFindValueOk() {
        assertEquals(SEVENTY_SEVEN, param().findValue(obj(77)))
    }

    @Test
    fun testFindValueWhenNotPresent() {
        assertNull(param().findValue(obj(77, "unknown-leaf")))
    }

    private fun obj(i: Int, name: String = "leaf"):
            JsonObject = JsonProvider.provider().createObjectBuilder().add(name, i).build()

    private fun obj(d: Double, name: String = "leaf"):
            JsonObject = JsonProvider.provider().createObjectBuilder().add(name, d).build()

    private fun param() = YangByteParameter(
            YangContainerMetaData(null, "m", "m", "container"),
            "m", "n", "leaf")

}
