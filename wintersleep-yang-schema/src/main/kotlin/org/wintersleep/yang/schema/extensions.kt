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
package org.wintersleep.yang.schema

import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.*

val SchemaContext.mainModules: Set<Module>
    get() = modules

val SchemaContext.subModules: Set<Module>
    get() {
        val result = LinkedHashSet<Module>()
        for (module in modules) {
            // do not add top-level modules: result.add(module)
            for (submodule in module.submodules) {
                addModule(submodule, result)
            }
        }
        return result
    }

val SchemaContext.allModules: Set<Module>
    get() {
        val result = LinkedHashSet<Module>()
        for (module in modules) {
            addModule(module, result)
        }
        return result
    }

private fun addModule(parent: Module, result: MutableSet<Module>) {
    result.add(parent)
    for (child in parent.submodules) {
        addModule(child, result)
    }
}

val SchemaContext.allTypedDataSchemaNodes: Set<TypedDataSchemaNode>
    get() {
        val result = LinkedHashSet<TypedDataSchemaNode>()
        getTypedDataSchemaNodes(this, result)
        for (grouping in groupings) {
            for (childNode in grouping.childNodes) {
                getTypedDataSchemaNodes(childNode, result)
            }
        }
        return result
    }

fun getTypedDataSchemaNodes(dataSchemaNode: DataSchemaNode, result: MutableSet<TypedDataSchemaNode>) {
    if (dataSchemaNode is TypedDataSchemaNode) {
        result.addWithDuplicateCheck(dataSchemaNode.qName, dataSchemaNode, dataSchemaNode.path)
    } else if (dataSchemaNode is DataNodeContainer) {
        for (childNode in dataSchemaNode.childNodes) {
            getTypedDataSchemaNodes(childNode, result)
        }
    }
}

private fun MutableSet<TypedDataSchemaNode>.addWithDuplicateCheck(qName: QName, node: TypedDataSchemaNode, path: SchemaPath) {
    if (node in this) {
//        warnDuplicateQName(qName, path, this[qName]!!.path)
        throw IllegalAccessException("Duplicate $node")
    } else {
        add(node)
    }
}

fun warnDuplicateQName(qName: QName, currentPath: SchemaPath, previousPath: SchemaPath) {
    // This is probably not a problem
    println("Warning: duplicate qname: $qName")
    println(" Previous: " + previousPath.toString().replace(',', '\n'))
    println(" Current:  " + currentPath.toString().replace(',', '\n'))
}
