/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-codegen
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
package org.wintersleep.yang.codegen

import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import java.util.*

class TreeGenerator(private val node: SchemaNode, private val indent: String) {
    fun generate(): Map<String, Any?> {
        // not stable: val result = LinkedHashMap<String, Any?>()
        val result = TreeMap<String, Any?>()
        if (node.path.lastComponent != null) {
            //println(indent + node.path.lastComponent.toString() + ": " + node.qName)
            //println(indent + node.path.lastComponent.localName + ": " + node.qName.localName)
            if (node.path.lastComponent !== node.qName) {
                println(indent + node.path.lastComponent.localName + ": " + node.qName.localName)
            }
        }
        if (node is DataNodeContainer) {
            for (childNode in node.childNodes) {
                //val childName = childNode.qName.localName
                //val childName = childNode.path.toLocalPath()
                val childName = childNode.path.last
                val childTree = TreeGenerator(childNode, "$indent  ").generate()
                if (childTree.isEmpty()) {
                    if (childNode is TypedDataSchemaNode && childNode.type is EnumTypeDefinition) {
//                        if (childNode.type.baseType == null) {
//                            throw IllegalArgumentException("baseType is null for: ${childNode.qName}: ${childNode.type.qName}")
//                        }
                        result[childName] = "${childNode.type.qName}=${childNode.type.baseType?.qName}"
                    } else {
                        result[childName] = null // TODO put type definition and other stuff here
                    }
//                    result[childName] = null // TODO put type definition and other stuff here
                } else {
                    result[childName] = childTree
                }
            }
        }
        return result
    }
}
