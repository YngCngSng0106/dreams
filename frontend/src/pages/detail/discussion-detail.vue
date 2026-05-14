<template>
    <view class="disc-detail-page">
        <!-- 创建模式 -->
        <view class="create-form card" v-if="isCreateMode">
            <text class="form-title">{{ $t('detail.createDiscussion') || '创建讨论组' }}</text>
            <view class="form-item">
                <text class="label">{{ $t('detail.discussionTitle') || '标题' }}</text>
                <input class="input" v-model="createForm.title" :placeholder="$t('detail.enterTitle') || '请输入标题'" />
            </view>
            <view class="form-item">
                <text class="label">{{ $t('detail.discussionDesc') || '描述' }}</text>
                <textarea class="textarea" v-model="createForm.description" :placeholder="$t('detail.enterDesc') || '请输入描述'" maxlength="500" />
            </view>
            <view class="form-item">
                <text class="label">{{ $t('detail.discussionType') || '类型' }}</text>
                <picker :range="typeOptions" @change="onTypeChange" :value="typeIndex">
                    <view class="picker-value">{{ typeOptions[typeIndex] }}</view>
                </picker>
            </view>
            <button class="btn-primary" @click="submitCreate" :loading="submitting">{{ $t('detail.create') || '创建' }}</button>
        </view>

        <!-- 查看详情模式 -->
        <template v-else>
            <view class="header-section card" v-if="discussion">
                <text class="title">{{ discussion.title }}</text>
                <text class="desc">{{ discussion.description }}</text>
                <view class="meta">
                    <text>{{ $t('detail.creator') }}: {{ discussion.creatorNickname }}</text>
                    <text>{{ $t('detail.members') }}: {{ discussion.memberCount }}</text>
                </view>
                <view class="actions" v-if="isLoggedIn">
                    <button class="btn-primary" size="mini" @click="joinDiscussion" v-if="!isMember">{{ $t('detail.join') }}</button>
                    <button class="btn-secondary" size="mini" @click="leaveDiscussion" v-else>{{ $t('detail.leave') }}</button>
                </view>
            </view>

            <view class="section-title">{{ $t('detail.content') }}</view>

            <view class="comment-item card" v-for="comment in comments" :key="comment.commentId">
                <view class="comment-header">
                    <image class="avatar" :src="'/static/default-avatar.png'" mode="aspectFill" />
                    <text class="nickname">{{ comment.nickname }}</text>
                    <text class="time">{{ formatTime(comment.createTime) }}</text>
                </view>
                <text class="comment-content">{{ comment.content }}</text>
                <view class="comment-footer">
                    <view class="like-btn" @click="toggleCommentLike(comment)">
                        <text>{{ comment.liked ? '❤️' : '🤍' }}</text>
                        <text>{{ comment.likeCount }}</text>
                    </view>
                    <view class="reply-btn" @click="replyTo(comment)">
                        <text>{{ $t('detail.reply') }}</text>
                    </view>
                </view>

                <view class="reply-item" v-for="reply in comment.replies" :key="reply.commentId">
                    <text class="reply-text">{{ reply.nickname }}: {{ reply.content }}</text>
                </view>
            </view>

            <view class="comment-input" v-if="isLoggedIn">
                <input v-model="newComment" :placeholder="$t('detail.writeComment')" class="input" />
                <button class="send-btn" @click="sendComment" size="mini">{{ $t('detail.publishComment') }}</button>
            </view>

            <view class="empty-state" v-if="!isLoggedIn">
                <text class="empty-text">{{ $t('detail.loginFirst') }}</text>
                <button class="btn-primary" @click="goLogin">{{ $t('detail.goLogin') }}</button>
            </view>
        </template>
    </view>
</template>

<script>
import { discussionApi, commentApi } from '@/utils/api';
import { isLoggedIn, requireLogin, getUserId } from '@/utils/auth';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';
import 'dayjs/locale/zh';
dayjs.extend(relativeTime);
dayjs.locale('zh');

