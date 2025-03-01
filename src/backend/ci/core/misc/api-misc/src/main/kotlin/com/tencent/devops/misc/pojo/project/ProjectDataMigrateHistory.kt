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

package com.tencent.devops.misc.pojo.project

import com.tencent.devops.common.api.enums.SystemModuleEnum
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("项目数据迁移历史记录")
data class ProjectDataMigrateHistory(
    @ApiModelProperty("主键Id", required = true)
    val id: String,
    @ApiModelProperty("项目Id", required = true)
    val projectId: String,
    @ApiModelProperty("模块标识", required = true)
    val moduleCode: SystemModuleEnum,
    @ApiModelProperty("被迁移集群名称", required = true)
    val sourceClusterName: String,
    @ApiModelProperty("被迁移数据源名称", required = true)
    val sourceDataSourceName: String,
    @ApiModelProperty("迁移集群名称", required = true)
    val targetClusterName: String,
    @ApiModelProperty("迁移数据源名称", required = true)
    val targetDataSourceName: String,
    @ApiModelProperty("迁移数据源标签", required = false)
    val targetDataTag: String? = null
)
