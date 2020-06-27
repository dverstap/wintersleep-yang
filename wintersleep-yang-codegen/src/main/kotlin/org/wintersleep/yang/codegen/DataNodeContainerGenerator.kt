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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import org.opendaylight.yangtools.yang.model.api.*
import org.opendaylight.yangtools.yang.model.api.type.*
import org.wintersleep.yang.model.*
import java.io.File
import kotlin.reflect.KClass

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
            } else if (childNode is ChoiceSchemaNode) {
                for (case in childNode.cases) {
                    DataNodeContainerGenerator(classNames, case.value, outputDir).generate()
                }
            }
        }
    }

    private fun generateClass() {
        val className = makeClassName(schemaNode)
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
                                YangContainerMetaData::class.asTypeName().copy(nullable = true))
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
        val file = FileSpec.builder(className.packageName, className.simpleName)
                .addType(classBuilder.build())
                .build()
        file.writeTo(outputDir)
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
        when (childNode) {
            is TypedDataSchemaNode -> // leaf or leaf-list
                addLeafProperty(classBuilder, childNode)
            is DataNodeContainer -> {
                val childClassName = makeClassName(childNode)
                classBuilder.addProperty(PropertySpec.builder(
                        uniqueFieldName(childNode, duplicateFieldNames),
                        childClassName)
                        .initializer("%T(this)", childClassName)
                        .addAnnotation(JvmField::class)
                        .build())
            }
            is ChoiceSchemaNode -> for (case in childNode.cases) {
                for (grandChildNode in case.value.childNodes) {
                    addProperty(classBuilder, duplicateFieldNames, grandChildNode)
                }
            }
            else -> TODO("Unsupported: $childNode")
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

    private fun addLeafProperty(classBuilder: TypeSpec.Builder, childNode: TypedDataSchemaNode) {
        val parameterType = determineYangParameterType(childNode)
        var initializer = "%T(this, %S, %S, %S)"

        if (childNode.type is EnumTypeDefinition) { // || childNode.type.baseType is EnumTypeDefinition)) {
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

    private fun determineYangParameterType(childNode: TypedDataSchemaNode): TypeName {
        // TODO visitor pattern
        if (childNode.type is BooleanTypeDefinition || childNode.type.baseType is BooleanTypeDefinition) {
            return leafOrList(childNode, YangBooleanParameter::class, YangBooleanListParameter::class)
        } else if (childNode.type is Int8TypeDefinition || childNode.type.baseType is Int8TypeDefinition) {
            return leafOrList(childNode, YangByteParameter::class, YangByteListParameter::class)
        } else if (childNode.type is Uint8TypeDefinition || childNode.type.baseType is Uint8TypeDefinition) {
            return leafOrList(childNode, YangUnsignedByteParameter::class, YangUnsignedByteListParameter::class)
        } else if (childNode.type is Int16TypeDefinition || childNode.type.baseType is Int16TypeDefinition) {
            return leafOrList(childNode, YangShortParameter::class, YangShortListParameter::class)
        } else if (childNode.type is Uint16TypeDefinition || childNode.type.baseType is Uint16TypeDefinition) {
            return leafOrList(childNode, YangUnsignedShortParameter::class, YangUnsignedShortListParameter::class)
        } else if (childNode.type is Int32TypeDefinition || childNode.type.baseType is Int32TypeDefinition) {
            return leafOrList(childNode, YangIntegerParameter::class, YangIntegerListParameter::class)
        } else if (childNode.type is Uint32TypeDefinition || childNode.type.baseType is Uint32TypeDefinition) {
            return leafOrList(childNode, YangUnsignedIntegerParameter::class, YangUnsignedIntegerListParameter::class)
        } else if (childNode.type is Int64TypeDefinition || childNode.type.baseType is Int64TypeDefinition) {
            return leafOrList(childNode, YangLongParameter::class, YangLongListParameter::class)
        } else if (childNode.type is Uint64TypeDefinition || childNode.type.baseType is Uint64TypeDefinition) {
            return leafOrList(childNode, YangUnsignedLongParameter::class, YangUnsignedLongListParameter::class)
        } else if (childNode.type is DecimalTypeDefinition || childNode.type.baseType is DecimalTypeDefinition) {
            return leafOrList(childNode, YangDecimalParameter::class, YangDecimalListParameter::class)
        } else if (childNode.type is EnumTypeDefinition || childNode.type.baseType is EnumTypeDefinition) {
            val enumClassName = childNode.kClassName
            return leafOrList(childNode, YangEnumParameter::class, YangEnumListParameter::class).plusParameter(enumClassName)
        } else if (childNode.type is StringTypeDefinition || childNode.type.baseType is StringTypeDefinition) {
            return leafOrList(childNode, YangStringParameter::class, YangStringListParameter::class)
        } else if (childNode.type is BinaryTypeDefinition || childNode.type.baseType is BinaryTypeDefinition) {
            return leafOrList(childNode, YangBinaryParameter::class, YangBinaryListParameter::class)
        } else if (childNode.type is UnionTypeDefinition || childNode.type.baseType is UnionTypeDefinition) {
            return leafOrList(childNode, YangUnionParameter::class, YangUnionListParameter::class)
        } else if (childNode.type is BitsTypeDefinition || childNode.type.baseType is BitsTypeDefinition) {
            return leafOrList(childNode, YangBitsParameter::class, YangBitsListParameter::class)
        } else if (childNode.type is LeafrefTypeDefinition || childNode.type.baseType is LeafrefTypeDefinition) {
            return leafOrList(childNode, YangLeafRefParameter::class, YangLeafRefListParameter::class)
        } else if (childNode.type is IdentityTypeDefinition || childNode.type.baseType is IdentityTypeDefinition) {
            return leafOrList(childNode, YangIdentityParameter::class, YangIdentityListParameter::class)
        } else if (childNode.type is IdentityrefTypeDefinition || childNode.type.baseType is IdentityrefTypeDefinition) {
            return leafOrList(childNode, YangIdentityRefParameter::class, YangIdentityRefListParameter::class)
        } else if (childNode.type is InstanceIdentifierTypeDefinition || childNode.type.baseType is InstanceIdentifierTypeDefinition) {
            return leafOrList(childNode, YangInstanceIdentifierParameter::class, YangInstanceIdentifierListParameter::class)
        } else if (childNode.type is EmptyTypeDefinition || childNode.type.baseType is EmptyTypeDefinition) {
            return leafOrList(childNode, YangEmptyParameter::class, YangEmptyListParameter::class)
        } else if (childNode.type is UnknownTypeDefinition || childNode.type.baseType is UnknownTypeDefinition) {
            return leafOrList(childNode, YangUnknownParameter::class, YangUnknownListParameter::class)
        } else {
            throw IllegalArgumentException("Unknown type: ${childNode.type}")
        }
    }

    // TODO stricter contraints on KClass type parameters:
    private fun leafOrList(node: TypedDataSchemaNode, leafClass: KClass<*>, leafListClass: KClass<*>): ClassName {
        if (node is LeafSchemaNode) {
            return leafClass.asClassName()
        } else if (node is LeafListSchemaNode) {
            return leafListClass.asClassName()
        }
        throw IllegalArgumentException("Cannot handle type of : $node")
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

    private fun makeClassName(container: DataNodeContainer): ClassName {
        // Using the namespace for packages generates duplicated classes,
        // which have and need to have different content. See for instance
        // InterfaceMetaData (under interfaces/interfaces-state).
        // This is why we use the xml/json tree for the package names.
        return if (container is ContainerSchemaNode) {
            container.path.toTreeClassName("MetaData")
        } else if (container is ListSchemaNode) {
            container.path.toTreeClassName("MetaData")
        } else if (container is CaseSchemaNode) {
            container.path.toTreeClassName("MetaData")
        } else {
            TODO("Type support needed for: $container")
        }
    }
}
