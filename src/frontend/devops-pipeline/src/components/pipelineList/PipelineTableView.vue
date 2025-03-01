<template>
    <bk-table
        ext-cls="pipeline-list-table"
        v-bkloading="{ isLoading }"
        ref="pipelineTable"
        row-key="pipelineId"
        height="100%"
        :data="pipelineList"
        :size="tableSize"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-limit-change="handlePageLimitChange"
        :row-class-name="setRowCls"
        @sort-change="handleSort"
        :default-sort="sortField"
        @selection-change="handleSelectChange"
        @header-dragend="handelHeaderDragend"
        @row-mouse-enter="handleRowMouseEnter"
        @row-mouse-leave="handleRowMouseLeave"
        :row-style="{ height: '56px' }"
        v-on="$listeners"
    >
        <PipelineListEmpty slot="empty" :is-patch="isPatchView"></PipelineListEmpty>
        <div v-if="selectionLength > 0" slot="prepend" class="selected-all-indicator">
            <span v-html="$t('selectedCount', [selectionLength])"></span>
            <bk-button theme="primary" text @click="clearSelection">
                {{$t('clearSelection')}}
            </bk-button>
        </div>
        <bk-table-column v-if="isPatchView" type="selection" width="60" fixed="left" :selectable="checkSelecteable"></bk-table-column>
        <bk-table-column v-if="!isPatchView && !isDeleteView" width="20" fixed="left">
            <template slot-scope="{ row, $index }">
                <bk-button
                    v-show="showCollectIndex === $index || row.hasCollect"
                    text
                    class="icon-star-btn"
                    :theme="row.hasCollect ? 'warning' : ''"
                    @click="collectHandler(row)">
                    <i :class="{
                        'devops-icon': true,
                        'icon-star': !row.hasCollect,
                        'icon-star-shape': row.hasCollect
                    }" />
                </bk-button>
            </template>
        </bk-table-column>
        <bk-table-column v-if="allRenderColumnMap.pipelineName" :width="tableWidthMap.pipelineName" min-width="250" fixed="left" sortable="custom" :label="$t('pipelineName')" prop="pipelineName">
            <template slot-scope="props">
                <!-- hack disabled event -->
                <span
                    v-if="!props.row.delete && !props.row.hasPermission && !isDeleteView"
                    class="pointer"
                    @click="applyPermission(props.row)"
                >
                    {{props.row.pipelineName}}
                </span>
                <router-link
                    v-else-if="!props.row.delete && !isDeleteView && props.row.historyRoute"
                    class="pipeline-cell-link"
                    :disabled="!props.row.hasPermission"
                    :to="props.row.historyRoute">
                    {{props.row.pipelineName}}
                </router-link>
                <span v-else>{{props.row.pipelineName}}</span>
            </template>
        </bk-table-column>
        <bk-table-column v-if="allRenderColumnMap.ownGroupName && (isAllPipelineView || isPatchView || isDeleteView)" :width="tableWidthMap.viewNames" min-width="300" :label="$t('ownGroupName')" prop="viewNames">
            <div :ref="`belongsGroupBox_${props.$index}`" class="pipeline-group-box-cell" slot-scope="props">
                <template v-if="pipelineGroups[props.$index].visibleGroups">
                    <bk-tag
                        ext-cls="pipeline-group-name-tag"
                        :ref="`groupName_${props.$index}`"
                        v-for="(viewName, index) in pipelineGroups[props.$index].visibleGroups"
                        :key="index"
                        v-bk-tooltips="{ content: viewName, delay: [300, 0], allowHTML: false }"
                        @click="goGroup(viewName)"
                    >
                        {{viewName}}
                    </bk-tag>
                    <bk-popover
                        theme="light"
                        placement="bottom-end"
                        max-width="250"
                        v-if="pipelineGroups[props.$index].showMore"
                    >
                        <bk-tag
                            :ref="`groupNameMore_${props.$index}`"
                        >
                            +{{ pipelineGroups[props.$index].showMore }}
                        </bk-tag>
                        <div slot="content">
                            <bk-tag
                                v-for="hiddenGroup in pipelineGroups[props.$index].hiddenGroups"
                                ext-cls="pipeline-group-name-tag"
                                :key="hiddenGroup"
                                v-bk-tooltips="{ content: hiddenGroup, delay: [300, 0], allowHTML: false }"
                                @click="goGroup(hiddenGroup)"
                            >
                                {{hiddenGroup}}
                            </bk-tag>
                        </div>
                    </bk-popover>
                </template>
            </div>
        </bk-table-column>
        <template v-if="isPatchView">
            <bk-table-column :width="tableWidthMap.latestBuildNum" :label="$t('latestExec')" prop="latestBuildNum">
                <span slot-scope="props">{{ props.row.latestBuildNum ? `#${props.row.latestBuildNum}` : '--' }}</span>
            </bk-table-column>
            <bk-table-column :width="tableWidthMap.latestBuildStartDate" sortable="custom" :label="$t('lastExecTime')" prop="latestBuildStartDate" />
            <bk-table-column :width="tableWidthMap.createTime" sortable="custom" :label="$t('restore.createTime')" prop="createTime" :formatter="formatTime" />
            <bk-table-column :width="tableWidthMap.creator" :label="$t('creator')" prop="creator" />
        </template>
        <template v-else-if="isDeleteView">
            <bk-table-column :width="tableWidthMap.createTime" key="createTime" :label="$t('restore.createTime')" sortable="custom" prop="createTime" sort :formatter="formatTime" />
            <bk-table-column :width="tableWidthMap.deleteTime" key="updateTime" :label="$t('restore.deleteTime')" sortable="custom" prop="updateTime" :formatter="formatTime" />
            <bk-table-column :width="tableWidthMap.lastModifyUser" key="lastModifyUser" :label="$t('restore.deleter')" prop="lastModifyUser"></bk-table-column>
        </template>
        <template v-else>
            <bk-table-column v-if="allRenderColumnMap.latestExec" :width="tableWidthMap.latestExec" min-width="180" :label="$t('latestExec')" prop="latestExec">
                <span v-if="props.row.delete" slot-scope="props">
                    {{$t('deleteAlready')}}
                </span>
                <div v-else slot-scope="props" class="pipeline-latest-exec-cell">
                    <pipeline-status-icon :status="props.row.latestBuildStatus" />
                    <div class="pipeline-exec-msg">
                        <template v-if="props.row.latestBuildNum">
                            <router-link
                                class="pipeline-cell-link pipeline-exec-msg-title"
                                :disabled="!props.row.hasPermission"
                                :event="props.row.hasPermission ? 'click' : ''"
                                :to="props.row.latestBuildRoute"
                            >
                                <b>#{{ props.row.latestBuildNum }}</b>
                                |
                                <span>{{ props.row.lastBuildMsg }}</span>
                            </router-link>
                            <p class="pipeline-exec-msg-desc">
                                <span class="desc">
                                    <logo :name="props.row.trigger" size="16" />
                                    <span>{{ props.row.latestBuildUserId }}</span>
                                </span>
                                <span v-if="props.row.webhookAliasName" class="desc">
                                    <logo name="branch" size="16" />
                                    <span>{{ props.row.webhookAliasName }}</span>
                                </span>
                                <span v-if="props.row.webhookMessage" class="desc">
                                    <span>{{ props.row.webhookMessage }}</span>
                                </span>
                            </p>
                        </template>
                        <p v-else class="desc">{{$t('unexecute')}}</p>
                    </div>
                </div>
            </bk-table-column>
            <bk-table-column v-if="allRenderColumnMap.lastExecTime" :width="tableWidthMap.latestBuildStartDate" sortable="custom" :label="$t('lastExecTime')" prop="latestBuildStartDate">
                <div class="latest-build-multiple-row" v-if="!props.row.delete" slot-scope="props">
                    <p>{{ props.row.latestBuildStartDate }}</p>
                    <p v-if="props.row.progress" class="primary">{{ props.row.progress }}</p>
                    <p v-else class="desc">{{props.row.duration}}</p>
                </div>
            </bk-table-column>
            <bk-table-column v-if="allRenderColumnMap.lastModify" :width="tableWidthMap.updateTime" :label="$t('lastModify')" sortable="custom" prop="updateTime" sort>
                <div class="latest-build-multiple-row" v-if="!props.row.delete" slot-scope="props">
                    <p>{{ props.row.updater }}</p>
                    <p class="desc">{{props.row.updateDate}}</p>
                </div>
            </bk-table-column>
            <bk-table-column v-if="allRenderColumnMap.creator" :width="tableWidthMap.creator" :label="$t('creator')" prop="creator" />
            <bk-table-column v-if="allRenderColumnMap.created" :width="tableWidthMap.created" :label="$t('created')" prop="createTime">
                <template slot-scope="props">
                    {{ prettyDateTimeFormat(props.row.createTime) }}
                </template>
            </bk-table-column>
        </template>
        <bk-table-column v-if="!isPatchView" :width="tableWidthMap.pipelineId" fixed="right" :label="$t('operate')" prop="pipelineId">
            <div class="pipeline-operation-cell" slot-scope="props">
                <bk-button
                    v-if="isDeleteView"
                    text
                    theme="primary"
                    @click="handleRestore(props.row)">
                    {{ $t('restore.restore') }}
                </bk-button>
                <bk-button
                    v-else-if="props.row.delete && !isRecentView"
                    text
                    theme="primary"
                    :disabled="!isManage"
                    @click="removeHandler(props.row)"
                >
                    {{ $t('removeFromGroup') }}
                </bk-button>
                <bk-button
                    v-else-if="!props.row.hasPermission && !props.row.delete"
                    outline
                    theme="primary"
                    @click="applyPermission(props.row)">
                    {{ $t('apply') }}
                </bk-button>
                <template
                    v-else-if="props.row.hasPermission && !props.row.delete"
                >
                    <span v-bk-tooltips="props.row.tooltips">
                        <bk-button
                            text
                            theme="primary"
                            class="exec-pipeline-btn"
                            :disabled="props.row.disabled || props.row.lock"
                            
                            @click="execPipeline(props.row)"
                        >
                            {{ props.row.lock ? $t('disabled') : props.row.canManualStartup ? $t('exec') : $t('nonManual') }}
                        </bk-button>
                    </span>
                    <ext-menu :data="props.row" :config="props.row.pipelineActions"></ext-menu>
                </template>
            </div>
        </bk-table-column>
        <bk-table-column
            v-if="!isPatchView && !isDeleteView"
            type="setting">
            <bk-table-setting-content
                :fields="tableColumn"
                :selected="selectedTableColumn"
                :size="tableSize"
                @setting-change="handleSettingChange"
            />
        </bk-table-column>
    </bk-table>
