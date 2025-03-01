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

package com.tencent.devops.project.pojo

import com.tencent.devops.common.auth.api.pojo.SubjectScopeInfo
import com.tencent.devops.project.pojo.enums.ProjectAuthSecrecyStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("项目-修改模型")
data class ProjectUpdateInfo(
    @ApiModelProperty("项目名称")
//    @JsonProperty("project_name")
    val projectName: String,
    @ApiModelProperty("项目类型")
//    @JsonProperty("project_type")
    val projectType: Int = 0,
    @ApiModelProperty("事业群ID")
//    @JsonProperty("bg_id")
    val bgId: Long = 0,
    @ApiModelProperty("事业群名字")
//    @JsonProperty("bg_name")
    val bgName: String = "",
    @ApiModelProperty("中心ID")
//    @JsonProperty("center_id")
    val centerId: Long = 0,
    @ApiModelProperty("中心名称")
//    @JsonProperty("center_name")
    val centerName: String = "",
    @ApiModelProperty("部门ID")
//    @JsonProperty("dept_id")
    val deptId: Long = 0,
    @ApiModelProperty("部门名称")
//    @JsonProperty("dept_name")
    val deptName: String = "",
    @ApiModelProperty("描述")
    val description: String,
    @ApiModelProperty("英文缩写")
//    @JsonProperty("english_name")
    val englishName: String = "",
    @ApiModelProperty("cc app id")
//    @JsonProperty("cc_app_id")
    val ccAppId: Long?,
    @ApiModelProperty("cc app name")
//    @JsonProperty("cc_app_name")
    var ccAppName: String?, // APP name 通过调用CC接口同步
    @ApiModelProperty("容器选择， 0 是不选， 1 是k8s, 2 是mesos")
    val kind: Int?,
    @ApiModelProperty("是否保密")
    var secrecy: Boolean = false,
    @ApiModelProperty("项目相关配置")
    val properties: ProjectProperties? = null,
    @ApiModelProperty("项目最大可授权人员范围")
    val subjectScopes: List<SubjectScopeInfo>? = emptyList(),
    @ApiModelProperty("logo地址")
    val logoAddress: String? = null,
    @ApiModelProperty("项目性质")
    val authSecrecy: Int? = ProjectAuthSecrecyStatus.PUBLIC.value
)
