package org.wintersleep.yang.yang2kt

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