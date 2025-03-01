# 数据库设计文档

**数据库名：** devops_ci_repository

**文档版本：** 1.0.0

**文档描述：** devops_ci_repository的数据库文档

| 表名                  | 说明       |
| :---: | :---: |
| [T_REPOSITORY](#T_REPOSITORY) | 代码库表 |
| [T_REPOSITORY_CODE_GIT](#T_REPOSITORY_CODE_GIT) | 工蜂代码库明细表 |
| [T_REPOSITORY_CODE_GITLAB](#T_REPOSITORY_CODE_GITLAB) | gitlab代码库明细表 |
| [T_REPOSITORY_CODE_SVN](#T_REPOSITORY_CODE_SVN) | svn代码库明细表 |
| [T_REPOSITORY_COMMIT](#T_REPOSITORY_COMMIT) | 代码库变更记录 |
| [T_REPOSITORY_GITHUB](#T_REPOSITORY_GITHUB) | github代码库明细表 |
| [T_REPOSITORY_GITHUB_TOKEN](#T_REPOSITORY_GITHUB_TOKEN) | githuboauthtoken表 |
| [T_REPOSITORY_GIT_CHECK](#T_REPOSITORY_GIT_CHECK) | 工蜂oauthtoken表 |
| [T_REPOSITORY_GIT_TOKEN](#T_REPOSITORY_GIT_TOKEN) | 工蜂commitchecker表 |

**表名：** <a id="T_REPOSITORY">T_REPOSITORY</a>

**说明：** 代码库表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | REPOSITORY_ID |   bigint   | 20 |   0    |    N     |  Y   |       | 主键ID  |
|  2   | PROJECT_ID |   varchar   | 32 |   0    |    N     |  N   |       | 项目ID  |
|  3   | USER_ID |   varchar   | 64 |   0    |    N     |  N   |       | 用户ID  |
|  4   | ALIAS_NAME |   varchar   | 255 |   0    |    N     |  N   |       | 别名  |
|  5   | URL |   varchar   | 255 |   0    |    N     |  N   |       | url地址  |
|  6   | TYPE |   varchar   | 20 |   0    |    N     |  N   |       | 类型  |
|  7   | CREATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 创建时间  |
|  8   | UPDATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 修改时间  |
|  9   | IS_DELETED |   bit   | 1 |   0    |    N     |  N   |       | 是否删除0可用1删除  |

**表名：** <a id="T_REPOSITORY_CODE_GIT">T_REPOSITORY_CODE_GIT</a>

**说明：** 工蜂代码库明细表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | REPOSITORY_ID |   bigint   | 20 |   0    |    N     |  Y   |       | 仓库ID  |
|  2   | PROJECT_NAME |   varchar   | 255 |   0    |    N     |  N   |       | 项目名称  |
|  3   | USER_NAME |   varchar   | 64 |   0    |    N     |  N   |       | 用户名称  |
|  4   | CREATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 创建时间  |
|  5   | UPDATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 修改时间  |
|  6   | CREDENTIAL_ID |   varchar   | 64 |   0    |    N     |  N   |       | 凭据ID  |
|  7   | AUTH_TYPE |   varchar   | 8 |   0    |    Y     |  N   |       | 认证方式  |

**表名：** <a id="T_REPOSITORY_CODE_GITLAB">T_REPOSITORY_CODE_GITLAB</a>

**说明：** gitlab代码库明细表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | REPOSITORY_ID |   bigint   | 20 |   0    |    N     |  Y   |       | 仓库ID  |
|  2   | PROJECT_NAME |   varchar   | 255 |   0    |    N     |  N   |       | 项目名称  |
|  3   | CREDENTIAL_ID |   varchar   | 64 |   0    |    N     |  N   |       | 凭据ID  |
|  4   | CREATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 创建时间  |
|  5   | UPDATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 修改时间  |
|  6   | USER_NAME |   varchar   | 64 |   0    |    N     |  N   |       | 用户名称  |

**表名：** <a id="T_REPOSITORY_CODE_SVN">T_REPOSITORY_CODE_SVN</a>

**说明：** svn代码库明细表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | REPOSITORY_ID |   bigint   | 20 |   0    |    N     |  Y   |       | 仓库ID  |
|  2   | REGION |   varchar   | 255 |   0    |    N     |  N   |       | 地区  |
|  3   | PROJECT_NAME |   varchar   | 255 |   0    |    N     |  N   |       | 项目名称  |
|  4   | USER_NAME |   varchar   | 64 |   0    |    N     |  N   |       | 用户名称  |
|  5   | CREATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 创建时间  |
|  6   | UPDATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   2019-08-0100:00:00    | 修改时间  |
|  7   | CREDENTIAL_ID |   varchar   | 64 |   0    |    N     |  N   |       | 凭据ID  |
|  8   | SVN_TYPE |   varchar   | 32 |   0    |    Y     |  N   |       | 仓库类型  |

**表名：** <a id="T_REPOSITORY_COMMIT">T_REPOSITORY_COMMIT</a>

**说明：** 代码库变更记录

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | ID |   bigint   | 20 |   0    |    N     |  Y   |       | 主键ID  |
|  2   | BUILD_ID |   varchar   | 34 |   0    |    Y     |  N   |       | 构建ID  |
|  3   | PIPELINE_ID |   varchar   | 34 |   0    |    Y     |  N   |       | 流水线ID  |
|  4   | REPO_ID |   bigint   | 20 |   0    |    Y     |  N   |       | 代码库ID  |
|  5   | TYPE |   smallint   | 6 |   0    |    Y     |  N   |       | 1-svn,2-git,3-gitlab  |
|  6   | COMMIT |   varchar   | 64 |   0    |    Y     |  N   |       | 提交  |
|  7   | COMMITTER |   varchar   | 32 |   0    |    Y     |  N   |       | 提交者  |
|  8   | COMMIT_TIME |   datetime   | 19 |   0    |    Y     |  N   |       | 提交时间  |
|  9   | COMMENT |   longtext   | 2147483647 |   0    |    Y     |  N   |       | 评论  |
|  10   | ELEMENT_ID |   varchar   | 34 |   0    |    Y     |  N   |       | 原子ID  |
|  11   | REPO_NAME |   varchar   | 128 |   0    |    Y     |  N   |       | 代码库别名  |

**表名：** <a id="T_REPOSITORY_GITHUB">T_REPOSITORY_GITHUB</a>

**说明：** github代码库明细表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | REPOSITORY_ID |   bigint   | 20 |   0    |    N     |  Y   |       | 仓库ID  |
|  2   | CREDENTIAL_ID |   varchar   | 128 |   0    |    Y     |  N   |       | 凭据ID  |
|  3   | PROJECT_NAME |   varchar   | 255 |   0    |    N     |  N   |       | 项目名称  |
|  4   | USER_NAME |   varchar   | 64 |   0    |    N     |  N   |       | 用户名称  |
|  5   | CREATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   CURRENT_TIMESTAMP    | 创建时间  |
|  6   | UPDATED_TIME |   timestamp   | 19 |   0    |    N     |  N   |   CURRENT_TIMESTAMP    | 修改时间  |

**表名：** <a id="T_REPOSITORY_GITHUB_TOKEN">T_REPOSITORY_GITHUB_TOKEN</a>

**说明：** githuboauthtoken表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | ID |   bigint   | 20 |   0    |    N     |  Y   |       | 主键ID  |
|  2   | USER_ID |   varchar   | 64 |   0    |    N     |  N   |       | 用户ID  |
|  3   | ACCESS_TOKEN |   varchar   | 96 |   0    |    N     |  N   |       | 权限Token  |
|  4   | TOKEN_TYPE |   varchar   | 64 |   0    |    N     |  N   |       | token类型  |
|  5   | SCOPE |   text   | 65535 |   0    |    N     |  N   |       | 生效范围  |
|  6   | CREATE_TIME |   datetime   | 19 |   0    |    N     |  N   |       | 创建时间  |
|  7   | UPDATE_TIME |   datetime   | 19 |   0    |    N     |  N   |       | 更新时间  |

**表名：** <a id="T_REPOSITORY_GIT_CHECK">T_REPOSITORY_GIT_CHECK</a>

**说明：** 工蜂oauthtoken表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | ID |   bigint   | 20 |   0    |    N     |  Y   |       | 主键ID  |
|  2   | PIPELINE_ID |   varchar   | 64 |   0    |    N     |  N   |       | 流水线ID  |
|  3   | BUILD_NUMBER |   int   | 10 |   0    |    N     |  N   |       | 构建编号  |
|  4   | REPO_ID |   varchar   | 64 |   0    |    Y     |  N   |       | 代码库ID  |
|  5   | COMMIT_ID |   varchar   | 64 |   0    |    N     |  N   |       | 代码提交ID  |
|  6   | CREATE_TIME |   datetime   | 19 |   0    |    N     |  N   |       | 创建时间  |
|  7   | UPDATE_TIME |   datetime   | 19 |   0    |    N     |  N   |       | 更新时间  |
|  8   | REPO_NAME |   varchar   | 128 |   0    |    Y     |  N   |       | 代码库别名  |
|  9   | CONTEXT |   varchar   | 255 |   0    |    Y     |  N   |       | 内容  |
|  10   | SOURCE |   varchar   | 64 |   0    |    N     |  N   |       | 事件来源  |

**表名：** <a id="T_REPOSITORY_GIT_TOKEN">T_REPOSITORY_GIT_TOKEN</a>

**说明：** 工蜂commitchecker表

**数据列：**

| 序号 | 名称 | 数据类型 |  长度  | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
|  1   | ID |   bigint   | 20 |   0    |    N     |  Y   |       | 主键ID  |
|  2   | USER_ID |   varchar   | 64 |   0    |    Y     |  N   |       | 用户ID  |
|  3   | ACCESS_TOKEN |   varchar   | 96 |   0    |    Y     |  N   |       | 权限Token  |
|  4   | REFRESH_TOKEN |   varchar   | 96 |   0    |    Y     |  N   |       | 刷新token  |
|  5   | TOKEN_TYPE |   varchar   | 64 |   0    |    Y     |  N   |       | token类型  |
|  6   | EXPIRES_IN |   bigint   | 20 |   0    |    Y     |  N   |       | 过期时间  |
|  7   | CREATE_TIME |   datetime   | 19 |   0    |    Y     |  N   |   CURRENT_TIMESTAMP    | token的创建时间  |
