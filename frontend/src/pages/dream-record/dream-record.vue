<template>
    <view class="record-page">
        <view class="page-header">
            <text class="title">记录梦境</text>
            <text class="subtitle">描述你今晚的梦</text>
        </view>
        
        <view class="form-container">
            <!-- 分类选择 -->
            <view class="form-item">
                <view class="item-header" @click="showCategoryPicker = true">
                    <text class="label">梦境类型</text>
                    <text class="value">{{ currentCategory || '请选择' }}</text>
                    <text class="arrow">›</text>
                </view>
            </view>
            
            <!-- 做梦日期 -->
            <view class="form-item">
                <view class="item-header" @click="showDatePicker = true">
                    <text class="label">做梦日期</text>
                    <text class="value">{{ dreamDate || '请选择' }}</text>
                    <text class="arrow">›</text>
                </view>
            </view>
            
            <!-- 发生地点 -->
            <view class="form-item">
                <text class="label">发生地点</text>
                <input class="input" v-model="location" placeholder="梦中的地点" />
            </view>
            
            <!-- 关键词 -->
            <view class="form-item">
                <text class="label">关键词</text>
                <input class="input" v-model="keywords" placeholder="用逗号分隔多个关键词" />
            </view>
            
            <!-- 清晰度 -->
            <view class="form-item">
                <text class="label">清晰度</text>
                <view class="clarity-picker">
                    <view class="clarity-item" :class="{active: clarity >= i}" v-for="i in 5" :key="i" @click="clarity = i">
                        ⭐
                    </view>
                </view>
            </view>
            
            <!-- 详细描述 -->
            <view class="form-item">
                <text class="label">梦境描述</text>
                <textarea class="textarea" v-model="description" placeholder="详细描述你的梦境..." maxlength="2000" />
                <text class="char-count">{{ description.length }}/2000</text>
            </view>
            
            <!-- 是否重复 -->
            <view class="form-item">
                <text class="label">这是重复的梦吗？</text>
                <switch :checked="isRecurring" @change="isRecurring = $event.detail.value" color="#6C5CE7" />
            </view>
            
            <!-- 图片上传 -->
            <view class="form-item">
                <text class="label">上传图片(可选)</text>
                <view class="image-list">
                    <view class="image-item" v-for="(img, idx) in images" :key="idx">
                        <image class="preview-img" :src="img" mode="aspectFill" />
                        <view class="delete-btn" @click="removeImage(idx)">×</view>
                    </view>
                    <view class="upload-btn" v-if="images.length < 3" @click="chooseImage">
                        <text class="upload-icon">+</text>
                    </view>
                </view>
            </view>
            
            <button class="btn-primary submit-btn" @click="submitDream" :loading="submitting">
                发布梦境
            </button>
        </view>
    </view>
</template>

<script>
import { dreamApi, categoryApi, uploadFile } from '@/utils/request';
import { requireLogin } from '@/utils/auth';
import { uploadFile } from '@/utils/request';
import dayjs from 'dayjs';

