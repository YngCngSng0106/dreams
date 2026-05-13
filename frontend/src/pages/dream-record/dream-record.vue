<template>
    <view class="gradient-bg dream-record-page">
        <view class="header-bar">
            <text class="title">{{ $t('record.title') }}</text>
            <text class="subtitle">{{ $t('record.subtitle') }}</text>
            <view class="lang-switch">
                <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
            </view>
        </view>
        
        <view class="form-section">
            <!-- 梦境类型 - 加@click打开弹窗 -->
            <view class="form-item picker-row" @click="openCategoryPicker">
                <text class="label">{{ $t('record.dreamType') }}</text>
                <text class="value">{{ currentCategory || $t('record.pleaseSelect') }}</text>
                <text class="arrow">›</text>
            </view>
            
            <!-- 做梦日期 - 用picker组件 -->
            <view class="form-item picker-row">
                <text class="label">{{ $t('record.dreamDate') }}</text>
                <picker mode="date" :start="dateStart" :end="dateEnd" :value="dreamDate" @change="onDateChange">
                    <text class="value">{{ dreamDate || $t('record.pleaseSelect') }}</text>
                </picker>
                <text class="arrow">›</text>
            </view>
            
            <!-- 发生地点 -->
            <view class="form-item">
                <text class="label">{{ $t('record.location') }}</text>
                <input class="input" v-model="location" :placeholder="$t('record.locationPlaceholder')" placeholder-class="input-placeholder" />
            </view>
            
            <!-- 关键词 -->
            <view class="form-item">
                <text class="label">{{ $t('record.keywords') }}</text>
                <input class="input" v-model="keywords" :placeholder="$t('record.keywordsPlaceholder')" placeholder-class="input-placeholder" />
            </view>
            
            <!-- 清晰度 -->
            <view class="form-item">
                <text class="label">{{ $t('record.clarity') }}</text>
                <view class="clarity-options">
                    <text class="clarity-btn" :class="{active: clarity===1}" @click.stop="clarity=1">{{ $t('record.low') }}</text>
                    <text class="clarity-btn" :class="{active: clarity===2}" @click.stop="clarity=2">{{ $t('record.mid') }}</text>
                    <text class="clarity-btn" :class="{active: clarity===3}" @click.stop="clarity=3">{{ $t('record.high') }}</text>
                </view>
            </view>
            
            <!-- 梦境描述 -->
            <view class="form-item">
                <text class="label">{{ $t('record.description') }}</text>
                <textarea class="textarea" v-model="description" :placeholder="$t('record.descriptionPlaceholder')" maxlength="2000" />
            </view>
            
            <!-- 重复梦 -->
            <view class="form-item">
                <text class="label">{{ $t('record.isRecurring') }}</text>
                <switch :checked="isRecurring" @change="isRecurring=$event.detail.value" color="#6C5CE7" />
            </view>
            
            <!-- 上传图片 -->
            <view class="form-item" v-if="imageList.length > 0 || imageList.length < 3">
                <text class="label">{{ $t('record.uploadImage') }}</text>
                <view class="image-list">
                    <view class="image-item" v-for="(img, index) in imageList" :key="index">
                        <image :src="img" mode="aspectFill" class="preview-img" />
                        <text class="delete-icon" @click.stop="imageList.splice(index,1)">✕</text>
                    </view>
                    <view class="upload-btn" v-if="imageList.length < 3" @click="uploadImage">+</view>
                </view>
            </view>
            
            <button class="publish-btn" @click="submitDream" :loading="submitting">
                {{ $t('record.publish') }}
            </button>
        </view>
        
        <!-- 分类选择弹窗 -->
        <view class="category-modal" v-if="showCategory" @click="showCategory=false">
            <view class="modal-content" @click.stop>
                <text class="modal-title">{{ $t('record.dreamType') }}</text>
                <view class="category-item" v-for="cat in categories" :key="cat.id" @click="selectCategory(cat)">
                    <text class="cat-icon">{{ cat.icon || '🔮' }}</text>
                    <text class="cat-name">{{ cat.name }}</text>
                </view>
                <view class="modal-close" @click="showCategory=false">{{ $t('record.cancel') }}</view>
            </view>
        </view>
    </view>

    <custom-tab-bar ref="tabbar" />
</template>

<script>
import { dreamApi, categoryApi } from '@/utils/api';
import { requireLogin, getToken } from '@/utils/auth';
import { setLocale } from '@/locale';

