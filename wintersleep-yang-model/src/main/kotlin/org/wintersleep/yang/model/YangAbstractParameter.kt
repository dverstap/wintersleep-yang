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

import javax.json.JsonException
import javax.json.JsonObject
import javax.json.JsonValue

abstract class YangAbstractParameter<T, out JsonResultType : JsonValue>(yangParent: YangContainerMetaData,
                                                                        yangModule: String,
                                                                        yangNamespace: String,
                                                                        yangName: String,
                                                                        private val jsonResultTypeClass: Class<JsonResultType>)
    : YangNodeMetaData(yangParent, yangModule, yangNamespace, yangName),
        YangParameter<T> {

    internal fun findJsonValue(obj: JsonObject): JsonResultType? {
        return findJsonValue(obj, jsonPathFromRoot.stringList)
    }

    private fun findJsonValue(obj: JsonObject, path: List<String>): JsonResultType? {
        val childName = path.first()
        val child = obj[childName]
        if (child == null) {
            return null
        }
        if (path.size == 1) {
            if (jsonResultTypeClass.isInstance(child)) {
                return jsonResultTypeClass.cast(child)
            } else {
                throw IllegalArgumentException("$jsonFullPath: '$path' in '$obj' is not of the expected type $jsonResultTypeClass.")
            }
        }
        return findJsonValue(child as JsonObject, path.subList(1, path.size))
    }

    @Throws(JsonException::class)
    internal fun getJsonValue(obj: JsonObject): JsonResultType {
        val value = findJsonValue(obj)
        if (value == null) {
            throw IllegalArgumentException("$jsonFullPath: Could not find '$jsonPathFromRoot' in '$obj'.")
        }
        return jsonResultTypeClass.cast(value)
    }

    override fun setValue(obj: JsonObject, value: T) {
        TODO("not implemented")
    }

}
