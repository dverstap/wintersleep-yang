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
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.wintersleep.yang.model.YangEnum
import java.io.File

internal class EnumTypeGenerator(
        private val outputDir: File,
        private val definition: EnumTypeDefinition,
        private val qName: QName = definition.qName
) {

    fun generate(): ClassName {
        val className = qName.toNamespaceClassName()
        val constructorFunSpec = FunSpec.constructorBuilder()
                .addParameter("yangName", String::class)
                .addParameter("yangValue", Int::class)
                .build()
        val enumBuilder = TypeSpec.enumBuilder(className)
                .addSuperinterface(YangEnum::class)
                .primaryConstructor(constructorFunSpec)
                .addProperty(PropertySpec.builder("yangName", String::class)
                        .initializer("yangName")
                        .addModifiers(KModifier.OVERRIDE)
                        .build())
                .addProperty(PropertySpec.builder("yangValue", Int::class)
                        .initializer("yangValue")
                        .addModifiers(KModifier.OVERRIDE)
                        .build())
        for ((k, v) in definition.values) {
            enumBuilder.addEnumConstant(
                    k.codeEnumConstantName(),
                    TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter("%S", k)
                            .addSuperclassConstructorParameter("%L", v)
                            .build()
            )
        }
        val file = FileSpec.builder(className.packageName, className.simpleName)
                .addType(enumBuilder.build())
                .build()
        //println("Writing: " + className)
        file.writeTo(outputDir)
        return className
    }

}

private operator fun EnumTypeDefinition.EnumPair.component2(): Int {
    return value
}

private operator fun EnumTypeDefinition.EnumPair.component1(): String {
    return name
}
