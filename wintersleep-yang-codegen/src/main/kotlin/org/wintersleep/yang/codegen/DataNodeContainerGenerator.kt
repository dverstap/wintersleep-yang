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
package org.wintersleep.yang.codegen

import com.squareup.kotlinpoet.*
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import java.io.File
import javax.xml.namespace.QName

class DataNodeContainerGenerator(
        private val schemaNode: ContainerSchemaNode,
        private val outputDir: File
) {

    fun generate() {
        val classShortName = schemaNode.path.lastComponent.localName.codeClassName()
        val className = ClassName("org.wintersleep.yang.bbf", classShortName)
        val classBuilder = TypeSpec.classBuilder(className)
        for (childNode in schemaNode.childNodes) {
            classBuilder.addProperty(PropertySpec.builder(
                    childNode.path.lastComponent.localName.codeName(),
                    QName::class)
                    .initializer("QName(%S, %S)",
                            childNode.path.lastComponent.namespace,
                            childNode.path.lastComponent.localName)
                    .build())
        }
        val file = FileSpec.builder(className.packageName(), className.simpleName())
                .addType(classBuilder.build())
                .build()
        file.writeTo(outputDir)
    }


}
