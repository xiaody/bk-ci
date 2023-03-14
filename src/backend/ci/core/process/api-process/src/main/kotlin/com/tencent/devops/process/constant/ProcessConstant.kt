package com.tencent.devops.process.constant

const val BK_PIPELINE_SINGLE_BUILD = "bkPipelineSingleBuild"
const val BK_MUTEX_GROUP_SINGLE_BUILD = "bkMutexGroupSingleBuild"
const val BK_NON_TIMED_TRIGGER_SKIP = "bkNonTimedTriggerSkip"
const val BK_FIRST_STAGE_ENV_NOT_EMPTY = "bkFirstStageEnvNotEmpty"
const val BK_QUALITY_CHECK_FAIL = "bkQualityCheckFail"
const val BK_QUALITY_CHECK_SUCCEED = "bkQualityCheckSucceed"
const val BK_QUALITY_CHECK_INTERCEPTED = "bkQualityCheckIntercepted"
const val BK_QUALITY_TO_BE_REVIEW = "bkQualityToBeReview"
const val BK_POLLING_WAIT_FOR_QUALITY_RESULT = "bkPollingWaitForQualityResult"
const val BK_QUALITY_CHECK_RESULT = "bkQualityCheckResult"
const val BK_AUDIT_TIMEOUT = "bkAuditTimeout"
const val BK_AUDIT_RESULT = "bkAuditResult"
const val BK_JOB_MATRIX_YAML_CONFIG_ERROR = "BkJobMatrixYamlConfigError"// Job[{0]的矩阵YAML配置错误:
const val BK_PIPELINE_DELETE_NOT_EXIST = "PipelineDeleteNotExist"
const val BK_PROCESSING_CURRENT_REPORTED_TASK_PLEASE_WAIT = "BkProcessingCurrentReportedTaskPleaseWait"// 正在处理当前上报的任务, 请稍等。。。
const val BK_CODE_BASE_NOT_EXIST = "BkCodeBaseNotExist"// 代码库({0})不存在
const val BK_NOT_SVN_CODE_BASE = "BkNotSvnCodeBase"// 代码库({0})不是svn代码库
const val BK_FAIL_TO_GET_SVN_DIRECTORY = "BkFailToGetSvnDirectory"// 获取Svn目录失败, msg:{0}
const val BK_REPOSITORY_ID_AND_NAME_ARE_EMPTY = "BkRepositoryIdAndNameAreEmpty"// 仓库ID和仓库名都为空
const val BK_PULLING_LATEST_VERSION_NUMBER_EXCEPTION = "BkPullingLatestVersionNumberException"// 拉取最新版本号出现异常,重试{0}次失败
const val BK_NO_BUILD_RECORD_FOR_CORRESPONDING_SUB_PIPELINE = "NoBuildRecordForCorrespondingSubPipeline"// 找不到对应子流水线的构建记录
const val BK_NO_CORRESPONDING_SUB_PIPELINE = "BkNoCorrespondingSubPipeline"// "找不到对应子流水线"
const val BK_NO_CORRESPONDING_SUB_PIPELINE_BUILD_RECORDS= "BkNoCorrespondingSubPipelineBuildRecords"// 找不到对应子流水线的构建记录
const val BK_SUB_PIPELINE_ID_PARAMETER_IS_EMPTY = "BkSubPipelineIdParameterIsEmpty"// 子流水线ID参数为空，请检查流水线重新保存后并重新执行
const val BK_SUB_PIPELINE_NOT_EXIST = "BkSubPipelineNotExist"//子流水线[{0}]不存在,请检查流水线是否还存在
const val BK_BACKGROUND_SERVICE_TASK_EXECUTION_ERROR = "BkBackgroundServiceTaskExecutionError"//后台服务任务执行出错
const val BK_BACKGROUND_SERVICE_RUNNING_ERROR = "BkBackgroundServiceRunningError"// 后台服务运行出错
const val BK_ENV_NOT_YET_SUPPORTED = "BkEnvNotYetSupported"// 尚未支持 {0} {1}，请联系 DevOps-helper 添加对应版本
const val BK_VIEW_ID_AND_NAME_CANNOT_BE_EMPTY_TOGETHER = "BkViewIdAndNameCannotBeEmptyTogether"// <viewId>和<viewName>不能同时为空, 填<viewName>时需同时填写参数<isProject>
const val BK_VIEW_NOT_FOUND_IN_PROJECT = "BkViewNotFoundInProject"// 在项目 {0} 下未找到{1}视图{2}
const val BK_TRIGGERED_BY_GIT_EVENT_PLUGIN = "BkTriggeredByGitEventPlugin"// 因【Git事件触发】插件中，MR Request Hook勾选了【MR为同源同目标分支时，等待队列只保留最新触发的任务】配置，该次构建已被新触发的构建
const val BK_BUILD_IN_REVIEW_STATUS = "BkBuildInReviewStatus"// 项目【{0}】下的流水线【{1}】#{2} 构建处于待审核状态
const val BK_CHECK_THE_WEB_DATA = "BkCheckTheWebData"// 查web端数据查web端数据:
const val BK_CHECK_FILE_COUNT_AND_VERSION = "BkCheckFileCountAndVersion"// 查文件个数、版本:
const val BK_QUERY_PIPELINE_INFO = "BkQueryPipelineInfo"// 查流水线信息:
const val BK_QUERY_FAVORITE_PIPELINE = "BkQueryFavoritePipeline"// 查询收藏的流水线:
const val BK_PROJECT_NO_PIPELINE = "BkProjectNoPipeline"// 项目下无流水线
const val BK_NO_MATCHING_STARTED_PIPELINE = "BkNoMatchingStartedPipeline"//未匹配到启用流水线
const val BK_USER_NO_PERMISSION_GET_PIPELINE_INFO = "BkUserNoPermissionGetPipelineInfo"// 用户（{0}) 无权限获取流水线({1})信息({2})
const val BK_USER_NO_PIPELINE_EXECUTE_PERMISSIONS = "BkUserNoPipelineExecutePermissions"// 用户（{0}) 没有流水线({1})的执行权限
const val BK_SUB_PIPELINE_PARAM_FILTER_FAILED = "BkSubPipelineParamFilterFailed"// 子流水线参数过滤失败
