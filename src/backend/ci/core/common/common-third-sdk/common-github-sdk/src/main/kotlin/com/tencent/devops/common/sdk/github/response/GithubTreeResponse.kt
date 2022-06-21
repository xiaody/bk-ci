package com.tencent.devops.common.sdk.github.response

import com.tencent.devops.common.sdk.github.pojo.GithubTree

data class GithubTreeResponse(
    val sha:String,
    val url:String,
    val tree:List<GithubTree>,
    val truncated:Boolean
)
