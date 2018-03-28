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
import javax.json.JsonString

class YangEnumParameter<T : YangEnum>(
        yangParent: YangContainerMetaData,
        yangModule: String,
        yangNamespace: String,
        yangName: String,
        val enumClass: Class<T>
) : YangAbstractParameter<T, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    private val valueMap = enumClass.enumConstants.map { it.yangName to it }.toMap()

    override fun findValue(obj: JsonObject): T? {
        val jsonValue = findJsonValue(obj) ?: return null
        return convert(jsonValue.string);
    }

    private fun convert(name: String): T {
        return valueMap[name] ?: throw IllegalArgumentException("'$name' is not a valid constant in ${enumClass}")
    }

    override fun getValue(obj: JsonObject): T {
        return convert(getJsonValue(obj).string);
    }

}
