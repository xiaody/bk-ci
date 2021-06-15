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

package com.tencent.devops.common.webhook.service.code.handler.tgit

import com.tencent.devops.common.pipeline.pojo.element.trigger.enums.CodeEventType
import com.tencent.devops.common.webhook.annotation.CodeWebhookHandler
import com.tencent.devops.common.webhook.pojo.code.WebHookParams
import com.tencent.devops.common.webhook.pojo.code.git.GitReviewEvent
import com.tencent.devops.common.webhook.service.code.filter.CodeReviewStateFilter
import com.tencent.devops.common.webhook.service.code.filter.EventTypeFilter
import com.tencent.devops.common.webhook.service.code.filter.UrlFilter
import com.tencent.devops.common.webhook.service.code.filter.WebhookFilter
import com.tencent.devops.common.webhook.service.code.handler.CodeWebhookTriggerHandler
import com.tencent.devops.common.webhook.util.WebhookUtils
import com.tencent.devops.repository.pojo.Repository
import com.tencent.devops.scm.pojo.BK_REPO_GIT_WEBHOOK_REVIEW_RESTRICT_TYPE
import com.tencent.devops.scm.pojo.BK_REPO_GIT_WEBHOOK_REVIEW_REVIEWABLE_ID
import com.tencent.devops.scm.pojo.BK_REPO_GIT_WEBHOOK_REVIEW_REVIEWABLE_TYPE
import com.tencent.devops.scm.utils.code.git.GitUtils

@CodeWebhookHandler
class TGitReviewTriggerHandler : CodeWebhookTriggerHandler<GitReviewEvent> {
    override fun eventClass(): Class<GitReviewEvent> {
        return GitReviewEvent::class.java
    }

    override fun getUrl(event: GitReviewEvent): String {
        return event.repository.git_http_url
    }

    override fun getUsername(event: GitReviewEvent): String {
        return ""
    }

    override fun getRevision(event: GitReviewEvent): String {
        return ""
    }

    override fun getRepoName(event: GitReviewEvent): String {
        return GitUtils.getProjectName(event.repository.git_ssh_url)
    }

    override fun getBranchName(event: GitReviewEvent): String {
        return ""
    }

    override fun getEventType(): CodeEventType {
        return CodeEventType.REVIEW
    }

    override fun getMessage(event: GitReviewEvent): String? {
        return ""
    }

    override fun retrieveParams(event: GitReviewEvent, projectId: String?, repository: Repository?): Map<String, Any> {
        val startParams = mutableMapOf<String, Any>()
        startParams[BK_REPO_GIT_WEBHOOK_REVIEW_REVIEWABLE_ID] = event.reviewableId
        startParams[BK_REPO_GIT_WEBHOOK_REVIEW_REVIEWABLE_TYPE] = event.reviewableType
        startParams[BK_REPO_GIT_WEBHOOK_REVIEW_RESTRICT_TYPE] = event.restrictType
        return startParams
    }

    override fun getWebhookFilters(
        event: GitReviewEvent,
        projectId: String,
        pipelineId: String,
        repository: Repository,
        webHookParams: WebHookParams
    ): List<WebhookFilter> {
        with(webHookParams) {
            val urlFilter = UrlFilter(
                pipelineId = pipelineId,
                triggerOnUrl = getUrl(event),
                repositoryUrl = repository.url,
                includeHost = includeHost
            )
            val eventTypeFilter = EventTypeFilter(
                pipelineId = pipelineId,
                triggerOnEventType = getEventType(),
                eventType = eventType
            )
            val codeReviewStateFilter = CodeReviewStateFilter(
                pipelineId = pipelineId,
                triggerOnState = event.state,
                includedState = WebhookUtils.convert(includeCrState)
            )
            return listOf(urlFilter, eventTypeFilter, codeReviewStateFilter)
        }
    }
}