</template>

<script>
    import Logo from '@/components/Logo'
    import PipelineStatusIcon from '@/components/PipelineStatusIcon'
    import PipelineListEmpty from '@/components/pipelineList/PipelineListEmpty'
    import ExtMenu from '@/components/pipelineList/extMenu'
    import piplineActionMixin from '@/mixins/pipeline-action-mixin'
    import {
        ALL_PIPELINE_VIEW_ID,
        DELETED_VIEW_ID,
        RECENT_USED_VIEW_ID,
        CACHE_PIPELINE_TABLE_WIDTH_MAP,
        TABLE_COLUMN_CACHE
    } from '@/store/constants'
    import { ORDER_ENUM, PIPELINE_SORT_FILED } from '@/utils/pipelineConst'
    import { convertTime, isShallowEqual, prettyDateTimeFormat } from '@/utils/util'
    import { mapGetters, mapState } from 'vuex'

    export default {
        components: {
            Logo,
            ExtMenu,
            PipelineStatusIcon,
            PipelineListEmpty
        },
        mixins: [piplineActionMixin],
        props: {
            isPatchView: Boolean,
            filterParams: {
                type: Object,
                default: () => ({})
            }
        },
        data () {
            return {
                isLoading: false,
                pipelineList: [],
                selectionLength: 0,
                pagination: {
                    current: parseInt(this.$route.query.page ?? 1),
                    limit: parseInt(this.$route.query.pageSize ?? 50),
                    count: 0
                },
                visibleTagCountList: {},
                tableWidthMap: {},
                tableSize: 'small',
                tableColumn: [],
                selectedTableColumn: [],
                showCollectIndex: -1
            }
        },
        computed: {
            ...mapGetters('pipelines', [
                'groupNamesMap'
            ]),
            ...mapState('pipelines', [
                'isManage'
            ]),
            isAllPipelineView () {
                return this.$route.params.viewId === ALL_PIPELINE_VIEW_ID
            },
            maxheight () {
                return this.$refs?.pipelineTable?.$el?.parent?.clientHeight
            },
            isDeleteView () {
                return this.$route.params.viewId === DELETED_VIEW_ID
            },
            isRecentView () {
                return this.$route.params.viewId === RECENT_USED_VIEW_ID
            },
            pipelineGroups () {
                const res = this.pipelineList.map((pipeline, index) => {
                    const { viewNames } = pipeline
                    const visibleCount = this.visibleTagCountList[index]

                    if (visibleCount >= 1) {
                        return {
                            visibleGroups: viewNames.slice(0, visibleCount),
                            hiddenGroups: viewNames.slice(visibleCount),
                            showMore: viewNames.length - visibleCount
                        }
                    }

                    return {
                        visibleGroups: viewNames,
                        hiddenGroups: [],
                        showMore: viewNames?.length ?? 0
                    }
                })
                return res
            },
            sortField () {
                const { sortType, collation } = this.$route.query
                const prop = sortType ?? localStorage.getItem('pipelineSortType') ?? PIPELINE_SORT_FILED.createTime
                const order = collation ?? localStorage.getItem('pipelineSortCollation') ?? ORDER_ENUM.descending
                return {
                    prop: this.getkeyByValue(PIPELINE_SORT_FILED, prop),
                    order: this.getkeyByValue(ORDER_ENUM, order)
                }
            },
            allRenderColumnMap () {
                return this.selectedTableColumn.reduce((result, item) => {
                    result[item.id] = true
                    return result
                }, {})
            }
        },

        watch: {
            '$route.params.viewId': function (viewId) {
                this.$refs?.pipelineTable?.clearSort?.()
                this.requestList({
                    viewId,
                    page: 1
                })
            },
            sortField: function (newSortField, oldSortField) {
                if (!isShallowEqual(newSortField, oldSortField)) {
                    this.$refs?.pipelineTable?.clearSort?.()
                    this.$refs?.pipelineTable?.sort?.(newSortField.prop, newSortField.order)
                    this.$nextTick(this.requestList)
                }
            },
            filterParams: function (filterMap, oldFilterMap) {
                if (!isShallowEqual(filterMap, oldFilterMap)) {
                    this.requestList({
                        ...filterMap,
                        page: 1
                    })
                }
            },
            isAllPipelineView (val) {
                this.setTableColumn(val)
            },
            isPatchView (val) {
                this.setTableColumn(val)
            },
            isDeleteView (val) {
                this.setTableColumn(val)
            }
        },
        mounted () {
            this.tableColumn = [
                {
                    id: 'pipelineName',
                    label: this.$t('pipelineName'),
                    disabled: true
                },
                {
                    id: 'ownGroupName',
                    label: this.$t('ownGroupName')
                },
                {
                    id: 'latestExec',
                    label: this.$t('latestExec')
                },
                {
                    id: 'lastExecTime',
                    label: this.$t('lastExecTime')
                },
                {
                    id: 'lastModify',
                    label: this.$t('lastModify')
                },
                {
                    id: 'creator',
                    label: this.$t('creator')
                },
                {
                    id: 'created',
                    label: this.$t('created')
                },
                {
                    id: 'operate',
                    label: this.$t('operate'),
                    disabled: true
                }
            ]
            const columnsCache = JSON.parse(localStorage.getItem(TABLE_COLUMN_CACHE))
            if (columnsCache) {
                this.selectedTableColumn = columnsCache.columns
                this.tableSize = columnsCache.size
            } else {
                this.selectedTableColumn = [
                    { id: 'pipelineName' },
                    { id: 'ownGroupName' },
                    { id: 'latestExec' },
                    { id: 'lastExecTime' },
                    { id: 'lastModify' },
                    { id: 'creator' },
                    { id: 'created' },
                    { id: 'operate' }
                ]
            }

            if (!(this.isAllPipelineView)) {
                this.tableColumn.splice(1, 1)
            }
            this.tableWidthMap = JSON.parse(localStorage.getItem(CACHE_PIPELINE_TABLE_WIDTH_MAP)) || {
                pipelineName: 192,
                viewNames: 192,
                latestBuildNum: 150,
                latestBuildStartDate: 154,
                createTime: 154,
                deleteTime: 154,
                creator: 154,
                updateTime: 154,
                lastModifyUser: '',
                latestExec: 484,
                created: 154,
                pipelineId: 60
            }
            this.requestList()
        },
        methods: {
            prettyDateTimeFormat,
            getkeyByValue (obj, value) {
                return Object.keys(obj).find(key => obj[key] === value)
            },
            setRowCls ({ row }) {
                const clsObj = {
                    'has-delete': row.delete,
                    'no-permission': !row.hasPermission
                }
                return Object.keys(clsObj).filter(key => clsObj[key]).join(' ')
            },
            checkSelecteable (row) {
                return !row.delete
            },
            clearSelection () {
                this.$refs.pipelineTable?.clearSelection?.()
            },
            handleSelectChange (selection, ...args) {
                this.selectionLength = selection.length
                this.$emit('selection-change', selection, ...args)
            },
            goGroup (groupName) {
                const group = this.groupNamesMap[groupName]
                if (group) {
                    this.$router.push({
                        params: {
                            viewId: group?.id
                        }
                    })
                }
            },
            handlePageLimitChange (limit) {
                this.pagination.limit = limit
                this.$nextTick(this.requestList)
            },
            handlePageChange (page) {
                this.pagination.current = page
                this.$nextTick(this.requestList)
            },
            handleSort ({ prop, order }) {
                const sortType = PIPELINE_SORT_FILED[prop]
                if (sortType) {
                    const collation = prop ? ORDER_ENUM[order] : ORDER_ENUM.descending
                    localStorage.setItem('pipelineSortType', sortType)
                    localStorage.setItem('pipelineSortCollation', collation)
                    this.$router.push({
                        ...this.$route,
                        query: {
                            ...this.$route.query,
                            sortType: sortType,
                            collation
                        }
                    })
                }
            },
            async requestList (query = {}) {
                this.isLoading = true

                try {
                    const { count, page, records } = await this.getPipelines({
                        page: this.pagination.current,
                        pageSize: this.pagination.limit,
                        viewId: this.$route.params.viewId,
                        ...this.filterParams,
                        ...query
                    })
                    Object.assign(this.pagination, {
                        count,
                        current: page
                    })
                    this.pipelineList = records
                    if (this.isAllPipelineView || this.isPatchView || this.isDeleteView) {
                        this.visibleTagCountList = {}
                        setTimeout(this.calcOverPos, 100)
                    }
                } catch (e) {
                    console.error(e)
                } finally {
                    this.isLoading = false
                }
            },
            refresh () {
                this.requestList()
            },
            formatTime (row, cell, value) {
                return convertTime(value)
            },
            async handleRestore (...args) {
                const res = await this.restore(...args)
                if (res) {
                    this.refresh()
                }
            },
            calcOverPos () {
                const tagMargin = 6

                this.visibleTagCountList = this.pipelineList.reduce((acc, pipeline, index) => {
                    if (Array.isArray(pipeline.viewNames)) {
                        const groupNameBoxWidth = this.$refs[`belongsGroupBox_${index}`].clientWidth * 2
                        const groupNameLength = pipeline.viewNames.length
                        const moreTag = this.$refs?.[`groupNameMore_${index}`]?.$el
                        const moreTagWidth = (moreTag?.clientWidth ?? 0) + tagMargin
                        const viewPortWidth = groupNameBoxWidth - (groupNameLength > 1 ? moreTagWidth : 0)
                        let sumTagWidth = 0
                        let tagVisbleCount = 0

                        this.$refs[`groupName_${index}`]?.every((groupName) => {
                            sumTagWidth += groupName.$el.offsetWidth + tagMargin

                            const isOverSize = sumTagWidth > viewPortWidth
                            !isOverSize && tagVisbleCount++
                            return !isOverSize
                        })

                        acc[index] = tagVisbleCount
                    }
                    return acc
                }, {})
            },
            handelHeaderDragend (newWidth, oldWidth, column) {
                this.tableWidthMap[column.property] = newWidth
                // this.tableWidthMap.pipelineName -= 1
                localStorage.setItem(CACHE_PIPELINE_TABLE_WIDTH_MAP, JSON.stringify(this.tableWidthMap))
            },
            handleRowMouseEnter (index) {
                this.showCollectIndex = index
            },
            handleRowMouseLeave (index) {
                this.showCollectIndex = -1
            },
            setTableColumn (val) {
                if (val) {
                    this.tableColumn.splice(1, 0, {
                        id: 'ownGroupName',
                        label: this.$t('ownGroupName')
                    })
                } else {
                    this.tableColumn.splice(1, 1)
                }
            },
            handleSettingChange ({ fields, size }) {
                this.selectedTableColumn = fields
                this.tableSize = size
                localStorage.setItem(TABLE_COLUMN_CACHE, JSON.stringify({
                    columns: fields,
                    size
                }))
            }
        }
    }

</script>

<style lang="scss">
    @import '@/scss/conf.scss';
    .primary {
        color: $primaryColor;
    }
    .desc {
        color: #979BA5;
    }
    tr.no-permission {
        background-color: #FAFBFD;
    }
    tr.has-delete {
        color: #C4C6CC;
    }
    .selected-all-indicator {
        display: flex;
        align-items: center;
        justify-content: center;
        background: #EAEBF0;
        height: 32px;
    }
    .latest-build-multiple-row {
        display: flex;
        flex-direction: column;
    }
    .pipeline-list-table {
        td {
            position: inherit;
        }
        .bk-table-body-wrapper {
            td {
                .bk-table-setting-content {
                    display: none;
                }
            }
        }
        .bk-table-fixed-right {
            right: 6px !important;
        }
        ::-webkit-scrollbar {
            width: 6px !important;
            height: 6px !important;
            background-color: white;
        }
        ::-webkit-scrollbar-thumb {
            height: 6px;
            border-radius: 20px;
            background-color: #DCDEE5 !important;
        }
    }
    .exec-pipeline-btn {
        width: 55px;
        text-align: left;
        overflow: hidden;
    }
    .icon-star-btn {
        position: relative;
        font-size: 14px !important;
    }
</style>
