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
package org.wintersleep.yang.bbf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.wintersleep.yang.bbf.BbfTestYangModel.schemaContext
import org.wintersleep.yang.schema.allModules
import org.wintersleep.yang.schema.mainModules
import org.wintersleep.yang.schema.subModules

class BbfTest {

    /*
     */
    // $ find . -name "*.yang" | wc -l
    // 165
    @Test
    fun allModules() {
        assertEquals(165, schemaContext.allModules.size) // confirmed there are 165 .yang files
        val combinedModules = LinkedHashSet<Module>()
        combinedModules.addAll(schemaContext.mainModules)
        combinedModules.addAll(schemaContext.subModules)
        assertEquals(schemaContext.allModules, combinedModules)
    }

    // $ find . -name "*.yang" | xargs grep "^module" | wc -l
    // 46
    @Test
    fun mainModules() {
        assertEquals(46, schemaContext.mainModules.size)
        assertTrue(schemaContext.allModules.containsAll(schemaContext.mainModules))
    }

    // $ find . -name "*.yang" | xargs grep "^submodule" | wc -l
    // 119
    @Test
    fun subModules() {
        assertEquals(119, schemaContext.subModules.size)
        assertTrue(schemaContext.allModules.containsAll(schemaContext.subModules))
    }

    @Test
    fun testTypeDefinitions() {
        val typedefs = LinkedHashSet<TypeDefinition<*>>()
        schemaContext.allModules.map { it.typeDefinitions }.forEach { typedefs.addAll(it) }
        assertEquals(schemaContext.typeDefinitions, typedefs)
    }

    @Test
    fun testDataDefinitions() {
        assertEquals(34, schemaContext.dataDefinitions.size) // This contains only the direct ones
    }
}
