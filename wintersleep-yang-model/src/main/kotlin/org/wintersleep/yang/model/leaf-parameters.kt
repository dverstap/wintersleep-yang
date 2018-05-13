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
import javax.json.JsonNumber
import javax.json.JsonObject
import javax.json.JsonString
import javax.json.JsonValue

abstract class YangAbstractLeafParameter<T, JsonResultType : JsonValue>(yangParent: YangContainerMetaData,
                                                                        yangModule: String,
                                                                        yangNamespace: String,
                                                                        yangName: String,
                                                                        private val jsonResultTypeClass: Class<JsonResultType>)
    : YangAbstractParameter<T, JsonResultType>(yangParent, yangModule, yangNamespace, yangName, jsonResultTypeClass),
        YangLeafParameter<T> {

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

    abstract fun convert(jsonValue: JsonResultType): T

}

open class YangJsonLeafParameter(yangParent: YangContainerMetaData,
                                 yangModule: String,
                                 yangNamespace: String,
                                 yangName: String)
    : YangAbstractLeafParameter<JsonValue, JsonValue>(yangParent, yangModule, yangNamespace, yangName, JsonValue::class.java) {

    override fun convert(jsonValue: JsonValue): JsonValue {
        return jsonValue
    }

}

class YangBinaryParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafParameter<ByteArray, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun convert(jsonValue: JsonString): ByteArray {
        return Base64.getDecoder().decode(jsonValue.string)
    }

}

// TODO Implement real support for bits, using a generated enum class identifying the bits.
class YangBitsParameter(yangParent: YangContainerMetaData,
                        yangModule: String,
                        yangNamespace: String,
                        yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangBooleanParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafParameter<Boolean, JsonValue>(yangParent, yangModule, yangNamespace, yangName, JsonValue::class.java) {

    override fun convert(jsonValue: JsonValue): Boolean {
        return when (jsonValue.valueType) {
            JsonValue.ValueType.TRUE -> java.lang.Boolean.TRUE
            JsonValue.ValueType.FALSE -> java.lang.Boolean.FALSE
            else -> throw IllegalArgumentException("Not a valid boolean: $jsonValue")
        }
    }

}


class YangByteParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Byte>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Byte {
        return convertInteger(jsonValue, Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()
    }

}


class YangDecimalParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Decimal64>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Decimal64 {
        return Decimal64.valueOf(jsonValue.bigDecimalValue())
    }

}


class YangEmptyParameter(yangParent: YangContainerMetaData,
                         yangModule: String,
                         yangNamespace: String,
                         yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangEnumParameter<T : YangEnum>(
        yangParent: YangContainerMetaData,
        yangModule: String,
        yangNamespace: String,
        yangName: String,
        val enumClass: Class<T>
) : YangAbstractLeafParameter<T, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    private val valueMap = enumClass.enumConstants.map { it.yangName to it }.toMap()

    override fun convert(jsonValue: JsonString): T {
        val name = jsonValue.string
        return valueMap[name] ?: throw IllegalArgumentException("'$name' is not a valid constant in $enumClass: ${valueMap.keys}")
    }

}


class YangIdentityParameter(yangParent: YangContainerMetaData,
                            yangModule: String,
                            yangNamespace: String,
                            yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangIdentityRefParameter(yangParent: YangContainerMetaData,
                               yangModule: String,
                               yangNamespace: String,
                               yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangInstanceIdentifierParameter(yangParent: YangContainerMetaData,
                                      yangModule: String,
                                      yangNamespace: String,
                                      yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangIntegerParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Int>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Int {
        return convertInteger(jsonValue, Int.MIN_VALUE, Int.MAX_VALUE)
    }

}


class YangLeafRefParameter(yangParent: YangContainerMetaData,
                           yangModule: String,
                           yangNamespace: String,
                           yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)

class YangLongParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Long>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Long {
        return convertLong(jsonValue, Long.MIN_VALUE, Long.MAX_VALUE)
    }

}

// TODO: https://tools.ietf.org/html/rfc7951#section-6.1 says that "int64", "uint64" and "decimal64"
// must be represented as json strings, not numbers
abstract class YangNumberParameter<T : Number>(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafParameter<T, JsonNumber>(yangParent, yangModule, yangNamespace, yangName, JsonNumber::class.java) {

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


class YangShortParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Short>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Short {
        return convertInteger(jsonValue, Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }

}


class YangStringParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangAbstractLeafParameter<String, JsonString>(yangParent, yangModule, yangNamespace, yangName, JsonString::class.java) {

    override fun convert(jsonValue: JsonString): String {
        Preconditions.checkNotNull(jsonValue.string, "$jsonFullPath: string parameter value is not allowed to be null.")
        return jsonValue.string
    }

}


class YangUnionParameter(yangParent: YangContainerMetaData,
                         yangModule: String,
                         yangNamespace: String,
                         yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangUnknownParameter(yangParent: YangContainerMetaData,
                           yangModule: String,
                           yangNamespace: String,
                           yangName: String)
    : YangJsonLeafParameter(yangParent, yangModule, yangNamespace, yangName)


class YangUnsignedByteParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Uint8>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Uint8 {
        return Uint8.valueOf(convertInteger(jsonValue, 0, 0xFF))
    }

}


class YangUnsignedIntegerParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<UnsignedInteger>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): UnsignedInteger {
        return UnsignedInteger.valueOf(convertLong(jsonValue, UnsignedInteger.ZERO.toLong(), UnsignedInteger.MAX_VALUE.toLong()))
    }

}


class YangUnsignedLongParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<UnsignedLong>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): UnsignedLong {
        return UnsignedLong.valueOf(convertBigInteger(jsonValue, UnsignedLong.ZERO.bigIntegerValue(), UnsignedLong.MAX_VALUE.bigIntegerValue()))
    }

}

class YangUnsignedShortParameter(yangParent: YangContainerMetaData, yangModule: String, yangNamespace: String, yangName: String)
    : YangNumberParameter<Uint16>(yangParent, yangModule, yangNamespace, yangName) {

    override fun convert(jsonValue: JsonNumber): Uint16 {
        return Uint16.valueOf(convertInteger(jsonValue, 0, 0xFFFF))
    }

}

