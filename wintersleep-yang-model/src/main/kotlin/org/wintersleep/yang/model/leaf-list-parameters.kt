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
import com.google.common.primitives.UnsignedInteger
import com.google.common.primitives.UnsignedLong
import org.opendaylight.yangtools.yang.common.Decimal64
import org.opendaylight.yangtools.yang.common.Uint16
import org.opendaylight.yangtools.yang.common.Uint8
import java.math.BigInteger
import java.util.*
import javax.json.*


abstract class YangAbstractLeafListParameter<T, in JsonResultType : JsonValue>(yangParent: YangContainerMetaData,
                                                                               yangModule: String,
                                                                               yangNamespace: String,
                                                                               yangName: String,
                                                                               private val jsonResultTypeClass: Class<JsonResultType>)
    : YangAbstractParameter<List<T>, JsonArray>(yangParent, yangModule, yangNamespace, yangName, JsonArray::class.java),
        YangLeafListParameter<T> {

    override fun findValue(obj: JsonObject): List<T>? {
        val jsonValue = findJsonValue(obj)
        if (jsonValue != null) {
            return convertArray(jsonValue)
        }
        return null
    }

    override fun getValue(obj: JsonObject): List<T> {
        val value = getJsonValue(obj)
        return convertArray(value)
    }

    private fun convertArray(jsonArray: JsonArray): List<T> {
        val result = arrayListOf<T>();
        for (jsonValue in jsonArray) {
            result.add(convert(jsonValue as JsonResultType))
        }
        return result
    }

    abstract fun convert(jsonValue: JsonResultType): T

}

open class YangJsonLeafListParameter(yangParent: YangContainerMetaData,
                                     yangModule: String,
                                     yangNamespace: String,
                                     yangName: String)
    : YangAbstractLeafListParameter<JsonValue, JsonValue>(yangParent, yangModule, yangNamespace, yangName, JsonValue::class.java) {

    override fun convert(jsonValue: JsonValue): JsonValue {
        return jsonValue
    }

}


class YangBinaryListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafListParameter<ByteArray, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun convert(jsonValue: JsonString): ByteArray {
        return Base64.getDecoder().decode(jsonValue.string)
    }

}

// TODO Implement real support for bits, using a generated enum class identifying the bits.
class YangBitsListParameter(yangParent: YangContainerMetaData,
                            yangModule: String,
                            yangNamespace: String,
                            yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangBooleanListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafListParameter<Boolean, JsonValue>(yangParent, yangModule, yangNamespace, yangName, JsonValue::class.java) {

    override fun convert(jsonValue: JsonValue): Boolean {
        return when (jsonValue.valueType) {
            JsonValue.ValueType.TRUE -> java.lang.Boolean.TRUE
            JsonValue.ValueType.FALSE -> java.lang.Boolean.FALSE
            else -> throw IllegalArgumentException("Not a valid boolean: $jsonValue")
        }
    }

}


class YangByteListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Byte>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Byte {
        return convertInteger(jsonValue, Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()
    }

}


class YangDecimalListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Decimal64>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Decimal64 {
        return Decimal64.valueOf(jsonValue.bigDecimalValue())
    }

}


class YangEmptyListParameter(yangParent: YangContainerMetaData,
                             yangModule: String,
                             yangNamespace: String,
                             yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangEnumListParameter<T : YangEnum>(
        yangParent: YangContainerMetaData,
        yangModule: String,
        yangNamespace: String,
        yangName: String,
        val enumClass: Class<T>
) : YangAbstractLeafListParameter<T, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java),
        YangLeafListParameter<T> {

    private val valueMap = enumClass.enumConstants.map { it.yangName to it }.toMap()

    override fun convert(jsonValue: JsonString): T {
        val name = jsonValue.string
        return valueMap[name] ?: throw IllegalArgumentException("'$name' is not a valid constant in $enumClass: ${valueMap.keys}")
    }

}


class YangIdentityListParameter(yangParent: YangContainerMetaData,
                                yangModule: String,
                                yangNamespace: String,
                                yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangIdentityRefListParameter(yangParent: YangContainerMetaData,
                                   yangModule: String,
                                   yangNamespace: String,
                                   yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangInstanceIdentifierListParameter(yangParent: YangContainerMetaData,
                                          yangModule: String,
                                          yangNamespace: String,
                                          yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangIntegerListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Int>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Int {
        return convertInteger(jsonValue, Int.MIN_VALUE, Int.MAX_VALUE)
    }

}


class YangLeafRefListParameter(yangParent: YangContainerMetaData,
                               yangModule: String,
                               yangNamespace: String,
                               yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)

class YangLongListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Long>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Long {
        return convertLong(jsonValue, Long.MIN_VALUE, Long.MAX_VALUE)
    }

}


// TODO: https://tools.ietf.org/html/rfc7951#section-6.1 says that "int64", "uint64" and "decimal64"
// must be represented as json strings, not numbers
abstract class YangNumberListParameter<T : Number>(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafListParameter<T, JsonNumber>(yangParent, yangModule, yangNamespace, yangName, JsonNumber::class.java) {

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


class YangShortListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Short>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Short {
        return convertInteger(jsonValue, Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

}


class YangStringListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafListParameter<String, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun convert(jsonValue: JsonString): String {
        Preconditions.checkNotNull(jsonValue.string, "$jsonFullPath: string parameter value is not allowed to be null.")
        return jsonValue.string
    }

}


class YangUnionListParameter(yangParent: YangContainerMetaData,
                             yangModule: String,
                             yangNamespace: String,
                             yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangUnknownListParameter(yangParent: YangContainerMetaData,
                               yangModule: String,
                               yangNamespace: String,
                               yangName: String)
    : YangJsonLeafListParameter(yangParent, yangModule, yangNamespace, yangName)


class YangUnsignedByteListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Uint8>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Uint8 {
        return Uint8.valueOf(convertInteger(jsonValue, 0, 0xFF))
    }

}


class YangUnsignedIntegerListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<UnsignedInteger>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): UnsignedInteger {
        return UnsignedInteger.valueOf(convertLong(jsonValue, UnsignedInteger.ZERO.toLong(), UnsignedInteger.MAX_VALUE.toLong()))
    }

}


class YangUnsignedLongListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<UnsignedLong>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): UnsignedLong {
        return UnsignedLong.valueOf(convertBigInteger(jsonValue, UnsignedLong.ZERO.bigIntegerValue(), UnsignedLong.MAX_VALUE.bigIntegerValue()))
    }

}

class YangUnsignedShortListParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberListParameter<Uint16>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Uint16 {
        return Uint16.valueOf(convertInteger(jsonValue, 0, 0xFFFF))
    }

}
