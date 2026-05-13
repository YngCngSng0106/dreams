<template>
    <view class="disc-detail-page">
        <view class="header-section card" v-if="discussion">
            <text class="title">{{ discussion.title }}</text>
            <text class="desc">{{ discussion.description }}</text>
            <view class="meta">
                <text>创建者: {{ discussion.creatorNickname }}</text>
                <text>成员: {{ discussion.memberCount }}人</text>
            </view>
            <view class="actions" v-if="isLoggedIn">
                <button class="btn-primary" size="mini" @click="joinDiscussion" v-if="!isMember">加入讨论</button>
                <button class="btn-secondary" size="mini" @click="leaveDiscussion" v-else>退出讨论</button>
            </view>
        </view>
        
        <!-- 评论列表 -->
        <view class="section-title">讨论内容</view>
        
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
                    <text>回复</text>
                </view>
            </view>
            
            <!-- 回复列表 -->
            <view class="reply-item" v-for="reply in comment.replies" :key="reply.commentId">
                <text class="reply-text">{{ reply.nickname }}: {{ reply.content }}</text>
            </view>
        </view>
        
        <!-- 评论输入框 -->
        <view class="comment-input" v-if="isLoggedIn">
            <input v-model="newComment" placeholder="说点什么..." class="input" />
            <button class="send-btn" @click="sendComment" size="mini">发送</button>
        </view>
        
        <view class="empty-state" v-if="!isLoggedIn">
            <text class="empty-text">请先登录后参与讨论</text>
            <button class="btn-primary" @click="goLogin">去登录</button>
        </view>
    </view>
</template>

<script>
import { discussionApi, commentApi } from '@/utils/api';
import { isLoggedIn, requireLogin, getUserId } from '@/utils/auth';
import dayjs from 'dayjs';

export default {
    data() {
        return {
            discussionId: 0,
            discussion: null,
            comments: [],
            isMember: false,
            newComment: '',
            replyToId: null
        };
    },
    computed: {
        isLoggedIn() { return isLoggedIn(); }
    },
    onLoad(options) {
        this.discussionId = options.id;
        this.loadDetail();
    },
    onShow() {
        if (this.discussionId) {
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
        
        async joinDiscussion() {
            if (!requireLogin()) return;
            try {
                await discussionApi.join(this.discussionId);
                this.isMember = true;
                uni.showToast({ title: '加入成功', icon: 'success' });
            } catch (e) {
                console.error('Join failed:', e);
            }
        },
        
        async leaveDiscussion() {
            if (!requireLogin()) return;
            try {
                await discussionApi.leave(this.discussionId);
                this.isMember = false;
                uni.showToast({ title: '已退出', icon: 'success' });
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
                uni.showToast({ title: '发送成功', icon: 'success' });
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
            return dayjs(time).fromNow();
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
</style>