export default {
    data() {
        return {
            categories: [],
            currentCategory: '',
            categoryId: null,
            dreamDate: dayjs().subtract(1, 'day').format('YYYY-MM-DD'),
            location: '',
            keywords: '',
            clarity: 3,
            description: '',
            isRecurring: false,
            images: [],
            imageUrls: [],
            showCategoryPicker: false,
            showDatePicker: false,
            submitting: false
        };
    },
    async onLoad() {
        if (!requireLogin()) return;
        await this.loadCategories();
    },
    methods: {
        async loadCategories() {
            try {
                this.categories = await categoryApi.list();
                if (this.categories.length > 0) {
                    this.currentCategory = this.categories[0].name;
                    this.categoryId = this.categories[0].id;
                }
            } catch (e) {
                console.error('Load categories failed:', e);
            }
        },
        
        async submitDream() {
            if (!this.description.trim()) {
                uni.showToast({ title: '请填写梦境描述', icon: 'none' });
                return;
            }
            if (!this.categoryId) {
                uni.showToast({ title: '请选择梦境类型', icon: 'none' });
                return;
            }
            
            // 先上传图片
            for (const img of this.images) {
                try {
                    const res = await uploadFile(img);
                    this.imageUrls.push(res.url);
                } catch (e) {
                    console.error('Upload failed:', e);
                }
            }
            
            this.submitting = true;
            try {
                await dreamApi.create({
                    categoryId: this.categoryId,
                    dreamDate: this.dreamDate,
                    location: this.location,
                    keywords: this.keywords,
                    clarity: this.clarity,
                    description: this.description,
                    isRecurring: this.isRecurring,
                    images: JSON.stringify(this.imageUrls)
                });
                
                uni.showToast({ title: '发布成功', icon: 'success' });
                
                // 重置表单
                this.description = '';
                this.location = '';
                this.keywords = '';
                this.clarity = 3;
                this.isRecurring = false;
                this.images = [];
                this.imageUrls = [];
                
                // 跳转到探索页
                setTimeout(() => {
                    uni.switchTab({ url: '/pages/explore/explore' });
                }, 1500);
            } catch (e) {
                console.error('Submit failed:', e);
            } finally {
                this.submitting = false;
            }
        },
        
        chooseImage() {
            uni.chooseImage({
                count: 3 - this.images.length,
                success: (res) => {
                    this.images = this.images.concat(res.tempFilePaths);
                }
            });
        },
        
        removeImage(idx) {
            this.images.splice(idx, 1);
        }
    }
};
</script>

<style lang="scss" scoped>
.record-page {
    min-height: 100vh;
    background: #F8F9FE;
}

.page-header {
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
    padding: calc(20rpx + env(safe-area-inset-top)) 32rpx 40rpx;
    text-align: center;
    
    .title {
        font-size: 40rpx;
        font-weight: 700;
        color: #FFFFFF;
        display: block;
    }
    
    .subtitle {
        font-size: 26rpx;
        color: rgba(255,255,255,0.7);
        display: block;
        margin-top: 8rpx;
    }
}

.form-container {
    padding: 24rpx;
}

.form-item {
    background: #FFFFFF;
    border-radius: 24rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(108, 92, 231, 0.06);
    
    .label {
        font-size: 26rpx;
        color: #636E72;
        margin-bottom: 12rpx;
        display: block;
    }
    
    .item-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .value {
            flex: 1;
            text-align: right;
            font-size: 28rpx;
            color: #2D3436;
            margin: 0 16rpx;
        }
        
        .arrow {
            font-size: 32rpx;
            color: #B2BEC3;
        }
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
        line-height: 1.6;
    }
    
    .char-count {
        display: block;
        text-align: right;
        font-size: 22rpx;
        color: #B2BEC3;
        margin-top: 8rpx;
    }
}

.clarity-picker {
    display: flex;
    gap: 16rpx;
    
    .clarity-item {
        font-size: 48rpx;
        color: #E0E0E0;
        transition: color 0.2s;
        
        &.active {
            color: #FDCB6E;
        }
    }
}

.image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    
    .image-item {
        position: relative;
        width: 160rpx;
        height: 160rpx;
        
        .preview-img {
            width: 100%;
            height: 100%;
            border-radius: 16rpx;
        }
        
        .delete-btn {
            position: absolute;
            top: -8rpx;
            right: -8rpx;
            width: 36rpx;
            height: 36rpx;
            background: #E17055;
            color: #FFFFFF;
            border-radius: 50%;
            text-align: center;
            line-height: 36rpx;
            font-size: 24rpx;
        }
    }
    
    .upload-btn {
        width: 160rpx;
        height: 160rpx;
        border: 2rpx dashed #D0D0D0;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .upload-icon {
            font-size: 48rpx;
            color: #B2BEC3;
        }
    }
}

.submit-btn {
    width: 100%;
    margin-top: 40rpx;
    font-size: 32rpx;
    letter-spacing: 4rpx;
}
</style>
