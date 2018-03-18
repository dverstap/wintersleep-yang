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

import com.squareup.kotlinpoet.*
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.wintersleep.yang.model.YangEnum
import java.io.File
import java.net.URI

internal class EnumTypeGenerator(
        private val outputDir: File,
        private val definition: EnumTypeDefinition,
        private val qName: QName = definition.qName
) {

    fun generate(): ClassName {
        val className = qName.toClassName()
        val enumBuilder = TypeSpec.enumBuilder(className)
                .addSuperinterface(YangEnum::class)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("val yangName", String::class)
                                .addModifiers(KModifier.OVERRIDE)
                                .build())
                        .addParameter(ParameterSpec.builder("val yangValue", Int::class)
                                .addModifiers(KModifier.OVERRIDE)
                                .build())
                        .build())
        for ((k, v) in definition.values) {
            enumBuilder.addEnumConstant(k.codeEnumConstantName(),
                    TypeSpec.anonymousClassBuilder("\"%L\", %L", k, v).build())
        }
        val file = FileSpec.builder(className.packageName(), className.simpleName())
                .addType(enumBuilder.build())
                .build()
        //println("Writing: " + className)
        file.writeTo(outputDir)
        return className
    }

}

private fun QName.toClassName(): ClassName {
    val classShortName = this.localName.codeClassName()
    val packageName = this.namespace.codePackageName()
    return ClassName(packageName, classShortName)
}


private fun String.upCaseFirst(): CharSequence {
    if (isEmpty()) {
        return this
    }
    return get(0).toUpperCase() + substring(1)
}

fun URI.codePackageName(): String {
    return this.toString().replace(':', '.').replace('-', '_')
}

fun String.codeClassName(): String {
    // classname with dashes are not valid according to ASM:
    // return codeName()
    val parts = split('-')
    return parts.joinToString(separator = "", transform = { it.upCaseFirst() })
}

private fun String.codeEnumConstantName(): String {
    // We want the names to be usable in Java as well, plus putting backticks
    // around the names (in Kotlin) is not very convenient either.
    if (this == "open" || this == "short") {
        return "_" + this
    }
    val result = this.replace('-', '_').replace('.', '_')
    if (!Character.isJavaIdentifierStart(result[0])) {
        return "_" + result
    }
    return result
    // Like Square Wire, not changing this to upper-case, to stick closer to the original name.
    // Although in this case, it's less interesting, because we have to change the name anyway in some cases.
    // return codeName()
}

fun String.codeName(): String {
    var result = this.replace('.', '_')
    if ('-' in result) {
        result = "`" + result + "`"
    }
    if (result == "interface") {
        result += "_"
    }
    return result
}

private operator fun EnumTypeDefinition.EnumPair.component2(): Int {
    return value
}

private operator fun EnumTypeDefinition.EnumPair.component1(): String {
    return name
}
