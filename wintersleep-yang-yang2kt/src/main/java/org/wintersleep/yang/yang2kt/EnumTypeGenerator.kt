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
                        .addParameter(ParameterSpec.builder("yangName", String::class).build())
                        .addParameter(ParameterSpec.builder("yangValue", Int::class).build())
                        .build())
        for ((k, v) in definition.values) {
            enumBuilder.addEnumConstant(k.codeEnumClassName(),
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

private fun String.codeClassName(): String {
    val parts = split('-')
    return parts.joinToString(separator = "", transform = { it.upCaseFirst() })
}

private fun String.codeEnumClassName(): String {
    // Like Square Wire, not changing this to upper-case, to stick closer to the original name.
    // Although in this case, it's less interesting, because we have to change the name anyway in some cases.
    return this.replace('-', '_')
}

private operator fun EnumTypeDefinition.EnumPair.component2(): Int {
    return value
}

private operator fun EnumTypeDefinition.EnumPair.component1(): String {
    return name
}
