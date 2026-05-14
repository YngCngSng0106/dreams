<template>
    <view class="edit-page">
        <view class="header-bar">
            <text class="title">{{ $t('detail.editTitle') }}</text>
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中文' }}</text>
        </view>
        <view class="form-container" v-if="dream">
            <view class="form-item">
                <text class="label">{{ $t('detail.locationLabel') }}</text>
                <input class="input" v-model="form.location" :placeholder="$t('record.placeholder.location')" />
            </view>
            
            <view class="form-item">
                <text class="label">{{ $t('detail.keywordsLabel') }}</text>
                <input class="input" v-model="form.keywords" :placeholder="$t('record.placeholder.keywords')" />
            </view>
            
            <view class="form-item">
                <text class="label">{{ $t('detail.clarityLabel') }}</text>
                <view class="clarity-picker">
                    <view class="clarity-item" :class="{active: form.clarity >= i}" v-for="i in 5" :key="i" @click="form.clarity = i">
                        ⭐
                    </view>
                </view>
            </view>
            
            <view class="form-item">
                <text class="label">{{ $t('detail.descLabel') }}</text>
                <textarea class="textarea" v-model="form.description" :placeholder="$t('record.placeholder.description')" maxlength="2000" />
            </view>
            
            <view class="form-item">
                <text class="label">{{ $t('detail.recurringLabel') }}</text>
                <switch :checked="form.isRecurring" @change="form.isRecurring = $event.detail.value" color="#6C5CE7" />
            </view>
            
            <button class="btn-primary submit-btn" @click="saveDream" :loading="saving">{{ $t('detail.saveChanges') }}</button>
            <button class="btn-danger delete-btn" @click="deleteDream">{{ $t('detail.deleteDream') }}</button>
        </view>
    </view>
</template>

<script>
import { dreamApi } from '@/utils/api';
import { requireLogin, getUserId } from '@/utils/auth';
import { setLocale } from '@/locale';

export default {
    data() {
        return {
            dreamId: 0,
            dream: null,
            form: {},
            saving: false,
            currentLang: 'zh'
        };
    },
    async onLoad(options) {
        if (!requireLogin()) return;
        this.currentLang = uni.getStorageSync('locale') || 'zh';
        this.dreamId = parseInt(options.id) || 0;
        try {
            this.dream = await dreamApi.detail(this.dreamId);
            const myId = getUserId();
            if (this.dream.userId !== myId) {
                uni.showToast({ title: this.$t('detail.noAuthEdit'), icon: 'none' });
                setTimeout(() => uni.navigateBack(), 1500);
                return;
            }
            this.form = {
                location: this.dream.location || '',
                keywords: this.dream.keywords || '',
                clarity: this.dream.clarity || 3,
                description: this.dream.description || '',
                isRecurring: this.dream.isRecurring ? true : false
            };
        } catch (e) {
            console.error('Load dream failed:', e);
        }
    },
    methods: {
        toggleLang() {
            this.currentLang = this.currentLang === 'zh' ? 'en' : 'zh';
            setLocale(this.currentLang);
        },
        
        async saveDream() {
            if (!this.form.description.trim()) {
                uni.showToast({ title: this.$t('detail.fillDesc'), icon: 'none' });
                return;
            }
            this.saving = true;
            try {
                const submitData = {...this.form, isRecurring: this.form.isRecurring ? 1 : 0};
                await dreamApi.update(this.dreamId, submitData);
                uni.showToast({ title: this.$t('detail.saveSuccess'), icon: 'success' });
                setTimeout(() => uni.navigateBack(), 1500);
            } catch (e) {
                console.error('Save failed:', e);
            } finally {
                this.saving = false;
            }
        },
        
        async deleteDream() {
            uni.showModal({
                title: this.$t('detail.delete'),
                content: this.$t('detail.deleteConfirm'),
                success: async (res) => {
                    if (res.confirm) {
                        try {
                            await dreamApi.delete(this.dreamId);
                            uni.showToast({ title: this.$t('detail.deleted'), icon: 'success' });
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

.header-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    
    .title {
        font-size: 36rpx;
        font-weight: 700;
        color: #2D3436;
    }
    
    .lang-btn {
        font-size: 24rpx;
        color: #6C5CE7;
        background: rgba(108, 92, 231, 0.1);
        padding: 8rpx 16rpx;
        border-radius: 24rpx;
    }
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
