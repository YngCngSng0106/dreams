     1|     1|<template>
     2|     2|    <view class="disc-detail-page">
     3|     3|        <!-- 创建模式 -->
     4|     4|        <view class="create-form card" v-if="isCreateMode">
     5|     5|            <text class="form-title">{{ $t('detail.createDiscussion') }}</text>
     6|     6|            <view class="form-item">
     7|     7|                <text class="label">{{ $t('detail.discussionTitle') }}</text>
     8|     8|                <input class="input" v-model="createForm.title" :placeholder="$t('detail.enterTitle')" />
     9|     9|            </view>
    10|    10|            <view class="form-item">
    11|    11|                <text class="label">{{ $t('detail.discussionDesc') }}</text>
    12|    12|                <textarea class="textarea" v-model="createForm.description" :placeholder="$t('detail.enterDesc')" maxlength="500" />
    13|    13|            </view>
    14|    14|            <view class="form-item">
    15|    15|                <text class="label">{{ $t('detail.discussionType') }}</text>
    16|    16|                <picker :range="typeOptions" @change="onTypeChange" :value="typeIndex">
    17|    17|                    <view class="picker-value">{{ typeOptions[typeIndex] }}</view>
    18|    18|                </picker>
    19|    19|            </view>
    20|    20|            <button class="btn-primary" @click="submitCreate" :loading="submitting">{{ $t('discussion.create') || '创建' }}</button>
    21|    21|        </view>
    22|    22|
    23|    23|        <!-- 查看详情模式 -->
    24|    24|        <template v-else>
    25|    25|            <view class="header-section card" v-if="discussion">
    26|    26|                <text class="title">{{ discussion.title }}</text>
    27|    27|                <text class="desc">{{ discussion.description }}</text>
    28|    28|                <view class="meta">
    29|    29|                    <text>{{ $t('detail.creator') }}: {{ discussion.creatorNickname }}</text>
    30|    30|                    <text>{{ $t('detail.members') }}: {{ discussion.memberCount }}</text>
    31|    31|                </view>
    32|    32|                <view class="actions" v-if="isLoggedIn">
    33|    33|                    <button class="btn-primary" size="mini" @click="joinDiscussion" v-if="!isMember">{{ $t('detail.join') }}</button>
    34|    34|                    <button class="btn-secondary" size="mini" @click="leaveDiscussion" v-else>{{ $t('detail.leave') }}</button>
    35|    35|                </view>
    36|    36|            </view>
    37|    37|
    38|    38|            <view class="section-title">{{ $t('detail.content') }}</view>
    39|    39|
    40|    40|            <view class="comment-item card" v-for="comment in comments" :key="comment.commentId">
    41|    41|                <view class="comment-header">
    42|    42|                    <image class="avatar" :src="'/static/default-avatar.png'" mode="aspectFill" />
    43|    43|                    <text class="nickname">{{ comment.nickname }}</text>
    44|    44|                    <text class="time">{{ formatTime(comment.createTime) }}</text>
    45|    45|                </view>
    46|    46|                <text class="comment-content">{{ comment.content }}</text>
    47|    47|                <view class="comment-footer">
    48|    48|                    <view class="like-btn" @click="toggleCommentLike(comment)">
    49|    49|                        <text>{{ comment.liked ? '❤️' : '🤍' }}</text>
    50|    50|                        <text>{{ comment.likeCount }}</text>
    51|    51|                    </view>
    52|    52|                    <view class="reply-btn" @click="replyTo(comment)">
    53|    53|                        <text>{{ $t('detail.reply') }}</text>
    54|    54|                    </view>
    55|    55|                </view>
    56|    56|
    57|    57|                <view class="reply-item" v-for="reply in comment.replies" :key="reply.commentId">
    58|    58|                    <text class="reply-text">{{ reply.nickname }}: {{ reply.content }}</text>
    59|    59|                </view>
    60|    60|            </view>
    61|    61|
    62|    62|            <view class="comment-input" v-if="isLoggedIn">
    63|    63|                <input v-model="newComment" :placeholder="$t('detail.writeComment')" class="input" />
    64|    64|                <button class="send-btn" @click="sendComment" size="mini">{{ $t('detail.publishComment') }}</button>
    65|    65|            </view>
    66|    66|
    67|    67|            <view class="empty-state" v-if="!isLoggedIn">
    68|    68|                <text class="empty-text">{{ $t('detail.loginFirst') }}</text>
    69|    69|                <button class="btn-primary" @click="goLogin">{{ $t('detail.goLogin') }}</button>
    70|    70|            </view>
    71|    71|        </template>
    72|    72|    </view>
    73|    73|</template>
    74|    74|
    75|    75|<script>
    76|    76|import { discussionApi, commentApi } from '@/utils/api';
    77|    77|import { isLoggedIn, requireLogin, getUserId } from '@/utils/auth';
    78|    78|import dayjs from 'dayjs';
    79|    79|import relativeTime from 'dayjs/plugin/relativeTime.js';
    80|    80|import 'dayjs/locale/zh';
    81|    81|dayjs.extend(relativeTime);
    82|    82|dayjs.locale('zh');
    83|    83|
    84|    84|export default {
    85|    85|    data() {
    86|    86|        return {
    87|    87|            discussionId: 0,
    88|    88|            isCreateMode: false,
    89|    89|            discussion: null,
    90|    90|            comments: [],
    91|    91|            isMember: false,
    92|    92|            newComment: '',
    93|    93|            replyToId: null,
    94|    94|            creating: false,
    95|    95|            submitting: false,
    96|    96|            createForm: {
    97|    97|                title: '',
    98|    98|                description: '',
    99|    99|                type: 0
   100|   100|            },
   101|   101|            typeIndex: 0
   102|   102|        };
   103|   103|    },
   104|   104|    computed: {
   105|   105|        isLoggedIn() { return isLoggedIn(); },
   106|   106|        typeOptions() {
   107|   107|            return [
   108|   108|                this.$t('detail.typePublic', '公开'),
   109|   109|                this.$t('detail.typePrivate', '私密'),
   110|   110|                this.$t('detail.typeInvite', '邀请')
   111|   111|            ];
   112|   112|        }
   113|   113|    },
   114|   114|    onLoad(options) {
   115|   115|        // 创建模式
   116|   116|        if (options.create === '1') {
   117|   117|            this.isCreateMode = true;
   118|   118|            this.discussionId = 0;
   119|   119|            return;
   120|   120|        }
   121|   121|        // 详情模式
   122|   122|        if (options.id) {
   123|   123|            this.discussionId = parseInt(options.id);
   124|   124|            this.isCreateMode = false;
   125|   125|            this.loadDetail();
   126|   126|        } else {
   127|   127|            uni.showToast({ title: '缺少参数', icon: 'none' });
   128|   128|            setTimeout(() => uni.navigateBack(), 1500);
   129|   129|        }
   130|   130|    },
   131|   131|    onShow() {
   132|   132|        if (!this.isCreateMode && this.discussionId) {
   133|   133|            this.loadDetail();
   134|   134|            this.loadComments();
   135|   135|        }
   136|   136|    },
   137|   137|    methods: {
   138|   138|        async loadDetail() {
   139|   139|            try {
   140|   140|                this.discussion = await discussionApi.detail(this.discussionId);
   141|   141|                const myId = getUserId();
   142|   142|                this.isMember = false; // TODO: check membership
   143|   143|            } catch (e) {
   144|   144|                console.error('Load detail failed:', e);
   145|   145|            }
   146|   146|        },
   147|   147|
   148|   148|        async loadComments() {
   149|   149|            try {
   150|   150|                const res = await commentApi.list(this.discussionId);
   151|   151|                this.comments = res.records || [];
   152|   152|            } catch (e) {
   153|   153|                console.error('Load comments failed:', e);
   154|   154|            }
   155|   155|        },
   156|   156|
   157|   157|        onTypeChange(e) {
   158|   158|            this.typeIndex = e.detail.value;
   159|   159|            this.createForm.type = this.typeIndex;
   160|   160|        },
   161|   161|
   162|   162|        async submitCreate() {
   163|   163|            if (!requireLogin()) return;
   164|   164|            if (!this.createForm.title.trim()) {
   165|   165|                uni.showToast({ title: '请输入标题', icon: 'none' });
   166|   166|                return;
   167|   167|            }
   168|   168|            this.submitting = true;
   169|   169|            try {
   170|   170|                const result = await discussionApi.create({
   171|   171|                    title: this.createForm.title,
   172|   172|                    description: this.createForm.description,
   173|   173|                    type: this.createForm.type
   174|   174|                });
   175|   175|                uni.showToast({ title: '创建成功', icon: 'success' });
   176|   176|                this.discussionId = result.id || 0;
   177|   177|                this.isCreateMode = false;
   178|   178|                this.discussion = result;
   179|   179|                this.comments = [];
   180|   180|            } catch (e) {
   181|   181|                console.error('Create failed:', e);
   182|   182|                uni.showToast({ title: '创建失败', icon: 'none' });
   183|   183|            } finally {
   184|   184|                this.submitting = false;
   185|   185|            }
   186|   186|        },
   187|   187|
   188|   188|        async joinDiscussion() {
   189|   189|            if (!requireLogin()) return;
   190|   190|            try {
   191|   191|                await discussionApi.join(this.discussionId);
   192|   192|                this.isMember = true;
   193|   193|                uni.showToast({ title: this.$t('detail.joinSuccess'), icon: 'success' });
   194|   194|            } catch (e) {
   195|   195|                console.error('Join failed:', e);
   196|   196|            }
   197|   197|        },
   198|   198|
   199|   199|        async leaveDiscussion() {
   200|   200|            if (!requireLogin()) return;
   201|   201|            try {
   202|   202|                await discussionApi.leave(this.discussionId);
   203|   203|                this.isMember = false;
   204|   204|                uni.showToast({ title: this.$t('detail.leaveSuccess'), icon: 'success' });
   205|   205|            } catch (e) {
   206|   206|                console.error('Leave failed:', e);
   207|   207|            }
   208|   208|        },
   209|   209|
   210|   210|        async sendComment() {
   211|   211|            if (!this.newComment.trim()) return;
   212|   212|            if (!requireLogin()) return;
   213|   213|            try {
   214|   214|                await commentApi.create({
   215|   215|                    discussionId: this.discussionId,
   216|   216|                    content: this.newComment,
   217|   217|                    parentId: this.replyToId || 0
   218|   218|                });
   219|   219|                this.newComment = '';
   220|   220|                this.replyToId = null;
   221|   221|                await this.loadComments();
   222|   222|                uni.showToast({ title: this.$t('detail.commentSuccess'), icon: 'success' });
   223|   223|            } catch (e) {
   224|   224|                console.error('Send comment failed:', e);
   225|   225|            }
   226|   226|        },
   227|   227|
   228|   228|        async toggleCommentLike(comment) {
   229|   229|            if (!requireLogin()) return;
   230|   230|            try {
   231|   231|                if (comment.liked) {
   232|   232|                    await commentApi.unlike(comment.commentId);
   233|   233|                    comment.likeCount = Math.max(0, comment.likeCount - 1);
   234|   234|                } else {
   235|   235|                    await commentApi.like(comment.commentId);
   236|   236|                    comment.likeCount++;
   237|   237|                }
   238|   238|                comment.liked = !comment.liked;
   239|   239|            } catch (e) {
   240|   240|                console.error('Like failed:', e);
   241|   241|            }
   242|   242|        },
   243|   243|
   244|   244|        replyTo(comment) {
   245|   245|            this.replyToId = comment.commentId;
   246|   246|            this.newComment = '@' + comment.nickname + ' ';
   247|   247|        },
   248|   248|
   249|   249|        goLogin() {
   250|   250|            uni.navigateTo({ url: '/pages/auth/login' });
   251|   251|        },
   252|   252|
   253|   253|        formatTime(time) {
   254|   254|            if (!time) return '';
   255|   255|            const now = dayjs();
   256|   256|            const target = dayjs(time);
   257|   257|            if (now.diff(target, 'hour') < 1) return target.fromNow();
   258|   258|            if (now.diff(target, 'day') < 1) return '今天 ' + target.format('HH:mm');
   259|   259|            if (now.diff(target, 'day') < 7) return target.format('MM-DD HH:mm');
   260|   260|            return target.format('YYYY-MM-DD');
   261|   261|        }
   262|   262|    }
   263|   263|};
   264|   264|</script>
   265|   265|
   266|   266|<style lang="scss" scoped>
   267|   267|.disc-detail-page {
   268|   268|    min-height: 100vh;
   269|   269|    padding: 24rpx;
   270|   270|    padding-top: calc(24rpx + env(safe-area-inset-top));
   271|   271|}
   272|   272|
   273|   273|.create-form {
   274|   274|    .form-title {
   275|   275|        font-size: 36rpx;
   276|   276|        font-weight: 700;
   277|   277|        color: $text-primary;
   278|   278|        display: block;
   279|   279|        margin-bottom: 24rpx;
   280|   280|    }
   281|   281|    .form-item {
   282|   282|        margin-bottom: 24rpx;
   283|   283|        display: flex;
   284|   284|        flex-direction: column;
   285|   285|        gap: 8rpx;
   286|   286|        .label {
   287|   287|            font-size: 26rpx;
   288|   288|            color: $text-secondary;
   289|   289|            font-weight: 500;
   290|   290|        }
   291|   291|        .input {
   292|   292|            background: $glass-card-bg-light;
   293|   293|            border: none;
   294|   294|            border-radius: 16rpx;
   295|   295|            padding: 16rpx 20rpx;
   296|   296|            font-size: 28rpx;
   297|   297|        }
   298|   298|        .textarea {
   299|   299|            background: $glass-card-bg-light;
   300|   300|            border: none;
   301|   301|            border-radius: 16rpx;
   302|   302|            padding: 16rpx 20rpx;
   303|   303|            font-size: 28rpx;
   304|   304|            height: 160rpx;
   305|   305|        }
   306|   306|        .picker-value {
   307|   307|            background: $glass-card-bg-light;
   308|   308|            border-radius: 16rpx;
   309|   309|            padding: 16rpx 20rpx;
   310|   310|            font-size: 28rpx;
   311|   311|            color: $text-primary;
   312|   312|        }
   313|   313|    }
   314|   314|}
   315|   315|
   316|   316|.btn-primary {
   317|   317|    background: #6C5CE7;
   318|   318|    color: $text-primary;
   319|   319|    border-radius: 24rpx;
   320|   320|    margin-top: 24rpx;
   321|   321|    font-size: 28rpx;
   322|   322|}
   323|   323|
   324|   324|.btn-secondary {
   325|   325|    background: $glass-card-bg;
   326|   326|    color: $text-secondary;
   327|   327|    border-radius: 24rpx;
   328|   328|    font-size: 24rpx;
   329|   329|}
   330|   330|
   331|   331|.header-section {
   332|   332|    margin-bottom: 24rpx;
   333|   333|    .title {
   334|   334|        font-size: 36rpx;
   335|   335|        font-weight: 700;
   336|   336|        color: $text-primary;
   337|   337|        display: block;
   338|   338|        margin-bottom: 12rpx;
   339|   339|    }
   340|   340|    .desc {
   341|   341|        font-size: 26rpx;
   342|   342|        color: $text-secondary;
   343|   343|        display: block;
   344|   344|        margin-bottom: 16rpx;
   345|   345|    }
   346|   346|    .meta {
   347|   347|        display: flex;
   348|   348|        justify-content: space-between;
   349|   349|        font-size: 24rpx;
   350|   350|        color: $text-tertiary;
   351|   351|        margin-bottom: 16rpx;
   352|   352|    }
   353|   353|    .actions {
   354|   354|        display: flex;
   355|   355|        gap: 16rpx;
   356|   356|    }
   357|   357|}
   358|   358|
   359|   359|.section-title {
   360|   360|    font-size: 30rpx;
   361|   361|    font-weight: 600;
   362|   362|    color: $text-primary;
   363|   363|    margin-bottom: 16rpx;
   364|   364|    padding: 0 8rpx;
   365|   365|}
   366|   366|
   367|   367|.comment-item {
   368|   368|    margin-bottom: 20rpx;
   369|   369|    .comment-header {
   370|   370|        display: flex;
   371|   371|        align-items: center;
   372|   372|        margin-bottom: 12rpx;
   373|   373|        .avatar {
   374|   374|            width: 48rpx;
   375|   375|            height: 48rpx;
   376|   376|            border-radius: 50%;
   377|   377|            margin-right: 12rpx;
   378|   378|        }
   379|   379|        .nickname {
   380|   380|            font-size: 26rpx;
   381|   381|            font-weight: 600;
   382|   382|            color: $text-primary;
   383|   383|            margin-right: 16rpx;
   384|   384|        }
   385|   385|        .time {
   386|   386|            font-size: 22rpx;
   387|   387|            color: $text-tertiary;
   388|   388|        }
   389|   389|    }
   390|   390|    .comment-content {
   391|   391|        font-size: 28rpx;
   392|   392|        color: $text-primary;
   393|   393|        line-height: 1.6;
   394|   394|        display: block;
   395|   395|        margin-bottom: 12rpx;
   396|   396|    }
   397|   397|    .comment-footer {
   398|   398|        display: flex;
   399|   399|        gap: 32rpx;
   400|   400|        .like-btn, .reply-btn {
   401|   401|            display: flex;
   402|   402|            align-items: center;
   403|   403|            gap: 8rpx;
   404|   404|            font-size: 24rpx;
   405|   405|            color: $text-secondary;
   406|   406|        }
   407|   407|    }
   408|   408|    .reply-item {
   409|   409|        margin-left: 60rpx;
   410|   410|        padding: 12rpx 0;
   411|   411|        border-left: 4rpx solid $glass-card-bg;
   412|   412|        padding-left: 16rpx;
   413|   413|        .reply-text {
   414|   414|            font-size: 24rpx;
   415|   415|            color: $text-secondary;
   416|   416|        }
   417|   417|    }
   418|   418|}
   419|   419|
   420|   420|.comment-input {
   421|   421|    position: fixed;
   422|   422|    bottom: 0;
   423|   423|    left: 0;
   424|   424|    right: 0;
   425|   425|    background: $glass-tabbar-bg;
   426|   426|    backdrop-filter: blur(20px);
   427|   427|    -webkit-backdrop-filter: blur(20px);
   428|   428|    padding: 16rpx 24rpx;
   429|   429|    display: flex;
   430|   430|    align-items: center;
   431|   431|    box-shadow: 0 -4rpx 12rpx $glass-shadow;
   432|   432|    .input {
   433|   433|        flex: 1;
   434|   434|        background: $glass-card-bg-light;
   435|   435|        border: none;
   436|   436|        border-radius: 24rpx;
   437|   437|        padding: 16rpx 24rpx;
   438|   438|        font-size: 26rpx;
   439|   439|        margin-right: 16rpx;
   440|   440|    }
   441|   441|    .send-btn {
   442|   442|        background: #6C5CE7;
   443|   443|        color: $text-primary;
   444|   444|        border-radius: 24rpx;
   445|   445|        font-size: 24rpx;
   446|   446|        padding: 0 24rpx;
   447|   447|    }
   448|   448|}
   449|   449|
   450|   450|.empty-state {
   451|   451|    text-align: center;
   452|   452|    padding: 80rpx 0;
   453|   453|    .empty-text {
   454|   454|        font-size: 28rpx;
   455|   455|        color: $text-tertiary;
   456|   456|        display: block;
   457|   457|        margin-bottom: 24rpx;
   458|   458|    }
   459|   459|}
   460|   460|
   461|   461|.card {
   462|   462|    background: $glass-card-bg;
   463|   463|    backdrop-filter: blur(20px);
   464|   464|    -webkit-backdrop-filter: blur(20px);
   465|   465|    border: 1rpx solid $glass-card-bg-hover;
   466|   466|    border-radius: 16rpx;
   467|   467|    padding: 24rpx;
   468|   468|    box-shadow: 0 4rpx 16rpx $glass-shadow-light;
   469|   469|}
   470|   470|</style>
   471|   471|