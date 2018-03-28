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

import com.google.common.base.Preconditions
import com.squareup.kotlinpoet.ClassName
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import java.net.URI


fun QName.toNamespaceClassName(suffix: String = ""): ClassName {
    val classShortName = this.localName.codeClassName() + suffix
    if (classShortName == "Enumeration") {
        throw IllegalArgumentException()
    }
    val packageName = this.namespace.codePackageName()
    return ClassName(packageName, classShortName)
}

fun SchemaPath.toTreeClassName(suffix: String = ""): ClassName {
    val classShortName = this.lastComponent.localName.codeClassName() + suffix
    //val packageName = this.pathFromRoot.map { it.localName.underscoreCodeId }.joinToString(".")
    return ClassName(toTreePackageName(), classShortName)
}

// TODO: drop the last part of the package name?
fun SchemaPath.toTreePackageName(): String {
    val result = ArrayList<String>()
    var prev: QName? = null
//    for (qName in pathTowardsRoot.reversed()) {
    for (qName in pathFromRoot) {
        if (prev == null || prev.module != qName.module) {
            result.add(qName.module.namespace.last.underscoreCodeId)
        }
        result.add(qName.localName.underscoreCodeId)
        prev = qName
    }
    return result.joinToString(".")
}

private fun String.upCaseFirst(): CharSequence {
    if (isEmpty()) {
        return this
    }
    return get(0).toUpperCase() + substring(1)
}

fun URI.codePackageName(): String {
    return this.toString().split(":").map { it.underscoreCodeId }.joinToString(".")
}

val String.underscoreCodeId: String
    get() {
        return this.codeName()
    }

fun String.codeClassName(): String {
    // classname with dashes are not valid according to ASM:
    // return codeName()
    val parts = split('-')
    return parts.joinToString(separator = "", transform = { it.upCaseFirst() })
}

private val KEYWORDS = hashSetOf("class", "interface", "open", "short")

fun String.codeEnumConstantName(): String {
    return codeName()
}

fun String.codeName(): String {
    // We want the names to be usable in Java as well, plus putting backticks
    // around the names (in Kotlin) is not very convenient either.
    val result = this.replace('-', '_').replace('.', '_').replace(':', '_')
    if (result in KEYWORDS) {
        return "${result}_" // underscore at the end, to help with completion
    }
    if (!Character.isJavaIdentifierStart(result[0])) {
        return "_$result" // obviously in this case the underscore has to be at the start
    }
    return result
}

// Like Square Wire, not changing this to upper-case, to stick closer to the original name.
// Although in this case, it's less interesting, because we have to change the name anyway in some cases.
//fun String.codeName(): String {
//    var result = this.replace('.', '_')
//    if ('-' in result) {
//        result = "`" + result + "`"
//    }
//    if (result == "interface") {
//        result += "_"
//    }
//    return result
//}

fun SchemaPath.toLocalPath(): String {
    var result = ""
    var prev: QName? = null
//    for (qName in pathTowardsRoot.reversed()) {
    for (qName in pathFromRoot) {
        if (prev == null || prev.module != qName.module) {
            result += "/${qName.module.namespace.last}:${qName.localName}"
        } else {
            result += "/${qName.localName}"
        }
        prev = qName
    }
    return result;
}

val URI.last: String
    get() {
        return schemeSpecificPart.split(':').last()
    }

val SchemaPath.last: String
    get() {
        return if (parent == null || parent.lastComponent?.module != lastComponent.module) {
            lastComponent.module.namespace.last + ":" + lastComponent.localName
        } else {
            lastComponent.localName
        }
    }

// The ODL API does not seem to have an easy way to get the module from any schema node,
// so this assumes that the last part of the namespace is the same as the module name.
// This assumption is validated in the compiler, and is ok for the BBF model.
val SchemaNode.moduleName: String
    get() {
        return path.lastComponent.module.namespace.last
    }

val TypedDataSchemaNode.kClassName: ClassName
    get() {
        if (type is EnumTypeDefinition) {
//            if (type.qName.localName == "enumeration") { // anonymous enum type
//                return qName.toNamespaceClassName()
//            } else {
//                return type.qName.toNamespaceClassName()
//            }
            val enumType = type as EnumTypeDefinition
            return enumType.enumNamespace.toNamespaceClassName()
        } else {
            throw IllegalArgumentException("$qName does not have an enum type.")
        }
    }

//val EnumTypeDefinition.enumNamespace: QName
//    get() {
//        if (baseType == null) {
//            Preconditions.checkState(qName.localName != "enumeration")
//            return qName
//        }
//        if (baseType.qName.localName == "enumeration") {
//            Preconditions.checkState(qName.localName != "enumeration")
//            return qName
//        } else {
//            return baseType.qName
//        }
//    }

val EnumTypeDefinition.enumNamespace: QName
    get() {
        if (qName.localName == "enumeration") {
            Preconditions.checkState(path.parent.lastComponent.localName != "enumeration")
            return path.parent.lastComponent
        } else {
            return qName
        }
    }