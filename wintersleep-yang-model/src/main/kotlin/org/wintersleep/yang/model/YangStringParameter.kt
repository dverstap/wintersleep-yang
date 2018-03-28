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

import com.google.common.base.Preconditions
import javax.json.JsonObject
import javax.json.JsonString

class YangStringParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractParameter<String, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun findValue(obj: JsonObject): String? {
        val jsonValue = findJsonValue(obj)
        if (jsonValue != null) {
            return convert(jsonValue)
        }
        return null
    }

    override fun getValue(obj: JsonObject): String {
        val value = getJsonValue(obj)
        return convert(value)
    }

    private fun convert(jsonValue: JsonString): String {
        Preconditions.checkNotNull(jsonValue.string, "$jsonFullPath: string parameter value is not allowed to be null.")
        return jsonValue.string
    }

}
