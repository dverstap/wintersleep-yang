/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-model
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
package org.wintersleep.yang.model

// property names are prefixed with yang, to avoid name clashes with fields in generated subclasses
open class YangNodeMetaData(
        /** The MetaData of the parent of this node in the Yang-modelled data tree. null for the root node */
        val yangParent: YangNodeMetaData?,
        /** The module name is needed for JSON representations of Yang-modelled data */
        val yangModule: String,
        /** The namespace is needed for XML representations of Yang-modelled data */
        val yangNamespace: String,
        /** The short name of the node in the tree. */
        val yangName: String
) {

    val jsonName: String = if (yangParent == null || yangParent.yangModule != yangModule) {
        yangModule + ":" + yangName
    } else {
        yangName
    }

    /** Includes the root node */
    val jsonFullPath: YangJsonPath
        get() {
            val result = mutableListOf<YangNodeMetaData>()
            var node: YangNodeMetaData? = this
            while (node != null) {
                result.add(node)
                node = node.yangParent
            }
            return YangJsonPath(result.asReversed())
        }

    /** Excludes the root node */
    val jsonPathFromRoot: YangJsonPath
        get() {
            val result = mutableListOf<YangNodeMetaData>()
            var node: YangNodeMetaData? = this
            while (node != null && node.yangParent != null) {
                result.add(node)
                node = node.yangParent
            }
            return YangJsonPath(result.asReversed())
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as YangNodeMetaData

        if (yangParent != other.yangParent) return false
        if (yangModule != other.yangModule) return false
        if (yangNamespace != other.yangNamespace) return false
        if (yangName != other.yangName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yangParent?.hashCode() ?: 0
        result = 31 * result + yangModule.hashCode()
        result = 31 * result + yangNamespace.hashCode()
        result = 31 * result + yangName.hashCode()
        return result
    }

    override fun toString(): String {
        return "$jsonFullPath[$yangNamespace]"
    }


}
