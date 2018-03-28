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
package org.wintersleep.yang.model

import org.junit.Assert.*
import org.junit.Test

class YangNodeMetaDataTest {

    @Test
    fun testRoot() {
        val md = make(0)
        assertJsonName("test-module:name-0", md)
        assertJsonFullPath("test-module:name-0", md)
        assertJsonPathFromRoot("", md)
    }

    @Test
    fun testChild1() {
        val md = make(1)
        assertJsonName("name-1", md)
        assertJsonFullPath("test-module:name-0/name-1", md)
        assertJsonPathFromRoot("name-1", md)
    }

    @Test
    fun testChild2() {
        val md = make(2)
        assertJsonName("name-2", md)
        assertJsonFullPath("test-module:name-0/name-1/name-2", md)
        assertJsonPathFromRoot("name-1/name-2", md)
    }

    @Test
    fun testJsonPathWithDifferentModules() {
        val node0 = make(module = "module-0", name = "name-0")
        assertJsonName("module-0:name-0", node0)
        assertJsonFullPath("module-0:name-0", node0)
        assertJsonPathFromRoot("", node0)

        val node1 = make(node0, module = "module-0", name = "name-1")
        assertJsonName("name-1", node1)
        assertJsonFullPath("module-0:name-0/name-1", node1)
        assertJsonPathFromRoot("name-1", node1)

        val node2 = make(node1, module = "module-2", name = "name-2")
        assertJsonName("module-2:name-2", node2)
        assertJsonFullPath("module-0:name-0/name-1/module-2:name-2", node2)
        assertJsonPathFromRoot("name-1/module-2:name-2", node2)

        val node3 = make(node2, module = "module-2", name = "name-3")
        assertJsonName("name-3", node3)
        assertJsonFullPath("module-0:name-0/name-1/module-2:name-2/name-3", node3)
        assertJsonPathFromRoot("name-1/module-2:name-2/name-3", node3)
    }

    @Test
    fun testToString() {
        val nodeMetaData = make()
        // println(nodeMetaData)
        assertNull(nodeMetaData.yangParent)
        assertTrue(nodeMetaData.toString().contains("test-module"))
        assertTrue(nodeMetaData.toString().contains("test-namespace"))
        assertTrue(nodeMetaData.toString().contains("test-name"))
        assertFalse(nodeMetaData.toString().contains("test-module/test-name"))
    }

    private fun make(parent: YangNodeMetaData? = null,
                     module: String = "test-module",
                     namespace: String = "test-namespace",
                     name: String = "test-name") = YangNodeMetaData(parent, module, namespace, name)

    private fun make(n: Int): YangNodeMetaData {
        var result: YangNodeMetaData? = null
        for (i in 0 until n + 1) {
            result = YangNodeMetaData(result, "test-module", "test-namespace", "name-" + i)
        }
        return result!!
    }

    private fun assertJsonName(expected: String, md: YangNodeMetaData) {
        assertEquals(expected, md.jsonName)
    }

    private fun assertJsonFullPath(expected: String, md: YangNodeMetaData) {
        assertEquals(expected, md.jsonFullPath.toString())
    }

    private fun assertJsonPathFromRoot(expected: String, md: YangNodeMetaData) {
        assertEquals(expected, md.jsonPathFromRoot.toString())
    }

}
