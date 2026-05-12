<template>
    <view class="edit-page">
        <view class="form-container" v-if="dream">
            <view class="form-item">
                <text class="label">发生地点</text>
                <input class="input" v-model="form.location" placeholder="梦中的地点" />
            </view>
            
            <view class="form-item">
                <text class="label">关键词</text>
                <input class="input" v-model="form.keywords" placeholder="用逗号分隔" />
            </view>
            
            <view class="form-item">
                <text class="label">清晰度</text>
                <view class="clarity-picker">
                    <view class="clarity-item" :class="{active: form.clarity >= i}" v-for="i in 5" :key="i" @click="form.clarity = i">
                        ⭐
                    </view>
                </view>
            </view>
            
            <view class="form-item">
                <text class="label">梦境描述</text>
                <textarea class="textarea" v-model="form.description" placeholder="详细描述..." maxlength="2000" />
            </view>
            
            <view class="form-item">
                <text class="label">这是重复的梦吗？</text>
                <switch :checked="form.isRecurring" @change="form.isRecurring = $event.detail.value" color="#6C5CE7" />
            </view>
            
            <button class="btn-primary submit-btn" @click="saveDream" :loading="saving">保存修改</button>
            <button class="btn-danger delete-btn" @click="deleteDream">删除梦境</button>
        </view>
    </view>
</template>

<script>
import { dreamApi } from '@/utils/api';
import { requireLogin, getUserId } from '@/utils/auth';

export default {
    data() {
        return {
            dreamId: 0,
            dream: null,
            form: {},
            saving: false
        };
    },
    async onLoad(options) {
        if (!requireLogin()) return;
        this.dreamId = options.id;
        try {
            this.dream = await dreamApi.detail(this.dreamId);
            const myId = getUserId();
            if (this.dream.userId !== myId) {
                uni.showToast({ title: '无权编辑', icon: 'none' });
                setTimeout(() => uni.navigateBack(), 1500);
                return;
            }
            this.form = {
                location: this.dream.location || '',
                keywords: this.dream.keywords || '',
                clarity: this.dream.clarity || 3,
                description: this.dream.description || '',
                isRecurring: this.dream.isRecurring || false
            };
        } catch (e) {
            console.error('Load dream failed:', e);
        }
    },
    methods: {
        async saveDream() {
            if (!this.form.description.trim()) {
                uni.showToast({ title: '请填写描述', icon: 'none' });
                return;
            }
            this.saving = true;
            try {
                await dreamApi.update(this.dreamId, this.form);
                uni.showToast({ title: '保存成功', icon: 'success' });
                setTimeout(() => uni.navigateBack(), 1500);
            } catch (e) {
                console.error('Save failed:', e);
            } finally {
                this.saving = false;
            }
        },
        
        async deleteDream() {
            uni.showModal({
                title: '提示',
                content: '确定要删除这个梦境吗？',
                success: async (res) => {
                    if (res.confirm) {
                        try {
                            await dreamApi.delete(this.dreamId);
                            uni.showToast({ title: '已删除', icon: 'success' });
                            setTimeout(() => uni.navigateBack(), 1500);
                        } catch (e) {
                            console.error('Delete failed:', e);
                        }
                    }
                }
            });
        }
    }
};
</script>

<style lang="scss" scoped>
.edit-page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.form-container {
    .form-item {
        background: #FFFFFF;
        border-radius: 24rpx;
        padding: 24rpx;
        margin-bottom: 20rpx;
        
        .label {
            font-size: 26rpx;
            color: #636E72;
            margin-bottom: 12rpx;
            display: block;
        }
        
        .input {
            width: 100%;
            font-size: 28rpx;
            color: #2D3436;
            background: #F8F9FE;
            border-radius: 12rpx;
            padding: 16rpx;
        }
        
        .textarea {
            width: 100%;
            min-height: 240rpx;
            font-size: 28rpx;
            color: #2D3436;
            background: #F8F9FE;
            border-radius: 12rpx;
            padding: 16rpx;
        }
    }
    
    .clarity-picker {
        display: flex;
        gap: 16rpx;
        
        .clarity-item {
            font-size: 48rpx;
            color: #E0E0E0;
            &.active { color: #FDCB6E; }
        }
    }
}

.submit-btn {
    width: 100%;
    margin-top: 40rpx;
    font-size: 32rpx;
}

.delete-btn {
    width: 100%;
    margin-top: 20rpx;
    background: #FFFFFF;
    color: #E17055;
    border: 2rpx solid #E17055;
    border-radius: 48rpx;
    font-size: 28rpx;
}
</style>