export default {
    data() {
        return {
            discussionId: 0,
            isCreateMode: false,
            discussion: null,
            comments: [],
            isMember: false,
            newComment: '',
            replyToId: null,
            creating: false,
            submitting: false,
            createForm: {
                title: '',
                description: '',
                type: 0
            },
            typeOptions: ['公开', '私密', '邀请'],
            typeIndex: 0
        };
    },
    computed: {
        isLoggedIn() { return isLoggedIn(); }
    },
    onLoad(options) {
        // 创建模式
        if (options.create === '1') {
            this.isCreateMode = true;
            this.discussionId = 0;
            return;
        }
        // 详情模式
        if (options.id) {
            this.discussionId = parseInt(options.id);
            this.isCreateMode = false;
            this.loadDetail();
        } else {
            uni.showToast({ title: '缺少参数', icon: 'none' });
            setTimeout(() => uni.navigateBack(), 1500);
        }
    },
    onShow() {
        if (!this.isCreateMode && this.discussionId) {
            this.loadDetail();
            this.loadComments();
        }
    },
    methods: {
        async loadDetail() {
            try {
                this.discussion = await discussionApi.detail(this.discussionId);
                const myId = getUserId();
                this.isMember = false; // TODO: check membership
            } catch (e) {
                console.error('Load detail failed:', e);
            }
        },

        async loadComments() {
            try {
                const res = await commentApi.list(this.discussionId);
                this.comments = res.records || [];
            } catch (e) {
                console.error('Load comments failed:', e);
            }
        },

        onTypeChange(e) {
            this.typeIndex = e.detail.value;
            this.createForm.type = this.typeIndex;
        },

        async submitCreate() {
            if (!requireLogin()) return;
            if (!this.createForm.title.trim()) {
                uni.showToast({ title: '请输入标题', icon: 'none' });
                return;
            }
            this.submitting = true;
            try {
                const result = await discussionApi.create({
                    title: this.createForm.title,
                    description: this.createForm.description,
                    type: this.createForm.type
                });
                uni.showToast({ title: '创建成功', icon: 'success' });
                this.discussionId = result.id || 0;
                this.isCreateMode = false;
                this.discussion = result;
                this.comments = [];
            } catch (e) {
                console.error('Create failed:', e);
                uni.showToast({ title: '创建失败', icon: 'none' });
            } finally {
                this.submitting = false;
            }
        },

        async joinDiscussion() {
            if (!requireLogin()) return;
            try {
                await discussionApi.join(this.discussionId);
                this.isMember = true;
                uni.showToast({ title: this.$t('detail.joinSuccess'), icon: 'success' });
            } catch (e) {
                console.error('Join failed:', e);
            }
        },

        async leaveDiscussion() {
            if (!requireLogin()) return;
            try {
                await discussionApi.leave(this.discussionId);
                this.isMember = false;
                uni.showToast({ title: this.$t('detail.leaveSuccess'), icon: 'success' });
            } catch (e) {
                console.error('Leave failed:', e);
            }
        },

        async sendComment() {
            if (!this.newComment.trim()) return;
            if (!requireLogin()) return;
            try {
                await commentApi.create({
                    discussionId: this.discussionId,
                    content: this.newComment,
                    parentId: this.replyToId || 0
                });
                this.newComment = '';
                this.replyToId = null;
                await this.loadComments();
                uni.showToast({ title: this.$t('detail.commentSuccess'), icon: 'success' });
            } catch (e) {
                console.error('Send comment failed:', e);
            }
        },

        async toggleCommentLike(comment) {
            if (!requireLogin()) return;
            try {
                if (comment.liked) {
                    await commentApi.unlike(comment.commentId);
                    comment.likeCount = Math.max(0, comment.likeCount - 1);
                } else {
                    await commentApi.like(comment.commentId);
                    comment.likeCount++;
                }
                comment.liked = !comment.liked;
            } catch (e) {
                console.error('Like failed:', e);
            }
        },

        replyTo(comment) {
            this.replyToId = comment.commentId;
            this.newComment = '@' + comment.nickname + ' ';
        },

        goLogin() {
            uni.navigateTo({ url: '/pages/auth/login' });
        },

        formatTime(time) {
            if (!time) return '';
            const now = dayjs();
            const target = dayjs(time);
            if (now.diff(target, 'hour') < 1) return target.fromNow();
            if (now.diff(target, 'day') < 1) return '今天 ' + target.format('HH:mm');
            if (now.diff(target, 'day') < 7) return target.format('MM-DD HH:mm');
            return target.format('YYYY-MM-DD');
        }
    }
};
</script>

