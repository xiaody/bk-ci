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

package com.tencent.devops.stream.trigger.actions.tgit

import com.tencent.devops.common.client.Client
import com.tencent.devops.common.webhook.enums.code.tgit.TGitPushOperationKind
import com.tencent.devops.common.webhook.pojo.code.git.GitPushEvent
import com.tencent.devops.common.webhook.pojo.code.git.isCreateBranch
import com.tencent.devops.common.webhook.pojo.code.git.isDeleteBranch
import com.tencent.devops.common.webhook.pojo.code.git.isDeleteEvent
import com.tencent.devops.process.yaml.v2.enums.StreamObjectKind
import com.tencent.devops.process.yaml.v2.models.RepositoryHook
import com.tencent.devops.process.yaml.v2.models.Variable
import com.tencent.devops.process.yaml.v2.models.on.DeleteRule
import com.tencent.devops.process.yaml.v2.models.on.TriggerOn
import com.tencent.devops.process.yaml.v2.models.on.check
import com.tencent.devops.scm.utils.code.git.GitUtils
import com.tencent.devops.stream.dao.GitPipelineResourceDao
import com.tencent.devops.stream.pojo.GitRequestEvent
import com.tencent.devops.stream.pojo.enums.TriggerReason
import com.tencent.devops.stream.service.StreamPipelineBranchService
import com.tencent.devops.stream.trigger.actions.BaseAction
import com.tencent.devops.stream.trigger.actions.GitBaseAction
import com.tencent.devops.stream.trigger.actions.data.ActionData
import com.tencent.devops.stream.trigger.actions.data.ActionMetaData
import com.tencent.devops.stream.trigger.actions.data.StreamTriggerPipeline
import com.tencent.devops.stream.trigger.actions.tgit.data.TGitPushActionData
import com.tencent.devops.stream.trigger.actions.tgit.data.TGitPushEventCommonData
import com.tencent.devops.stream.trigger.exception.CommitCheck
import com.tencent.devops.stream.trigger.exception.StreamTriggerException
import com.tencent.devops.stream.trigger.git.pojo.ApiRequestRetryInfo
import com.tencent.devops.stream.trigger.git.pojo.tgit.TGitCred
import com.tencent.devops.stream.trigger.git.service.TGitApiService
import com.tencent.devops.stream.trigger.parsers.PipelineDelete
import com.tencent.devops.stream.trigger.parsers.triggerMatch.TriggerMatcher
import com.tencent.devops.stream.trigger.parsers.triggerMatch.TriggerResult
import com.tencent.devops.stream.trigger.parsers.triggerMatch.matchUtils.PathMatchUtils
import com.tencent.devops.stream.trigger.parsers.triggerParameter.GitRequestEventHandle
import com.tencent.devops.stream.trigger.pojo.CheckType
import com.tencent.devops.stream.trigger.pojo.YamlPathListEntry
import com.tencent.devops.stream.trigger.pojo.enums.StreamCommitCheckState
import com.tencent.devops.stream.trigger.service.DeleteEventService
import com.tencent.devops.stream.trigger.service.GitCheckService
import com.tencent.devops.stream.trigger.service.StreamEventService
import com.tencent.devops.stream.trigger.timer.service.StreamTimerService
import com.tencent.devops.stream.util.CommonCredentialUtils
import com.tencent.devops.stream.util.StreamCommonUtils
import com.tencent.devops.ticket.pojo.enums.CredentialType
import org.jooq.DSLContext
import org.slf4j.LoggerFactory

