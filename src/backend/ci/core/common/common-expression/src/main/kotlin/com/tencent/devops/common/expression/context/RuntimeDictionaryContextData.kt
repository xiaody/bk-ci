/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.common.expression.context

import com.fasterxml.jackson.databind.JsonNode
import com.tencent.devops.common.expression.ContextDataRuntimeException
import com.tencent.devops.common.expression.expression.sdk.IReadOnlyObject
import com.tencent.devops.common.expression.utils.ExpressionJsonUtil
import java.lang.Exception
import java.util.TreeMap

interface RuntimeNamedValue {
    val key: String
    fun getValue(key: String): PipelineContextData?
}

/**
 * 通过函数参数在在运行时动态的获取value的Dict上下文
 * 例如：凭据
 * @throws ContextDataRuntimeException
 */
@Suppress("TooManyFunctions", "ReturnCount")
class RuntimeDictionaryContextData(private val runtimeNamedValue: RuntimeNamedValue) :
    PipelineContextData(PipelineContextDataType.DICTIONARY),
    Iterable<Pair<String, PipelineContextData?>>,
    IReadOnlyObject {

    private var mIndexLookup: TreeMap<String, Int>? = null
    private var mList: MutableList<DictionaryContextDataPair> = mutableListOf()

    private fun requestAndSaveValue(key: String): PipelineContextData? {
        return try {
            val value = runtimeNamedValue.getValue(key)
            if (value != null) {
                set(key, value)
            }
            value
        } catch (ignore: Exception) {
            throw ContextDataRuntimeException("RuntimeDictionaryContextData request key:$key 's value error")
        }
    }

    override val values: Iterable<Any?>
        get() {
            if (mList.isNotEmpty()) {
                return mList.map { it.value }
            }
            return emptyList()
        }

    override fun tryGetValue(key: String): Pair<PipelineContextData?, Boolean> {
        if (mList.isNotEmpty() && indexLookup.containsKey(key)) {
            return Pair(mList[indexLookup[key]!!].value, true)
        }
        // 对象中没有则去请求一次
        val value = requestAndSaveValue(key) ?: return Pair(null, false)

        return Pair(value, true)
    }

    private val indexLookup: MutableMap<String, Int>
        get() {
            if (mIndexLookup == null) {
                mIndexLookup = TreeMap<String, Int>()
                if (mList.isNotEmpty()) {
                    mList.forEachIndexed { index, pair ->
                        mIndexLookup!![pair.key] = index
                    }
                }
            }

            return mIndexLookup!!
        }

    private val list: MutableList<DictionaryContextDataPair>
        get() {
            return mList
        }

    operator fun set(k: String, value: PipelineContextData?) {
        // Existing
        val index = indexLookup[k]
        if (index != null) {
            val key = mList[index].key // preserve casing
            mList[index] = DictionaryContextDataPair(key, value)
        }
        // New
        else {
            add(k, value)
        }
    }

    operator fun get(k: String): PipelineContextData? {
        return tryGetValue(k).first
    }

    operator fun IReadOnlyObject.get(key: String): Any? {
        return tryGetValue(key).first
    }

    operator fun Pair<String, PipelineContextData>.get(key: Int): Pair<String, PipelineContextData?> {
        val pair = mList[key]
        return Pair(pair.key, pair.value)
    }

    fun add(pairs: Iterable<Pair<String, PipelineContextData>>) {
        pairs.forEach { pair ->
            add(pair.first, pair.second)
        }
    }

    fun add(
        key: String,
        value: PipelineContextData?
    ) {
        indexLookup[key] = mList.count()
        list.add(DictionaryContextDataPair(key, value))
    }

    override fun clone(): PipelineContextData {
        val result = RuntimeDictionaryContextData(runtimeNamedValue)

        if (mList.isNotEmpty()) {
            result.mList = mutableListOf()
            mList.forEach {
                result.mList.add(DictionaryContextDataPair(it.key, it.value?.clone()))
            }
        }

        return result
    }

    override fun toJson(): JsonNode {
        val json = ExpressionJsonUtil.createObjectNode()
        if (mList.isNotEmpty()) {
            mList.forEach {
                json.set<JsonNode>(it.key, it.value?.toJson())
            }
        }
        return json
    }

    override fun fetchValue(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        if (mList.isNotEmpty()) {
            mList.forEach {
                map[it.key] = it.value?.fetchValue() ?: ""
            }
        }
        return map
    }

    override fun iterator(): Iterator<Pair<String, PipelineContextData?>> {
        return mList.map { pair -> Pair(pair.key, pair.value); }.iterator()
    }

    private data class DictionaryContextDataPair(
        val key: String,
        val value: PipelineContextData?
    )
}
