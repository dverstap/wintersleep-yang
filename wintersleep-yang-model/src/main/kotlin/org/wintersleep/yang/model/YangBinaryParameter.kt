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

import java.util.*
import javax.json.JsonObject
import javax.json.JsonString

class YangBinaryParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractParameter<ByteArray, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun findValue(obj: JsonObject): ByteArray? {
        val jsonValue = findJsonValue(obj)
        if (jsonValue != null) {
            return convert(jsonValue)
        }
        return null
    }

    override fun getValue(obj: JsonObject): ByteArray {
        val value = getJsonValue(obj)
        return convert(value)
    }

    private fun convert(value: JsonString): ByteArray {
        return Base64.getDecoder().decode(value.string)
    }

}
