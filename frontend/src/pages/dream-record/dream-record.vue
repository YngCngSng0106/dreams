     1|     1|<template>
     2|     2|    <view class="gradient-bg dream-record-page">
     3|     3|        <view class="header-bar">
     4|     4|            <text class="title">{{ $t('record.title') }}</text>
     5|     5|            <text class="subtitle">{{ $t('record.subtitle') }}</text>
     6|     6|            <view class="lang-switch">
     7|     7|                <LangSwitch position="custom" />
     8|     8|            </view>
     9|     9|        </view>
    10|    10|        
    11|    11|        <view class="form-section">
    12|    12|            <!-- 梦境类型 - 加@click打开弹窗 -->
    13|    13|            <view class="form-item picker-row" @click="openCategoryPicker">
    14|    14|                <text class="label">{{ $t('record.dreamType') }}</text>
    15|    15|                <text class="value">{{ currentCategory || $t('record.pleaseSelect') }}</text>
    16|    16|                <text class="arrow">›</text>
    17|    17|            </view>
    18|    18|            
    19|    19|            <!-- 做梦日期 - 用picker组件 -->
    20|    20|            <view class="form-item picker-row">
    21|    21|                <text class="label">{{ $t('record.dreamDate') }}</text>
    22|    22|                <picker mode="date" :start="dateStart" :end="dateEnd" :value="dreamDate" @change="onDateChange">
    23|    23|                    <text class="value">{{ dreamDate || $t('record.pleaseSelect') }}</text>
    24|    24|                </picker>
    25|    25|                <text class="arrow">›</text>
    26|    26|            </view>
    27|    27|            
    28|    28|            <!-- 发生地点 -->
    29|    29|            <view class="form-item">
    30|    30|                <text class="label">{{ $t('record.location') }}</text>
    31|    31|                <input class="input" v-model="location" :placeholder="$t('record.locationPlaceholder')" placeholder-class="input-placeholder" />
    32|    32|            </view>
    33|    33|            
    34|    34|            <!-- 关键词 -->
    35|    35|            <view class="form-item">
    36|    36|                <text class="label">{{ $t('record.keywords') }}</text>
    37|    37|                <input class="input" v-model="keywords" :placeholder="$t('record.keywordsPlaceholder')" placeholder-class="input-placeholder" />
    38|    38|            </view>
    39|    39|            
    40|    40|            <!-- 清晰度 -->
    41|    41|            <view class="form-item">
    42|    42|                <text class="label">{{ $t('record.clarity') }}</text>
    43|    43|                <view class="clarity-options">
    44|    44|                    <text class="clarity-btn" :class="{active: clarity===1}" @click.stop="clarity=1">{{ $t('record.low') }}</text>
    45|    45|                    <text class="clarity-btn" :class="{active: clarity===2}" @click.stop="clarity=2">{{ $t('record.mid') }}</text>
    46|    46|                    <text class="clarity-btn" :class="{active: clarity===3}" @click.stop="clarity=3">{{ $t('record.high') }}</text>
    47|    47|                </view>
    48|    48|            </view>
    49|    49|            
    50|    50|            <!-- 梦境描述 -->
    51|    51|            <view class="form-item">
    52|    52|                <text class="label">{{ $t('record.description') }}</text>
    53|    53|                <textarea class="textarea" v-model="description" :placeholder="$t('record.descriptionPlaceholder')" maxlength="2000" />
    54|    54|            </view>
    55|    55|            
    56|    56|            <!-- 重复梦 -->
    57|    57|            <view class="form-item">
    58|    58|                <text class="label">{{ $t('record.isRecurring') }}</text>
    59|    59|                <switch :checked="isRecurring" @change="isRecurring=$event.detail.value" color="#6C5CE7" />
    60|    60|            </view>
    61|    61|            
    62|    62|            <!-- 上传图片 -->
    63|    63|            <view class="form-item" v-if="imageList.length > 0 || imageList.length < 3">
    64|    64|                <text class="label">{{ $t('record.uploadImage') }}</text>
    65|    65|            <view class="image-list">
    66|    66|                <view class="image-item" v-for="(img, index) in imageList" :key="index">
    67|    67|                    <image :src="img" mode="aspectFill" class="preview-img" />
    68|    68|                    <text class="delete-icon" @click.stop="removeImage(index)">✕</text>
    69|    69|                </view>
    70|    70|                <view class="upload-btn" v-if="imageList.length < 3" @click="uploadImage">+</view>
    71|    71|            </view>
    72|    72|            </view>
    73|    73|            
    74|    74|            <button class="publish-btn" @click="submitDream" :loading="submitting">
    75|    75|                {{ $t('record.publish') }}
    76|    76|            </button>
    77|    77|        </view>
    78|    78|        
    79|    79|        <!-- 分类选择弹窗 -->
    80|    80|        <view class="category-modal" v-if="showCategory" @click="showCategory=false">
    81|    81|            <view class="modal-content" @click.stop>
    82|    82|                <text class="modal-title">{{ $t('record.dreamType') }}</text>
    83|    83|                <view class="category-item" v-for="cat in categories" :key="cat.id" @click="selectCategory(cat)">
    84|    84|                    <text class="cat-icon">{{ cat.icon || '🔮' }}</text>
    85|    85|                    <text class="cat-name">{{ cat.name }}</text>
    86|    86|                </view>
    87|    87|                <view class="modal-close" @click="showCategory=false">{{ $t('record.cancel') }}</view>
    88|    88|            </view>
    89|    89|        </view>
    90|    90|    </view>
    91|    91|
    92|    92|    <custom-tab-bar ref="tabbar" />
    93|    93|</template>
    94|    94|
    95|    95|<script>
    96|    96|import { dreamApi, categoryApi } from '@/utils/api';
    97|    97|import { requireLogin } from '@/utils/auth';
    98|    98|import { uploadFile } from '@/utils/request';
    99|    99|import { useSettingsStore } from '@/store/settings';
   100|   100|import LangSwitch from '@/components/LangSwitch.vue';
   101|   101|
   102|   102|export default {
   103|   103|    components: {
   104|   104|        LangSwitch
   105|   105|    },
   106|   106|    setup() {
   107|   107|        const settingsStore = useSettingsStore();
   108|   108|        return { settingsStore };
   109|   109|    },
   110|   110|    data() {
   111|   111|        const now = new Date();
   112|   112|        const yyyy = now.getFullYear();
   113|   113|        return {
   114|   114|            dreamType: '',
   115|   115|            currentCategory: '',
   116|   116|            dreamDate: `${yyyy}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`,
   117|   117|            dateStart: `${yyyy-1}-01-01`,
   118|   118|            dateEnd: `${yyyy+1}-12-31`,
   119|   119|            location: '',
   120|   120|            keywords: '',
   121|   121|            clarity: 2,
   122|   122|            description: '',
   123|   123|            isRecurring: false,
   124|   124|            imageList: [],        // 本地临时路径
   125|   125|            uploadedImageUrls: [], // 已上传到服务器的URL
   126|   126|            categories: [],
   127|   127|            showCategory: false,
   128|   128|            submitting: false
   129|   129|        };
   130|   130|    },
   131|   131|    async onLoad() {
   132|   132|        if (!requireLogin()) return;
   133|   133|        await this.loadCategories();
   134|   134|    },
   135|   135|    onShow() {
   136|   136|        this.$nextTick(() => {
   137|   137|            const tabbar = this.$refs.tabbar;
   138|   138|            if (tabbar) tabbar.updateCurrentPage();
   139|   139|        });
   140|   140|    },
   141|   141|    methods: {
   142|   142|        async loadCategories() {
   143|   143|            try {
   144|   144|                this.categories = await categoryApi.list();
   145|   145|                if (this.categories.length > 0) {
   146|   146|                    this.currentCategory = this.categories[0].name;
   147|   147|                    this.dreamType = String(this.categories[0].id);
   148|   148|                }
   149|   149|            } catch (e) {
   150|   150|                console.error('Load categories failed:', e);
   151|   151|            }
   152|   152|        },
   153|   153|        openCategoryPicker() {
   154|   154|            this.showCategory = true;
   155|   155|        },
   156|   156|        onDateChange(e) {
   157|   157|            this.dreamDate = e.detail.value;
   158|   158|        },
   159|   159|        selectCategory(cat) {
   160|   160|            this.currentCategory = cat.name;
   161|   161|            this.dreamType = String(cat.id);
   162|   162|            this.showCategory = false;
   163|   163|        },
   164|   164|        uploadImage() {
   165|   165|            uni.chooseImage({
   166|   166|                count: 3 - this.imageList.length,
   167|   167|                success: (res) => {
   168|   168|                    this.imageList.push(...res.tempFilePaths);
   169|   169|                }
   170|   170|            });
   171|   171|        },
   172|   172|        removeImage(index) {
   173|   173|            this.imageList.splice(index, 1);
   174|   174|        },
   175|   175|        async uploadImages() {
   176|   176|            // 批量上传图片到服务器
   177|   177|            const urls = [];
   178|   178|            for (const filePath of this.imageList) {
   179|   179|                try {
   180|   180|                    const result = await uploadFile(filePath);
   181|   181|                    urls.push(result.url);
   182|   182|                } catch (e) {
   183|   183|                    console.error('Upload failed:', e);
   184|   184|                }
   185|   185|            }
   186|   186|            return urls;
   187|   187|        },
   188|   188|        async submitDream() {
   189|   189|            if (!this.description) {
   190|   190|                uni.showToast({ title: this.$t('record.fillDesc'), icon: 'none' });
   191|   191|                return;
   192|   192|            }
   193|   193|            if (!this.currentCategory) {
   194|   194|                uni.showToast({ title: this.$t('record.selectType'), icon: 'none' });
   195|   195|                return;
   196|   196|            }
   197|   197|            this.submitting = true;
   198|   198|            try {
   199|   199|                // 先上传图片（如果有）
   200|   200|                let imagesJson = null;
   201|   201|                if (this.imageList.length > 0) {
   202|   202|                    uni.showLoading({ title: '上传图片中...', mask: true });
   203|   203|                    const uploadedUrls = await this.uploadImages();
   204|   204|                    uni.hideLoading();
   205|   205|                    if (uploadedUrls.length > 0) {
   206|   206|                        imagesJson = JSON.stringify(uploadedUrls);
   207|   207|                    }
   208|   208|                }
   209|   209|
   210|   210|                // 构建提交数据 — 匹配后端 DreamCreateRequest 字段
   211|   211|                const submitData = {
   212|   212|                    categoryId: this.dreamType ? parseInt(this.dreamType) : null,
   213|   213|                    description: this.description,
   214|   214|                    dreamDate: this.dreamDate,
   215|   215|                    location: this.location || null,
   216|   216|                    keywords: this.keywords || null,  // 保持字符串格式
   217|   217|                    clarity: this.clarity,
   218|   218|                    isRecurring: this.isRecurring ? 1 : 0,
   219|   219|                    images: imagesJson
   220|   220|                };
   221|   221|
   222|   222|                await dreamApi.create(submitData);
   223|   223|                uni.showToast({ title: this.$t('record.publishSuccess'), icon: 'success' });
   224|   224|                setTimeout(() => {
   225|   225|                    uni.navigateBack();
   226|   226|                }, 1500);
   227|   227|            } catch (e) {
   228|   228|                console.error('Submit dream failed:', e);
   229|   229|            } finally {
   230|   230|                this.submitting = false;
   231|   231|            }
   232|   232|        }
   233|   233|    }
   234|   234|};
   235|   235|</script>
   236|   236|
   237|   237|<style lang="scss" scoped>
   238|   238|.dream-record-page {
   239|   239|    min-height: 100vh;
   240|   240|    padding-bottom: 120rpx;
   241|   241|}
   242|   242|
   243|   243|.header-bar {
   244|   244|    padding: 40rpx;
   245|   245|    text-align: center;
   246|   246|    position: relative;
   247|   247|    
   248|   248|    .lang-switch {
   249|   249|        position: absolute;
   250|   250|        top: 20rpx;
   251|   251|        right: 20rpx;
   252|   252|    }
   253|   253|    
   254|   254|    .lang-btn {
   255|   255|        font-size: 24rpx;
   256|   256|        color: $text-primary;
   257|   257|        background: $glass-card-bg;
   258|   258|        backdrop-filter: blur(20px);
   259|   259|        -webkit-backdrop-filter: blur(20px);
   260|   260|        padding: 8rpx 16rpx;
   261|   261|        border-radius: 12rpx;
   262|   262|    }
   263|   263|    
   264|   264|    .title {
   265|   265|        display: block;
   266|   266|        font-size: 40rpx;
   267|   267|        color: $text-primary;
   268|   268|        font-weight: bold;
   269|   269|        margin-bottom: 12rpx;
   270|   270|    }
   271|   271|    
   272|   272|    .subtitle {
   273|   273|        font-size: 28rpx;
   274|   274|        color: $text-secondary;
   275|   275|    }
   276|   276|}
   277|   277|
   278|   278|.form-section {
   279|   279|    padding: 0 32rpx;
   280|   280|}
   281|   281|
   282|   282|.form-item {
   283|   283|    background: $glass-card-bg;
   284|   284|    backdrop-filter: blur(20px);
   285|   285|    -webkit-backdrop-filter: blur(20px);
   286|   286|    border: 1rpx solid $glass-card-bg-hover;
   287|   287|    border-radius: 16rpx;
   288|   288|    padding: 24rpx 32rpx;
   289|   289|    margin-bottom: 24rpx;
   290|   290|    display: flex;
   291|   291|    align-items: center;
   292|   292|    
   293|   293|    .label {
   294|   294|        font-size: 30rpx;
   295|   295|        color: $text-primary;
   296|   296|        width: 160rpx;
   297|   297|        flex-shrink: 0;
   298|   298|    }
   299|   299|    
   300|   300|    .value {
   301|   301|        flex: 1;
   302|   302|        font-size: 28rpx;
   303|   303|        color: $text-primary;
   304|   304|    }
   305|   305|    
   306|   306|    .arrow {
   307|   307|        font-size: 32rpx;
   308|   308|        color: $text-tertiary;
   309|   309|    }
   310|   310|    
   311|   311|    .input {
   312|   312|        flex: 1;
   313|   313|        font-size: 28rpx;
   314|   314|        color: $text-primary;
   315|   315|    }
   316|   316|    
   317|   317|    .textarea {
   318|   318|        flex: 1;
   319|   319|        font-size: 28rpx;
   320|   320|        color: $text-primary;
   321|   321|        min-height: 200rpx;
   322|   322|        &::placeholder {
   323|   323|            color: $text-tertiary;
   324|   324|        }
   325|   325|    }
   326|   326|    
   327|   327|    .clarity-options {
   328|   328|        display: flex;
   329|   329|        gap: 20rpx;
   330|   330|        
   331|   331|        .clarity-btn {
   332|   332|            padding: 8rpx 24rpx;
   333|   333|            border-radius: 24rpx;
   334|   334|            font-size: 26rpx;
   335|   335|            color: $text-secondary;
   336|   336|            border: 2rpx solid transparent;
   337|   337|            &.active {
   338|   338|                background: #6C5CE7;
   339|   339|                color: $text-primary;
   340|   340|                border-color: $text-tertiary;
   341|   341|            }
   342|   342|        }
   343|   343|    }
   344|   344|    
   345|   345|    .image-list {
   346|   346|        display: flex;
   347|   347|        flex-wrap: wrap;
   348|   348|        gap: 16rpx;
   349|   349|        
   350|   350|        .image-item {
   351|   351|            position: relative;
   352|   352|            width: 160rpx;
   353|   353|            height: 160rpx;
   354|   354|            
   355|   355|            .preview-img {
   356|   356|                width: 100%;
   357|   357|                height: 100%;
   358|   358|                border-radius: 12rpx;
   359|   359|            }
   360|   360|            
   361|   361|            .delete-icon {
   362|   362|                position: absolute;
   363|   363|                top: -10rpx;
   364|   364|                right: -10rpx;
   365|   365|                width: 40rpx;
   366|   366|                height: 40rpx;
   367|   367|                background: $glass-overlay;
   368|   368|                border-radius: 50%;
   369|   369|                text-align: center;
   370|   370|                line-height: 40rpx;
   371|   371|                font-size: 24rpx;
   372|   372|                color: $text-primary;
   373|   373|            }
   374|   374|        }
   375|   375|        
   376|   376|        .upload-btn {
   377|   377|            width: 160rpx;
   378|   378|            height: 160rpx;
   379|   379|            border: 2rpx dashed $text-tertiary;
   380|   380|            border-radius: 12rpx;
   381|   381|            display: flex;
   382|   382|            align-items: center;
   383|   383|            justify-content: center;
   384|   384|            font-size: 48rpx;
   385|   385|            color: $text-secondary;
   386|   386|        }
   387|   387|    }
   388|   388|}
   389|   389|
   390|   390|.publish-btn {
   391|   391|    width: 100%;
   392|   392|    height: 88rpx;
   393|   393|    line-height: 88rpx;
   394|   394|    background: $primary-color;
   395|   395|    backdrop-filter: blur(20px);
   396|   396|    -webkit-backdrop-filter: blur(20px);
   397|   397|    color: $text-primary;
   398|   398|    font-size: 34rpx;
   399|   399|    border-radius: 44rpx;
   400|   400|    margin-top: 40rpx;
   401|   401|    letter-spacing: 4rpx;
   402|   402|    border: 1rpx solid $glass-card-bg-hover;
   403|   403|}
   404|   404|
   405|   405|.category-modal {
   406|   406|    position: fixed;
   407|   407|    top: 0;
   408|   408|    left: 0;
   409|   409|    right: 0;
   410|   410|    bottom: 0;
   411|   411|    background: $glass-overlay;
   412|   412|    display: flex;
   413|   413|    align-items: flex-end;
   414|   414|    z-index: 100;
   415|   415|    
   416|   416|    .modal-content {
   417|   417|        background: $glass-card-bg;
   418|   418|        backdrop-filter: blur(20px);
   419|   419|        -webkit-backdrop-filter: blur(20px);
   420|   420|        border: 1rpx solid $glass-card-bg-hover;
   421|   421|        border-radius: 24rpx 24rpx 0 0;
   422|   422|        padding: 32rpx;
   423|   423|        max-height: 60vh;
   424|   424|        overflow-y: auto;
   425|   425|        width: 100%;
   426|   426|        
   427|   427|        .modal-title {
   428|   428|            display: block;
   429|   429|            text-align: center;
   430|   430|            font-size: 34rpx;
   431|   431|            font-weight: bold;
   432|   432|            color: $text-primary;
   433|   433|            margin-bottom: 24rpx;
   434|   434|        }
   435|   435|        
   436|   436|        .category-item {
   437|   437|            display: flex;
   438|   438|            align-items: center;
   439|   439|            padding: 20rpx 16rpx;
   440|   440|            border-bottom: 1rpx solid $glass-navbar-bg;
   441|   441|            
   442|   442|            .cat-icon {
   443|   443|                font-size: 40rpx;
   444|   444|                margin-right: 16rpx;
   445|   445|            }
   446|   446|            
   447|   447|            .cat-name {
   448|   448|                font-size: 30rpx;
   449|   449|                color: $text-primary;
   450|   450|            }
   451|   451|            
   452|   452|            &:last-child {
   453|   453|                border-bottom: none;
   454|   454|            }
   455|   455|        }
   456|   456|        
   457|   457|        .modal-close {
   458|   458|            text-align: center;
   459|   459|            padding: 24rpx;
   460|   460|            margin-top: 16rpx;
   461|   461|            font-size: 28rpx;
   462|   462|            color: #6C5CE7;
   463|   463|            background: $primary-color;
   464|   464|            border-radius: 16rpx;
   465|   465|        }
   466|   466|    }
   467|   467|}
   468|   468|
   469|   469|.input-placeholder {
   470|   470|    color: $text-tertiary !important;
   471|   471|}
   472|   472|</style>
   473|   473|