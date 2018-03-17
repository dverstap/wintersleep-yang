package org.wintersleep.yang.yang2kt

import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils
import java.io.File
import java.nio.file.Paths
import java.util.*


fun findFiles(parent: File, result: MutableList<File>): List<File> {
    if (!parent.exists()) {
        throw IllegalArgumentException("Parent does not exist: " + parent)
    }
    if (parent.isDirectory) {
        for (child in parent.listFiles()) {
            findFiles(child, result)
        }
    } else if (parent.isFile and parent.name.endsWith(".yang")) {
        result.add(parent)
    }
    return result
}

fun main(args: Array<String>) {
    val topDir = findTopDir()

    val outputDir = File(topDir, "target/generated-sources/yang")

    val files = ArrayList<File>()
    findFiles(File(topDir, "src/main/yang"), files)
    println(files)

    val typeNames = TreeMap<QName, Class<Any>>()
    val enumTypes = HashSet<EnumTypeDefinition>()
    val schemaContext = YangParserTestUtils.parseYangFiles(files)
    for (m in schemaContext.modules) {
        //println(m.name)
//        for (dataDefinition in m.childNodes) {
//            generateDataContainers(dataDefinition, "  ", outputDir)
//        }
        for (sm in m.submodules) {
            for (typeDefinition in sm.typeDefinitions) {
                //println("  " + typeDefinition.qName + ": " + typeDefinition.javaClass.name)
                typeNames.put(typeDefinition.qName, typeDefinition.javaClass)
                if (typeDefinition is EnumTypeDefinition) {
                    enumTypes.add(typeDefinition)
                }
            }
        }
    }
    for (typeDefinition in schemaContext.typeDefinitions) {
        //println("  " + typeDefinition.qName + ": " + typeDefinition.javaClass.name)
        typeNames.put(typeDefinition.qName, typeDefinition.javaClass)
        if (typeDefinition is EnumTypeDefinition) {
            enumTypes.add(typeDefinition)
        }
    }

//    for ((k, v) in typeNames) {
//        println(k.localName + ": " + v.simpleName)
//    }
    println(typeNames.size)
    for (enumType in enumTypes) {
//        println(enumType.qName.localName)
//        for ((k, v) in enumType.values) {
//            println("  $k: $v")
//        }
        EnumTypeGenerator(enumType, outputDir).generate()
    }
    println(enumTypes.size)

}

fun findTopDir(): File {
    val pwd = Paths.get("").toAbsolutePath()
    if (pwd.endsWith(Paths.get("wintersleep-yang-bbf"))) {
        return pwd.toFile()
    }
    return pwd.resolve("wintersleep-yang-bbf").toFile()
}

fun generateDataContainers(dataSchemaNode: DataSchemaNode, indent: String, outputDir: File) {
    //println(indent + dataSchemaNode.path.lastComponent.localName)
    if (dataSchemaNode is DataNodeContainer) {
        if (dataSchemaNode is ContainerSchemaNode) {
            DataNodeContainerGenerator(dataSchemaNode, outputDir).generate()
        }
        for (childNode in dataSchemaNode.childNodes) {
            generateDataContainers(childNode, indent + "  ", outputDir)
        }
    }
}

