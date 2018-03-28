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

import java.math.BigInteger
import javax.json.JsonNumber
import javax.json.JsonObject

// TODO add Decimal64 subclass
abstract class YangNumberParameter<T : Number>(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractParameter<T, JsonNumber>(yangParent, yangModule, yangNamespace, yangName, JsonNumber::class.java) {

    override fun findValue(obj: JsonObject): T? {
        val jsonValue = findJsonValue(obj)
        if (jsonValue != null) {
            return convert(jsonValue)
        }
        return null
    }

    override fun getValue(obj: JsonObject): T {
        val value = getJsonValue(obj)
        return convert(value)
    }

    protected abstract fun convert(jsonValue: JsonNumber): T

    protected fun convertInteger(jsonValue: JsonNumber, min: Int, max: Int): Int {
        val value: Int
        try {
            value = jsonValue.intValueExact()
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException("$jsonFullPath: $jsonValue is not an exact 32-bit integer.", e)
        }
        if (value !in min..max) {
            throw IllegalArgumentException("$jsonFullPath: $value is not in range [$min..$max].")
        }
        return value
    }

    protected fun convertLong(jsonValue: JsonNumber, min: Long, max: Long): Long {
        val value: Long
        try {
            value = jsonValue.longValueExact()
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException("$jsonFullPath: $jsonValue is not an exact 64-bit integer.", e)
        }
        if (value !in min..max) {
            throw IllegalArgumentException("$jsonFullPath: $value is not in range [$min..$max].")
        }
        return value
    }

    protected fun convertBigInteger(jsonValue: JsonNumber, min: BigInteger, max: BigInteger): BigInteger {
        val value: BigInteger
        try {
            value = jsonValue.bigIntegerValueExact()
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException("$jsonFullPath: $jsonValue is not an exact integer.", e)
        }
        if (value !in min..max) {
            throw IllegalArgumentException("$jsonFullPath: $value is not in range [$min..$max].")
        }
        return value
    }


}