<style lang="scss" scoped>
.disc-detail-page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.create-form {
    .form-title {
        font-size: 36rpx;
        font-weight: 700;
        color: #2D3436;
        display: block;
        margin-bottom: 24rpx;
    }
    .form-item {
        margin-bottom: 24rpx;
        display: flex;
        flex-direction: column;
        gap: 8rpx;
        .label {
            font-size: 26rpx;
            color: #636E72;
            font-weight: 500;
        }
        .input {
            background: #F8F9FE;
            border: none;
            border-radius: 16rpx;
            padding: 16rpx 20rpx;
            font-size: 28rpx;
        }
        .textarea {
            background: #F8F9FE;
            border: none;
            border-radius: 16rpx;
            padding: 16rpx 20rpx;
            font-size: 28rpx;
            height: 160rpx;
        }
        .picker-value {
            background: #F8F9FE;
            border-radius: 16rpx;
            padding: 16rpx 20rpx;
            font-size: 28rpx;
            color: #2D3436;
        }
    }
}

.btn-primary {
    background: #6C5CE7;
    color: #FFFFFF;
    border-radius: 24rpx;
    margin-top: 24rpx;
    font-size: 28rpx;
}

.btn-secondary {
    background: #F0F0F0;
    color: #636E72;
    border-radius: 24rpx;
    font-size: 24rpx;
}

.header-section {
    margin-bottom: 24rpx;
    .title {
        font-size: 36rpx;
        font-weight: 700;
        color: #2D3436;
        display: block;
        margin-bottom: 12rpx;
    }
    .desc {
        font-size: 26rpx;
        color: #636E72;
        display: block;
        margin-bottom: 16rpx;
    }
    .meta {
        display: flex;
        justify-content: space-between;
        font-size: 24rpx;
        color: #B2BEC3;
        margin-bottom: 16rpx;
    }
    .actions {
        display: flex;
        gap: 16rpx;
    }
}

.section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #2D3436;
    margin-bottom: 16rpx;
    padding: 0 8rpx;
}

.comment-item {
    margin-bottom: 20rpx;
    .comment-header {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;
        .avatar {
            width: 48rpx;
            height: 48rpx;
            border-radius: 50%;
            margin-right: 12rpx;
        }
        .nickname {
            font-size: 26rpx;
            font-weight: 600;
            color: #2D3436;
            margin-right: 16rpx;
        }
        .time {
            font-size: 22rpx;
            color: #B2BEC3;
        }
    }
    .comment-content {
        font-size: 28rpx;
        color: #2D3436;
        line-height: 1.6;
        display: block;
        margin-bottom: 12rpx;
    }
    .comment-footer {
        display: flex;
        gap: 32rpx;
        .like-btn, .reply-btn {
            display: flex;
            align-items: center;
            gap: 8rpx;
            font-size: 24rpx;
            color: #636E72;
        }
    }
    .reply-item {
        margin-left: 60rpx;
        padding: 12rpx 0;
        border-left: 4rpx solid #E8E8E8;
        padding-left: 16rpx;
        .reply-text {
            font-size: 24rpx;
            color: #636E72;
        }
    }
}

.comment-input {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #FFFFFF;
    padding: 16rpx 24rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 -4rpx 12rpx rgba(0,0,0,0.05);
    .input {
        flex: 1;
        background: #F8F9FE;
        border: none;
        border-radius: 24rpx;
        padding: 16rpx 24rpx;
        font-size: 26rpx;
        margin-right: 16rpx;
    }
    .send-btn {
        background: #6C5CE7;
        color: #FFFFFF;
        border-radius: 24rpx;
        font-size: 24rpx;
        padding: 0 24rpx;
    }
}

.empty-state {
    text-align: center;
    padding: 80rpx 0;
    .empty-text {
        font-size: 28rpx;
        color: #B2BEC3;
        display: block;
        margin-bottom: 24rpx;
    }
}

.card {
    background: #FFFFFF;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
</style>