class TGitPushActionGit(
    private val dslContext: DSLContext,
    private val client: Client,
    private val apiService: TGitApiService,
    private val streamEventService: StreamEventService,
    private val streamTimerService: StreamTimerService,
    private val streamPipelineBranchService: StreamPipelineBranchService,
    private val streamDeleteEventService: DeleteEventService,
    private val gitPipelineResourceDao: GitPipelineResourceDao,
    private val pipelineDelete: PipelineDelete,
    private val gitCheckService: GitCheckService
) : TGitActionGit(apiService, gitCheckService), GitBaseAction {

    companion object {
        val logger = LoggerFactory.getLogger(TGitPushActionGit::class.java)
        val SKIP_CI_KEYS = setOf("skip ci", "ci skip", "no ci", "ci.skip")
        private const val PUSH_OPTIONS_PREFIX = "ci.variable::"
    }

    override val metaData: ActionMetaData = ActionMetaData(streamObjectKind = StreamObjectKind.PUSH)

    override lateinit var data: ActionData
    fun data() = data as TGitPushActionData

    override val api: TGitApiService
        get() = apiService

    override fun init(requestEventId: Long) {
        this.data.context.requestEventId = requestEventId
        initCommonData()
    }

    private fun initCommonData(): GitBaseAction {
        this.data.eventCommon = TGitPushEventCommonData(data().event)
        return this
    }

    override fun isStreamDeleteAction() = data().event.isDeleteEvent()

    override fun buildRequestEvent(eventStr: String): GitRequestEvent? {
        val rData = data()
        if (!rData.event.pushEventFilter()) {
            return null
        }
        return GitRequestEventHandle.createPushEvent(rData.event, eventStr)
    }

    override fun skipStream(): Boolean {
        if (!data().event.skipStream()) {
            return false
        }
        logger.info("project: ${data().eventCommon.gitProjectId} commit: ${data().eventCommon.commit.commitId} skip ci")
        streamEventService.saveTriggerNotBuildEvent(
            action = this,
            reason = TriggerReason.USER_SKIPED.name,
            reasonDetail = TriggerReason.USER_SKIPED.detail
        )
        return true
    }

    override fun checkProjectConfig() {
        if (!data().setting.buildPushedBranches) {
            throw StreamTriggerException(this, TriggerReason.BUILD_PUSHED_BRANCHES_DISABLED)
        }
    }

    override fun checkMrConflict(path2PipelineExists: Map<String, StreamTriggerPipeline>): Boolean {
        return true
    }

    override fun checkAndDeletePipeline(path2PipelineExists: Map<String, StreamTriggerPipeline>) {
        // 直接删除分支,挪到前面，不需要对deleteYamlFiles获取后再做判断。
        if (data().event.isDeleteBranch()) {
            val pipelines = streamPipelineBranchService.getBranchPipelines(
                this.data.getGitProjectId().toLong(),
                this.data.eventCommon.branch
            )
            pipelines.forEach {
                // 这里需增加获取pipeline对应的yml路径，需要判断该文件是否在默认分支存在。
                val gitPipelineResourceRecord = gitPipelineResourceDao.getPipelineById(
                    dslContext = dslContext,
                    gitProjectId = this.data.getGitProjectId().toLong(),
                    pipelineId = it
                )
                pipelineDelete.delete(
                    action = this,
                    gitProjectId = it,
                    pipelineId = gitPipelineResourceRecord?.gitProjectId.toString(),
                    filePath = gitPipelineResourceRecord?.filePath
                )
            }
            return
        }

        val deleteYamlFiles = data().event.commits?.flatMap {
            if (it.removed != null) {
                it.removed!!.asIterable()
            } else {
                emptyList()
            }
        }?.filter { StreamCommonUtils.isCiFile(it) }
        pipelineDelete.checkAndDeletePipeline(this, path2PipelineExists, deleteYamlFiles)
    }

    override fun getYamlPathList(): List<YamlPathListEntry> {
        return TGitActionCommon.getYamlPathList(
            action = this,
            gitProjectId = this.data().getGitProjectId(),
            ref = this.data.eventCommon.branch
        ).map { YamlPathListEntry(it, CheckType.NO_NEED_CHECK) }
    }

    override fun getYamlContent(fileName: String): String {
        return api.getFileContent(
            cred = this.getGitCred(),
            gitProjectId = data.getGitProjectId(),
            fileName = fileName,
            ref = data.eventCommon.branch,
            retry = ApiRequestRetryInfo(true)
        )
    }

    override fun isMatch(triggerOn: TriggerOn): TriggerResult {
        val branch = TGitActionCommon.getTriggerBranch(data().eventCommon.branch)

        val isDefaultBranch = branch == data().context.defaultBranch
        // 校验是否注册跨项目触发
        val repoTriggerUserId = if (isDefaultBranch) {
            checkRepoTriggerCredentials(this, triggerOn)
        } else {
            null
        }

        // 判断是否注册定时任务
        val isTime = if (isDefaultBranch) {
            isSchedulesMatch(
                triggerOn = triggerOn,
                eventBranch = data.eventCommon.branch,
                userId = data.eventCommon.userId,
                pipelineId = data.context.pipeline!!.pipelineId
            )
        } else {
            false
        }

        // 判断是否注册删除任务
        val changeSet = getCommitChangeSet(data().event)
        data.context.changeSet = changeSet.toList()
        val isDelete = if (isDefaultBranch) {
            // 只有更改了delete相关流水线才做更新
            PathMatchUtils.isIncludePathMatch(listOf(data.context.pipeline!!.filePath), changeSet) &&
                isDeleteMatch(triggerOn.delete, data.context.pipeline!!.pipelineId)
        } else {
            false
        }

        val isMatch = TriggerMatcher.isPushMatch(
            triggerOn = triggerOn,
            eventBranch = data.eventCommon.branch,
            changeSet = changeSet,
            userId = data.eventCommon.userId,
            isCreateBranch = data().event.isCreateBranch()
        )
        val params = TGitActionCommon.getStartParams(
            action = this,
            triggerOn = triggerOn,
            userId = repoTriggerUserId
        )
        return TriggerResult(
            trigger = isMatch,
            startParams = params,
            timeTrigger = isTime,
            deleteTrigger = isDelete
        )
    }

    /**
     * 判断是否可以注册跨项目构建事件
     * @return 用户名称
     */
    private fun checkRepoTriggerCredentials(action: BaseAction, triggerOn: TriggerOn): String? {
        if (triggerOn.repoHook == null) {
            return null
        }
        val (repoTriggerCredentialsCheck, repoTriggerUserId) = try {
            checkRepoTriggerCredentials(action, triggerOn.repoHook!!)
        } catch (e: Exception) {
            throw StreamTriggerException(
                action = this,
                triggerReason = TriggerReason.REPO_TRIGGER_FAILED,
                reasonParams = listOf("请检查远程仓库(${triggerOn.repoHook?.name})鉴权信息是否正确,${e.message}"),
                commitCheck = CommitCheck(
                    block = false,
                    state = StreamCommitCheckState.FAILURE
                )
            )
        }
        if (!repoTriggerCredentialsCheck) {
            throw StreamTriggerException(
                action = this,
                triggerReason = TriggerReason.REPO_TRIGGER_FAILED,
                reasonParams = listOf("请检查远程仓库(${triggerOn.repoHook?.name})使用的权限是否大于等于MASTER"),
                commitCheck = CommitCheck(
                    block = false,
                    state = StreamCommitCheckState.FAILURE
                )
            )
        }
        return repoTriggerUserId
    }

    private fun checkRepoTriggerCredentials(action: BaseAction, repoHook: RepositoryHook): Pair<Boolean, String?> {
        val token = when {
            repoHook.credentialsForTicketId != null -> CommonCredentialUtils.getCredential(
                client = client,
                projectId = "git_${action.data.getGitProjectId()}",
                credentialId = repoHook.credentialsForTicketId!!,
                type = CredentialType.ACCESSTOKEN
            )["v1"] ?: return Pair(false, null)
            repoHook.credentialsForToken != null -> repoHook.credentialsForToken!!
            else -> return Pair(false, null)
        }
        // stream 侧需要的是user 数字id 而不是 rtx
        val userInfo = action.api.getUserInfoByToken(
            TGitCred(
                userId = action.data.eventCommon.userId,
                accessToken = token,
                useAccessToken = false
            )
        ) ?: return Pair(false, null)
        val check = action.api.getProjectUserInfo(
            cred = TGitCred(
                userId = action.data.eventCommon.userId,
                accessToken = token,
                useAccessToken = false
            ),
            userId = userInfo.username,
            gitProjectId = action.data.eventCommon.gitProjectId
        ).accessLevel >= 40
        return Pair(check, userInfo.username)
    }

    // 判断是否注册定时任务来看是修改还是删除
    private fun isSchedulesMatch(
        triggerOn: TriggerOn,
        eventBranch: String,
        userId: String,
        pipelineId: String
    ): Boolean {
        if (triggerOn.schedules == null) {
            // 新流水线没有定时任务就没注册过定时任务
            if (pipelineId.isBlank()) {
                return false
            } else {
                // 不是新流水线的可能注册过了要删除
                streamTimerService.get(pipelineId) ?: return false
                streamTimerService.deleteTimer(pipelineId, userId)
                return false
            }
        } else {
            if (triggerOn.schedules?.cron.isNullOrBlank()) {
                logger.info("The schedules cron is invalid($eventBranch)")
                return false
            }
        }
        return true
    }

    private fun getCommitChangeSet(gitEvent: GitPushEvent): Set<String> {
        val changeSet = mutableSetOf<String>()
        if (gitEvent.operation_kind == TGitPushOperationKind.UPDATE_NONFASTFORWORD.value) {
            for (i in 1..10) {
                // 反向进行三点比较可以比较出rebase的真实提交
                val result = apiService.getCommitChangeList(
                    cred = getGitCred(),
                    gitProjectId = data.eventCommon.gitProjectId,
                    from = gitEvent.after,
                    to = gitEvent.before,
                    straight = false,
                    page = i,
                    pageSize = 100,
                    retry = ApiRequestRetryInfo(true)
                )
                changeSet.addAll(
                    result.map {
                        if (it.deletedFile) {
                            it.oldPath
                        } else if (it.renameFile) {
                            it.oldPath
                            it.newPath
                        } else {
                            it.newPath
                        }
                    }
                )
                if (result.size < 100) {
                    break
                }
            }
            return changeSet
        }

        gitEvent.commits?.forEach { commit ->
            changeSet.addAll(commit.added?.map { it }?.toSet() ?: emptySet())
            changeSet.addAll(commit.modified?.map { it }?.toSet() ?: emptySet())
            changeSet.addAll(commit.removed?.map { it }?.toSet() ?: emptySet())
        }
        return changeSet
    }

    // 判断是否注册默认分支的删除任务
    private fun isDeleteMatch(
        deleteRule: DeleteRule?,
        pipelineId: String
    ): Boolean {
        if (deleteRule == null) {
            if (pipelineId.isBlank()) {
                return false
            } else {
                streamDeleteEventService.getDeleteEvent(pipelineId) ?: return false
                streamDeleteEventService.deleteDeleteEvent(pipelineId)
                return false
            }
        } else {
            if (deleteRule.types.isEmpty() || !deleteRule.check()) {
                return false
            }
        }
        return true
    }

    override fun getUserVariables(yamlVariables: Map<String, Variable>?): Map<String, Variable>? {
        return replaceVariablesByPushOptions(yamlVariables, data().event.push_options)
    }

    // git push -o ci.variable::<name>="<value>" -o ci.variable::<name>="<value>"
    private fun replaceVariablesByPushOptions(
        variables: Map<String, Variable>?,
        pushOptions: Map<String, String>?
    ): Map<String, Variable>? {
        if (variables.isNullOrEmpty() || pushOptions.isNullOrEmpty()) {
            return variables
        }
        val variablesOptionsKeys = pushOptions.keys.filter { it.startsWith(PUSH_OPTIONS_PREFIX) }
            .map { it.removePrefix(PUSH_OPTIONS_PREFIX) }

        val result = variables.toMutableMap()
        variables.forEach { (key, value) ->
            // 不替换只读变量
            if (value.readonly != null && value.readonly == true) {
                return@forEach
            }
            if (key in variablesOptionsKeys) {
                result[key] = Variable(
                    value = pushOptions["${PUSH_OPTIONS_PREFIX}$key"],
                    readonly = value.readonly
                )
            }
        }
        return result
    }

    override fun getWebHookStartParam(triggerOn: TriggerOn): Map<String, String> {
        return TGitActionCommon.getStartParams(
            action = this,
            triggerOn = triggerOn
        )
    }

    override fun needSaveOrUpdateBranch() = true

    override fun needSendCommitCheck() = !data().event.isDeleteBranch()
}

@SuppressWarnings("ReturnCount")
private fun GitPushEvent.pushEventFilter(): Boolean {
    // 放开删除分支操作为了流水线删除功能
    if (isDeleteBranch()) {
        return true
    }
    if (total_commits_count <= 0) {
        TGitPushActionGit.logger.info("$checkout_sha Git push web hook no commit($total_commits_count)")
        return false
    }
    if (GitUtils.isPrePushBranch(ref)) {
        TGitPushActionGit.logger.info("Git web hook is pre-push event|branchName=$ref")
        return false
    }
    return true
}

private fun GitPushEvent.skipStream(): Boolean {
    // 判断commitMsg
    commits?.filter { it.id == after }?.forEach { commit ->
        TGitPushActionGit.SKIP_CI_KEYS.forEach { key ->
            if (commit.message.contains(key)) {
                return true
            }
        }
    }
    push_options?.keys?.forEach {
        if (it == "ci.skip") {
            return true
        }
    }
    return false
}
