/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-yang2kt
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
package org.wintersleep.yang.yang2kt

import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.*
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils.parseYangFiles
import org.wintersleep.yang.schema.YangTools.findMyModuleTopDir
import org.wintersleep.yang.schema.YangTools.findYangFiles
import org.wintersleep.yang.schema.allTypedDataSchemaNodes
import java.io.File
import java.util.*
import kotlin.collections.LinkedHashMap

fun main(args: Array<String>) {
    val topDir = findMyModuleTopDir("wintersleep-yang-bbf")
    val files = findYangFiles(File(topDir, "src/main/yang"))
    // println(files)
    val outputDir = File(topDir, "target/generated-sources/yang")

    val beginParseTimestamp = System.currentTimeMillis()
    val schemaContext = parseYangFiles(files)
    val parseDuration = System.currentTimeMillis() - beginParseTimestamp

    val beginCodeGenTimestamp = System.currentTimeMillis()
    val enumTypesMap = getEnumTypesMap(schemaContext)
    for (entry in enumTypesMap) {
        EnumTypeGenerator(outputDir, entry.value, entry.key).generate()
    }
    println("#Enum types:" + enumTypesMap.size)
    val codeGenDuration = System.currentTimeMillis() - beginCodeGenTimestamp
    println("Parse phase duration:           $parseDuration")
    println("Code generation phase duration: $codeGenDuration")
}

//fun generateDataContainers(dataSchemaNode: DataSchemaNode, indent: String, outputDir: File) {
//    //println(indent + dataSchemaNode.path.lastComponent.localName)
//    if (dataSchemaNode is DataNodeContainer) {
//        if (dataSchemaNode is ContainerSchemaNode) {
//            DataNodeContainerGenerator(dataSchemaNode, outputDir).generate()
//        }
//        for (childNode in dataSchemaNode.childNodes) {
//            generateDataContainers(childNode, indent + "  ", outputDir)
//        }
//    }
//}
//
//fun getLeafNodes(dataSchemaNode: DataSchemaNode): Collection<LeafSchemaNode> {
//    val result = ArrayList<LeafSchemaNode>()
//    if (dataSchemaNode is LeafSchemaNode) {
//        result.add(dataSchemaNode)
//    } else if (dataSchemaNode is DataNodeContainer) {
//        for (childNode in dataSchemaNode.childNodes) {
//            result.addAll(getLeafNodes(childNode))
//        }
//    }
//    return result
//}

// TODO union typedefs can contain anonymous enum types: e.g. composite-downstream-wavelength-type
fun getEnumTypesMap(schemaContext: SchemaContext): Map<QName, EnumTypeDefinition> {
    val result = TreeMap<QName, EnumTypeDefinition>()
    for (typeDefinition in schemaContext.typeDefinitions) {
        if (typeDefinition is EnumTypeDefinition) {
            result.addWithDuplicateCheck(typeDefinition.qName, typeDefinition, typeDefinition.path)
        }
    }
    result.putAll(getAnonymousEnumTypes(schemaContext))
    return result
}

// TODO leaf-list support: Map<QName, LeafNode/ListLeafNode->TypedDataSchemaNode >
fun getAnonymousEnumTypes(schemaContext: SchemaContext): MutableMap<QName, EnumTypeDefinition> {
    val result: MutableMap<QName, EnumTypeDefinition> = LinkedHashMap()
    val allTypedDataSchemaNodes = schemaContext.allTypedDataSchemaNodes
    println(allTypedDataSchemaNodes.size)
    for (dataSchemaNode in allTypedDataSchemaNodes) {
        //println("" + dataSchemaNode::class + ": " + dataSchemaNode.type::class)
        val type = dataSchemaNode.type
        if (type is EnumTypeDefinition) {
            //println("Adding " + dataSchemaNode.qName)
            if (type.qName.localName == "enumeration") { // avoid duplicate generation of named enums
                result.addWithDuplicateCheck(dataSchemaNode.qName, type, dataSchemaNode.path)
            }
        }
    }
    return result
//    if (dataSchemaNode is LeafSchemaNode) {
//        val type = dataSchemaNode.type
//        if (type is EnumTypeDefinition) {
//            if (type.qName.localName == "enumeration") { // avoid duplicate generation of named enums
//                result.addWithDuplicateCheck(dataSchemaNode.qName, type, dataSchemaNode.path)
//            }
//        }
//    } else if (dataSchemaNode is DataNodeContainer) {
//        for (childNode in dataSchemaNode.childNodes) {
//            getAnonymousEnumTypes(childNode, result)
//        }
//    }
}

private fun MutableMap<QName, EnumTypeDefinition>.addWithDuplicateCheck(qName: QName, type: EnumTypeDefinition, path: SchemaPath) {
    if (qName in this) {
        warnDuplicateQName(qName, path, this[qName]!!.path)
    } else {
        put(qName, type)
    }
}

fun warnDuplicateQName(qName: QName, currentPath: SchemaPath, previousPath: SchemaPath) {
    // This is probably not a problem
    println("Warning: duplicate qname: $qName")
    println(" Previous: " + previousPath.toString().replace(',', '\n'))
    println(" Current:  " + currentPath.toString().replace(',', '\n'))
}

