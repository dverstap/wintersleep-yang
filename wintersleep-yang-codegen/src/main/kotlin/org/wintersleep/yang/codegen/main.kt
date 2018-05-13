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

import com.squareup.kotlinpoet.ClassName
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.*
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils.parseYangFiles
import org.wintersleep.yang.schema.YangTools.findMyModuleTopDir
import org.wintersleep.yang.schema.YangTools.findYangFiles
import org.wintersleep.yang.schema.allTypedDataSchemaNodes
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
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
    validateAssumptions(schemaContext)
    val beginCodeGenTimestamp = System.currentTimeMillis()

    val enumTypesMap = getEnumTypesMap(schemaContext)
    for (entry in enumTypesMap) {
        EnumTypeGenerator(outputDir, entry.value, entry.key).generate()
    }
    println("#Enum types:" + enumTypesMap.size)

    val classNames = HashSet<ClassName>()
    for (childNode in schemaContext.childNodes) {
        if (childNode is DataNodeContainer) { // CaseSchemaNode is a DataNodeContainer
            DataNodeContainerGenerator(classNames, childNode, outputDir).generate()
        } else if (childNode is ChoiceSchemaNode) { // but ChoiceSchemaNode is not
            for (case in childNode.cases) {
                DataNodeContainerGenerator(classNames, case.value, outputDir)
            }
        }
    }

    //var nodeCount = 0
    val tree = LinkedHashMap<String, Any>()
    for (childNode in schemaContext.childNodes) {
//        val childName = childNode.qName.localName
        // val childName = childNode.path.toLocalPath()
        val childName = childNode.path.last
        tree[childName] = TreeGenerator(childNode, "").generate()
    }
    //println("#nodes: $nodeCount")

    val codeGenDuration = System.currentTimeMillis() - beginCodeGenTimestamp
    println("Parse phase duration:           $parseDuration")
    println("Code generation phase duration: $codeGenDuration")

    val options = DumperOptions()
    options.indent = 2
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    val yaml = Yaml(EmptyNullRepresenter(), options);
    File("tree.yaml").writeText(yaml.dump(tree))
}

class EmptyNullRepresenter : Representer() {
    override fun representScalar(tag: Tag?, value: String?): Node {
        if (value == "null") {
            return representScalar(tag, "", DumperOptions.ScalarStyle.PLAIN)
        }
        return super.representScalar(tag, value)
    }
}


fun validateAssumptions(schemaContext: SchemaContext) {
    for (module in schemaContext.modules) {
        if (module.name != module.namespace.last) {
            throw IllegalArgumentException("Module name ${module.name} does not match last part of module namespace ${module.namespace}")
        }
        for (submodule in module.submodules) {
            if (module.name != submodule.namespace.last) {
                throw IllegalArgumentException("Module name ${module.name} does not match last part of submodule namespace ${submodule.namespace}")
            }
        }
    }
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
    // does not make a difference:
//    for (grouping in schemaContext.groupings) {
//        for (typeDefinition in schemaContext.typeDefinitions) {
//            if (typeDefinition is EnumTypeDefinition) {
//                result.addWithDuplicateCheck(typeDefinition.qName, typeDefinition, typeDefinition.path)
//            }
//        }
//    }
    result.putAll(getAnonymousEnumTypes(schemaContext))
    return result
}

// TODO leaf-list support: Map<QName, LeafNode/ListLeafNode->TypedDataSchemaNode >
fun getAnonymousEnumTypes(schemaContext: SchemaContext): MutableMap<QName, EnumTypeDefinition> {
    val result: MutableMap<QName, EnumTypeDefinition> = LinkedHashMap()
    val allTypedDataSchemaNodes = schemaContext.allTypedDataSchemaNodes
    println("#TypedDataSchemaNode: ${allTypedDataSchemaNodes.size}")
    for (dataSchemaNode in allTypedDataSchemaNodes) {
        //println("" + dataSchemaNode::class + ": " + dataSchemaNode.type::class)
        val type = dataSchemaNode.type
        if (dataSchemaNode.qName.localName == "rein-inter-arrival-time") { //.startsWith("rein-")) {
            println(dataSchemaNode)
            if (dataSchemaNode.type.qName.localName != "enumeration") {
                // TODO: study the difference between these two:
                println(dataSchemaNode.type) // this is the path within the concrete data tree (= duplicate class names?)
                println(dataSchemaNode.type.baseType)  // this is the path within the grouping (= unique class names?)
                println(dataSchemaNode.type.baseType.baseType)
                //throw IllegalArgumentException("rein-inter-arrival-time really is an anonymous enum type")
            }
        }
        if (type is EnumTypeDefinition) { // || type.baseType is EnumTypeDefinition) {
            //println("Adding " + dataSchemaNode.qName)
            //if (type.qName.localName == "enumeration") { // avoid duplicate generation of named enums
            result.addWithDuplicateCheck(dataSchemaNode.qName, type, dataSchemaNode.path)
            //}
        }
    }
//    for (grouping in schemaContext.groupings) {
//        for (typeDefinition in schemaContext.typeDefinitions) {
//            if (typeDefinition is EnumTypeDefinition) {
//                result.addWithDuplicateCheck(typeDefinition.qName, typeDefinition, typeDefinition.path)
//            }
//        }
//    }

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