export default {
    data() {
        const now = new Date();
        const yyyy = now.getFullYear();
        return {
            dreamType: '',
            currentCategory: '',
            dreamDate: `${yyyy}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`,
            dateStart: `${yyyy-1}-01-01`,
            dateEnd: `${yyyy+1}-12-31`,
            location: '',
            keywords: '',
            clarity: 2,
            description: '',
            isRecurring: false,
            imageList: [],
            categories: [],
            showCategory: false,
            submitting: false,
            currentLang: uni.getStorageSync('locale') || 'zh'
        };
    },
    async onLoad() {
        if (!requireLogin()) return;
        await this.loadCategories();
    },
    onShow() {
        this.$nextTick(() => {
            const tabbar = this.$refs.tabbar;
            if (tabbar) tabbar.updateCurrentPage();
        });
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            this.currentLang = next;
            setLocale(next);
        },
        async loadCategories() {
            try {
                this.categories = await categoryApi.list();
                if (this.categories.length > 0) {
                    this.currentCategory = this.categories[0].name;
                    this.dreamType = String(this.categories[0].id);
                }
            } catch (e) {
                console.error('Load categories failed:', e);
            }
        },
        openCategoryPicker() {
            this.showCategory = true;
        },
        onDateChange(e) {
            this.dreamDate = e.detail.value;
        },
        selectCategory(cat) {
            this.currentCategory = cat.name;
            this.dreamType = String(cat.id);
            this.showCategory = false;
        },
        uploadImage() {
            uni.chooseImage({
                count: 3 - this.imageList.length,
                success: (res) => {
                    this.imageList.push(...res.tempFilePaths);
                }
            });
        },
        async submitDream() {
            if (!this.description) {
                uni.showToast({ title: this.$t('record.fillDesc'), icon: 'none' });
                return;
            }
            if (!this.currentCategory) {
                uni.showToast({ title: this.$t('record.selectType'), icon: 'none' });
                return;
            }
            this.submitting = true;
            try {
                await dreamApi.create({
                    categoryId: this.dreamType,
                    title: this.description.substring(0, 50),
                    content: this.description,
                    dreamDate: this.dreamDate,
                    location: this.location,
                    keywords: this.keywords.split(',').filter(k => k.trim()),
                    clarity: this.clarity,
                    isRecurring: this.isRecurring
                });
                uni.showToast({ title: this.$t('record.publishSuccess'), icon: 'success' });
                setTimeout(() => {
                    uni.navigateBack();
                }, 1500);
            } catch (e) {
                console.error('Submit dream failed:', e);
            } finally {
                this.submitting = false;
            }
        }
    }
};
</script>

<style lang="scss" scoped>
.dream-record-page {
    min-height: 100vh;
    padding-bottom: 120rpx;
}

.header-bar {
    padding: 40rpx;
    text-align: center;
    position: relative;
    
    .lang-switch {
        position: absolute;
        top: 20rpx;
        right: 20rpx;
    }
    
    .lang-btn {
        font-size: 24rpx;
        color: rgba(255,255,255,0.8);
        background: rgba(255,255,255,0.15);
        padding: 8rpx 16rpx;
        border-radius: 12rpx;
    }
    
    .title {
        display: block;
        font-size: 40rpx;
        color: #FFFFFF;
        font-weight: bold;
        margin-bottom: 12rpx;
    }
    
    .subtitle {
        font-size: 28rpx;
        color: rgba(255,255,255,0.7);
    }
}

.form-section {
    padding: 0 32rpx;
}

.form-item {
    background: rgba(255,255,255,0.15);
    border-radius: 16rpx;
    padding: 24rpx 32rpx;
    margin-bottom: 24rpx;
    display: flex;
    align-items: center;
    
    .label {
        font-size: 30rpx;
        color: #FFFFFF;
        width: 160rpx;
        flex-shrink: 0;
    }
    
    .value {
        flex: 1;
        font-size: 28rpx;
        color: rgba(255,255,255,0.8);
    }
    
    .arrow {
        font-size: 32rpx;
        color: rgba(255,255,255,0.5);
    }
    
    .input {
        flex: 1;
        font-size: 28rpx;
        color: #FFFFFF;
    }
    
    .textarea {
        flex: 1;
        font-size: 28rpx;
        color: #FFFFFF;
        min-height: 200rpx;
        &::placeholder {
            color: rgba(255,255,255,0.45);
        }
    }
    
    .clarity-options {
        display: flex;
        gap: 20rpx;
        
        .clarity-btn {
            padding: 8rpx 24rpx;
            border-radius: 24rpx;
            font-size: 26rpx;
            color: rgba(255,255,255,0.6);
            border: 2rpx solid transparent;
            &.active {
                background: #6C5CE7;
                color: #FFFFFF;
                border-color: rgba(255,255,255,0.5);
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
                border-radius: 12rpx;
            }
            
            .delete-icon {
                position: absolute;
                top: -10rpx;
                right: -10rpx;
                width: 40rpx;
                height: 40rpx;
                background: rgba(0,0,0,0.6);
                border-radius: 50%;
                text-align: center;
                line-height: 40rpx;
                font-size: 24rpx;
                color: #FFFFFF;
            }
        }
        
        .upload-btn {
            width: 160rpx;
            height: 160rpx;
            border: 2rpx dashed rgba(255,255,255,0.5);
            border-radius: 12rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 48rpx;
            color: rgba(255,255,255,0.6);
        }
    }
}

.publish-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    background: rgba(108, 92, 231, 0.9);
    color: #FFFFFF;
    font-size: 34rpx;
    border-radius: 44rpx;
    margin-top: 40rpx;
    letter-spacing: 4rpx;
    border: none;
}

.category-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: flex-end;
    z-index: 100;
    
    .modal-content {
        background: #FFFFFF;
        border-radius: 24rpx 24rpx 0 0;
        padding: 32rpx;
        max-height: 60vh;
        overflow-y: auto;
        width: 100%;
        
        .modal-title {
            display: block;
            text-align: center;
            font-size: 34rpx;
            font-weight: bold;
            color: #2C3E50;
            margin-bottom: 24rpx;
        }
        
        .category-item {
            display: flex;
            align-items: center;
            padding: 20rpx 16rpx;
            border-bottom: 1rpx solid #F0F0F0;
            
            .cat-icon {
                font-size: 40rpx;
                margin-right: 16rpx;
            }
            
            .cat-name {
                font-size: 30rpx;
                color: #2C3E50;
            }
            
            &:last-child {
                border-bottom: none;
            }
        }
        
        .modal-close {
            text-align: center;
            padding: 24rpx;
            margin-top: 16rpx;
            font-size: 28rpx;
            color: #6C5CE7;
            background: rgba(108, 92, 231, 0.08);
            border-radius: 16rpx;
        }
    }
}

.input-placeholder {
    color: rgba(255,255,255,0.45) !important;
}
</style>
