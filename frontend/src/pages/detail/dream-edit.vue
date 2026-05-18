     1|<template>
     2|    <view class="edit-page">
     3|        <view class="header-bar">
     4|            <text class="title">{{ $t('detail.editTitle') }}</text>
     5|            <LangSwitch />
     6|        </view>
     7|        <view class="form-container" v-if="dream">
     8|            <view class="form-item">
     9|                <text class="label">{{ $t('detail.locationLabel') }}</text>
    10|                <input class="input" v-model="form.location" :placeholder="$t('record.placeholder.location')" />
    11|            </view>
    12|            
    13|            <view class="form-item">
    14|                <text class="label">{{ $t('detail.keywordsLabel') }}</text>
    15|                <input class="input" v-model="form.keywords" :placeholder="$t('record.placeholder.keywords')" />
    16|            </view>
    17|            
    18|            <view class="form-item">
    19|                <text class="label">{{ $t('detail.clarityLabel') }}</text>
    20|                <view class="clarity-picker">
    21|                    <view class="clarity-item" :class="{active: form.clarity >= i}" v-for="i in 5" :key="i" @click="form.clarity = i">
    22|                        ⭐
    23|                    </view>
    24|                </view>
    25|            </view>
    26|            
    27|            <view class="form-item">
    28|                <text class="label">{{ $t('detail.descLabel') }}</text>
    29|                <textarea class="textarea" v-model="form.description" :placeholder="$t('record.placeholder.description')" maxlength="2000" />
    30|            </view>
    31|            
    32|            <view class="form-item">
    33|                <text class="label">{{ $t('detail.recurringLabel') }}</text>
    34|                <switch :checked="form.isRecurring" @change="form.isRecurring = $event.detail.value" color="#6C5CE7" />
    35|            </view>
    36|            
    37|            <button class="btn-primary submit-btn" @click="saveDream" :loading="saving">{{ $t('detail.saveChanges') }}</button>
    38|            <button class="btn-danger delete-btn" @click="deleteDream">{{ $t('detail.deleteDream') }}</button>
    39|        </view>
    40|    </view>
    41|</template>
    42|
    43|<script>
    44|import { dreamApi } from '@/utils/api';
    45|import { requireLogin } from '@/utils/auth';
    46|import { useUserStore } from '@/store/user';
    47|import { useSettingsStore } from '@/store/settings';
    48|
    49|export default {
    50|    components: {
    51|        LangSwitch: () => import('@/components/LangSwitch.vue')
    52|    },
    53|    setup() {
    54|        const settingsStore = useSettingsStore();
    55|        return { settingsStore };
    56|    },
    57|    data() {
    58|        return {
    59|            dreamId: 0,
    60|            dream: null,
    61|            form: {},
    62|            saving: false
    63|        };
    64|    },
    65|    async onLoad(options) {
    66|        if (!requireLogin()) return;
    67|        this.dreamId = parseInt(options.id) || 0;
    68|        try {
    69|            this.dream = await dreamApi.detail(this.dreamId);
    70|            const myId = useUserStore().userId;
    71|            if (this.dream.userId !== myId) {
    72|                uni.showToast({ title: this.$t('detail.noAuthEdit'), icon: 'none' });
    73|                setTimeout(() => uni.navigateBack(), 1500);
    74|                return;
    75|            }
    76|            this.form = {
    77|                location: this.dream.location || '',
    78|                keywords: this.dream.keywords || '',
    79|                clarity: this.dream.clarity || 3,
    80|                description: this.dream.description || '',
    81|                isRecurring: this.dream.isRecurring ? true : false
    82|            };
    83|        } catch (e) {
    84|            console.error('Load dream failed:', e);
    85|        }
    86|    },
    87|    methods: {
    88|        async saveDream() {
    89|            if (!this.form.description.trim()) {
    90|                uni.showToast({ title: this.$t('detail.fillDesc'), icon: 'none' });
    91|                return;
    92|            }
    93|            this.saving = true;
    94|            try {
    95|                const submitData = {...this.form, isRecurring: this.form.isRecurring ? 1 : 0};
    96|                await dreamApi.update(this.dreamId, submitData);
    97|                uni.showToast({ title: this.$t('detail.saveSuccess'), icon: 'success' });
    98|                setTimeout(() => uni.navigateBack(), 1500);
    99|            } catch (e) {
   100|                console.error('Save failed:', e);
   101|            } finally {
   102|                this.saving = false;
   103|            }
   104|        },
   105|        
   106|        async deleteDream() {
   107|            uni.showModal({
   108|                title: this.$t('detail.delete'),
   109|                content: this.$t('detail.deleteConfirm'),
   110|                success: async (res) => {
   111|                    if (res.confirm) {
   112|                        try {
   113|                            await dreamApi.delete(this.dreamId);
   114|                            uni.showToast({ title: this.$t('detail.deleted'), icon: 'success' });
   115|                            setTimeout(() => uni.navigateBack(), 1500);
   116|                        } catch (e) {
   117|                            console.error('Delete failed:', e);
   118|                        }
   119|                    }
   120|                }
   121|            });
   122|        }
   123|    }
   124|};
   125|</script>
   126|
   127|<style lang="scss" scoped>
   128|.edit-page {
   129|    min-height: 100vh;
   130|    padding: 24rpx;
   131|    padding-top: calc(24rpx + env(safe-area-inset-top));
   132|}
   133|
   134|.header-bar {
   135|    display: flex;
   136|    justify-content: space-between;
   137|    align-items: center;
   138|    margin-bottom: 24rpx;
   139|    
   140|    .title {
   141|        font-size: 36rpx;
   142|        font-weight: 700;
   143|        color: $text-primary;
   144|    }
   145|    
   146|    }
   147|
   148|.form-container {
   149|    .form-item {
   150|        background: $glass-card-bg;
   151|        backdrop-filter: blur(20px);
   152|        -webkit-backdrop-filter: blur(20px);
   153|        border: 1rpx solid $glass-card-bg-hover;
   154|        border-radius: 24rpx;
   155|        padding: 24rpx;
   156|        margin-bottom: 20rpx;
   157|        
   158|        .label {
   159|            font-size: 26rpx;
   160|            color: $text-secondary;
   161|            margin-bottom: 12rpx;
   162|            display: block;
   163|        }
   164|        
   165|        .input {
   166|            width: 100%;
   167|            font-size: 28rpx;
   168|            color: $text-primary;
   169|            background: $glass-card-bg-light;
   170|            border-radius: 12rpx;
   171|            padding: 16rpx;
   172|        }
   173|        
   174|        .textarea {
   175|            width: 100%;
   176|            min-height: 240rpx;
   177|            font-size: 28rpx;
   178|            color: $text-primary;
   179|            background: $glass-card-bg-light;
   180|            border-radius: 12rpx;
   181|            padding: 16rpx;
   182|        }
   183|    }
   184|    
   185|    .clarity-picker {
   186|        display: flex;
   187|        gap: 16rpx;
   188|        
   189|        .clarity-item {
   190|            font-size: 48rpx;
   191|            color: $glass-border-active;
   192|            &.active { color: #FDCB6E; }
   193|        }
   194|    }
   195|}
   196|
   197|.submit-btn {
   198|    width: 100%;
   199|    margin-top: 40rpx;
   200|    font-size: 32rpx;
   201|}
   202|
   203|.delete-btn {
   204|    width: 100%;
   205|    margin-top: 20rpx;
   206|    background: $glass-card-bg;
   207|    color: #E17055;
   208|    border: 2rpx solid #E17055;
   209|    border-radius: 48rpx;
   210|    font-size: 28rpx;
   211|}
   212|</style>
   213|