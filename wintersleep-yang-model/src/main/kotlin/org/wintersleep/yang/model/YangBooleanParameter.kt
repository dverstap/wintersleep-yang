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

import javax.json.JsonObject
import javax.json.JsonValue

class YangBooleanParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractParameter<Boolean, JsonValue>(yangParent, yangModule, yangNamespace, yangName, JsonValue::class.java) {

    override fun findValue(obj: JsonObject): Boolean? {
        val jsonValue = findJsonValue(obj)
        return jsonValue != null && getBooleanValue(jsonValue)
    }

    override fun getValue(obj: JsonObject): Boolean {
        val value = getJsonValue(obj)
        return getBooleanValue(value)
    }

    private fun getBooleanValue(value: JsonValue): Boolean {
        return when (value.valueType) {
            JsonValue.ValueType.TRUE -> java.lang.Boolean.TRUE
            JsonValue.ValueType.FALSE -> java.lang.Boolean.FALSE
            else -> throw IllegalArgumentException("Not a valid boolean: " + value)
        }
    }

}
