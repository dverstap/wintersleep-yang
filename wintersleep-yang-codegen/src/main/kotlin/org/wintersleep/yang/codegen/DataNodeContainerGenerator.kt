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

import com.squareup.kotlinpoet.*
import org.opendaylight.yangtools.yang.model.api.*
import org.opendaylight.yangtools.yang.model.api.type.*
import org.wintersleep.yang.model.*
import java.io.File

class DataNodeContainerGenerator(
        private val classNames: MutableSet<ClassName>,
        private val schemaNode: DataNodeContainer,
        private val outputDir: File
) {

    fun generate() {
        generateClass()
        for (childNode in schemaNode.childNodes) {
            if (childNode is DataNodeContainer) {
                DataNodeContainerGenerator(classNames, childNode, outputDir).generate()
            }
        }
    }

    private fun generateClass() {
        val className = makeClassName(schemaNode)
        if (className != null) {
            val sn = schemaNode as SchemaNode
            if (className in classNames) {
                //className = ClassName(className.packageName(), className.simpleName() + "2")
                throw IllegalArgumentException("Duplicated class $className for $schemaNode")
            } else {
                classNames.add(className)
            }
            val classBuilder = TypeSpec.classBuilder(className)
                    .superclass(YangContainerMetaData::class)
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameter(ParameterSpec.builder("yangParent",
                                    YangContainerMetaData::class.asTypeName().asNullable())
                                    .defaultValue("null")
                                    .build())
                            .build())
                    .addSuperclassConstructorParameter("yangParent")
                    .addSuperclassConstructorParameter(quote(sn.moduleName))
                    .addSuperclassConstructorParameter(quote(sn.path.lastComponent.namespace))
                    .addSuperclassConstructorParameter(quote(sn.path.lastComponent.localName))
            val duplicateFieldNames = makeDuplicateFieldNamesSet()
//            if (duplicateFieldNames.isNotEmpty()) {
//                println(duplicateFieldNames)
//            }
            for (childNode in schemaNode.childNodes) {
                addProperty(classBuilder, duplicateFieldNames, childNode)
            }
            //println("Writing $className")
            val file = FileSpec.builder(className.packageName(), className.simpleName())
                    .addType(classBuilder.build())
                    .build()
            file.writeTo(outputDir)
        } else {
            throw IllegalArgumentException("Skipping generateClass because of no ClassName: " + schemaNode)
        }
    }

    private fun quote(arg: Any) = "\"$arg\""

    private fun makeDuplicateFieldNamesSet(): Set<String> {
        val all = HashSet<String>()
        val result = HashSet<String>()
        for (childNode in schemaNode.childNodes) {
            val fieldName = fieldName(childNode)
            if (fieldName in all) {
                result.add(fieldName)
            } else {
                all.add(fieldName)
            }
        }
        return result
    }

    private fun addProperty(classBuilder: TypeSpec.Builder, duplicateFieldNames: Set<String>, childNode: DataSchemaNode) {
//        if (!addContainerField(classBuilder, childNode)) {
//            addLeafProperty(classBuilder, childNode)
//        }
        if (childNode is DataNodeContainer) {
            val childClassName = makeClassName(childNode)
            if (childClassName != null) {
                classBuilder.addProperty(PropertySpec.builder(
                        uniqueFieldName(childNode, duplicateFieldNames),
                        childClassName)
                        .initializer("%T(this)", childClassName)
                        .addAnnotation(JvmField::class)
                        .build())
            } else {
                //println("Skipping addProperty because of no class name: $childNode")
                addLeafProperty(classBuilder, childNode)
            }
        } else {
            //println("Skipping addProperty because it is not a DataNodeContainer: $childNode")
            addLeafProperty(classBuilder, childNode)
        }
    }

    private fun fieldName(childNode: DataSchemaNode): String {
        return fieldName(childNode.path)
    }

    private fun fieldName(path: SchemaPath): String {
        return path.lastComponent.localName.codeName()
    }

    private fun uniqueFieldName(childNode: DataSchemaNode, duplicateFieldNames: Set<String>): String {
        val fieldName = fieldName(childNode)
        if (fieldName in duplicateFieldNames) {
            return childNode.path.lastComponent.namespace.last.codeName() + "_" + fieldName
        }
        return fieldName
    }

    private fun addLeafProperty(classBuilder: TypeSpec.Builder, childNode: DataSchemaNode) {
        val parameterType = determineYangParameterType(childNode)
        var initializer = "%T(this, %S, %S, %S)"

        if (childNode is TypedDataSchemaNode && (childNode.type is EnumTypeDefinition)) { // || childNode.type.baseType is EnumTypeDefinition)) {
            if (childNode.qName.localName == "transmission-system-capabilities") {
                println(childNode.qName)
                println(childNode.type.qName)
                println(childNode)
            }
            // childNode.type.qName
            // error: either namespace or tree based enums
            //val enumClassName = childNode.qName.toNamespaceClassName()
            //val enumClassName = childNode.type.qName.toNamespaceClassName()
            val enumClassName = childNode.kClassName
            initializer = "%T(this, %S, %S, %S, $enumClassName::class.java)"
        }
        classBuilder.addProperty(PropertySpec.builder(
                childNode.path.lastComponent.localName.codeName(),
                parameterType)
                .initializer(initializer,
                        parameterType,
                        childNode.moduleName,
                        childNode.path.lastComponent.namespace,
                        childNode.path.lastComponent.localName)
                .addAnnotation(JvmField::class)
                .build())
    }

    private fun determineYangParameterType(childNode: DataSchemaNode): TypeName {
        if (childNode is TypedDataSchemaNode) {
            // TODO visitor pattern
            if (childNode.type is BooleanTypeDefinition || childNode.type.baseType is BooleanTypeDefinition) {
                return YangBooleanParameter::class.asTypeName()
            } else if (childNode.type is Int8TypeDefinition || childNode.type.baseType is Int8TypeDefinition) {
                return YangByteParameter::class.asTypeName()
            } else if (childNode.type is Uint8TypeDefinition || childNode.type.baseType is Uint8TypeDefinition) {
                return YangUnsignedByteParameter::class.asTypeName()
            } else if (childNode.type is Int16TypeDefinition || childNode.type.baseType is Int16TypeDefinition) {
                return YangShortParameter::class.asTypeName()
            } else if (childNode.type is Uint16TypeDefinition || childNode.type.baseType is Uint16TypeDefinition) {
                return YangUnsignedShortParameter::class.asTypeName()
            } else if (childNode.type is Int32TypeDefinition || childNode.type.baseType is Int32TypeDefinition) {
                return YangIntegerParameter::class.asTypeName()
            } else if (childNode.type is Uint32TypeDefinition || childNode.type.baseType is Uint32TypeDefinition) {
                return YangUnsignedIntegerParameter::class.asTypeName()
            } else if (childNode.type is Int64TypeDefinition || childNode.type.baseType is Int64TypeDefinition) {
                return YangLongParameter::class.asTypeName()
            } else if (childNode.type is Uint64TypeDefinition || childNode.type.baseType is Uint64TypeDefinition) {
                return YangUnsignedLongParameter::class.asTypeName()
            } else if (childNode.type is DecimalTypeDefinition || childNode.type.baseType is DecimalTypeDefinition) {
                return YangDecimalParameter::class.asTypeName()
            } else if (childNode.type is EnumTypeDefinition || childNode.type.baseType is EnumTypeDefinition) {
//                val enumClassName = childNode.type.qName.toNamespaceClassName()
                val enumClassName = childNode.kClassName
                return ParameterizedTypeName.get(YangEnumParameter::class.asClassName(), enumClassName)
            } else if (childNode.type is StringTypeDefinition || childNode.type.baseType is StringTypeDefinition) {
                return YangStringParameter::class.asTypeName()
            } else if (childNode.type is BinaryTypeDefinition || childNode.type.baseType is BinaryTypeDefinition) {
                return YangBinaryParameter::class.asTypeName()
            }
        }
        return YangJsonParameter::class.asTypeName()
    }

//    private fun addContainerField(classBuilder: TypeSpec.Builder, childNode: DataSchemaNode): Boolean {
//        if (childNode is ContainerSchemaNode) {
//            val className = makeClassName(childNode)
//            if (className != null) {
//                classBuilder.addProperty(PropertySpec.builder(
//                        childNode.path.lastComponent.localName.codeName(),
//                        className)
//                        .initializer("%T()", className)
//                        .addAnnotation(JvmField::class)
//                        .build())
//                return true
//            }
//        }
//        return false
//    }

    private fun makeClassName(container: DataNodeContainer): ClassName? {
        // Using the namespace for packages generates duplicated classes,
        // which have and need to have different content. See for instance
        // InterfaceMetaData (under interfaces/interfaces-state).
        // This is why we use the xml/json tree for the package names.
        return if (container is ContainerSchemaNode) {
            container.path.toTreeClassName("MetaData")
        } else if (container is ListSchemaNode) {
            container.path.toTreeClassName("MetaData")
        } else if (container is GroupingDefinition) {
//            container.path.lastComponent.toClassName("Group")
            throw IllegalArgumentException()
        } else {
            // TODO more types?
            null
        }
    }
}
