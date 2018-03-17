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
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import java.io.File

internal class EnumTypeGenerator(
        private val definition: EnumTypeDefinition,
        private val outputDir: File
) {

    fun generate() {
        val classShortName = definition.qName.localName.codeClassName()
        val className = ClassName("org.wintersleep.yang.bbf", classShortName)
        val enumBuilder = TypeSpec.enumBuilder(className)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("val yangName", String::class).build())
                        .addParameter(ParameterSpec.builder("val yangValue", Int::class).build())
                        .build())
        for ((k, v) in definition.values) {

            var constantName = k.codeEnumClassName()
            if (constantName == classShortName) {
                constantName = "_" + constantName;
            }
            enumBuilder.addEnumConstant(constantName,
                    TypeSpec.anonymousClassBuilder("\"%L\", %L", k, v).build())
        }

        val file = FileSpec.builder(className.packageName(), className.simpleName())
                .addType(enumBuilder.build())
                .build()
        file.writeTo(outputDir)
    }

}

private fun String.upCaseFirst(): CharSequence {
    if (isEmpty()) {
        return this
    }
    return get(0).toUpperCase() + substring(1)
}

fun String.codeClassName(): String {
    // classname with dashes are not valid according to ASM:
    // return codeName()
    val parts = split('-')
    return parts.joinToString(separator = "", transform = { it.upCaseFirst() })
}

private fun String.codeEnumClassName(): String {
    // We want the names to be usable in Java as well, plus putting backticks
    // around the names (in Kotlin) is not very convenient either.
    return this.replace('-', '_')
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
