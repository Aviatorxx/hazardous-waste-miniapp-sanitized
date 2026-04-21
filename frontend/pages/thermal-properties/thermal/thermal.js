Page({
  data: {
    spectrumType: '',
    pageTitle: '',
    keyword: '',
    wasteList: [],
    loading: false,
    showImageViewer: false,
    currentImage: null,
    emptyMessage: '暂无数据'
  },
  
  onLoad(options) {
    const type = options.type || 'TG-DSC';
    const title = options.title || 'TGA-DSC热分析';
    
    this.setData({
      spectrumType: type,
      pageTitle: title
    });
    
    wx.setNavigationBarTitle({
      title: title
    });
    
    this.loadData();
  },
  
  onKeywordInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },
  
  onSearch() {
    this.loadData();
  },
  
  clearSearch() {
    this.setData({
      keyword: '',
      emptyMessage: '暂无数据'
    });
    this.loadData();
  },
  
  loadData() {
    if (this.data.loading) return;
    
    this.setData({ 
      loading: true,
      emptyMessage: '暂无数据'
    });
    
          const API_CONFIG = require('../../../config/api-config.js');
      const url = `${API_CONFIG.getFullUrl('/api/thermal-properties')}/search?spectrumType=${this.data.spectrumType}&keyword=${encodeURIComponent(this.data.keyword)}&pageNum=1&pageSize=20`;
    
    wx.request({
      url: url,
      method: 'GET',
      success: (res) => {
        console.log('API Response:', res);
        
        if (res.statusCode === 200 && res.data.success) {
          const records = res.data.data.records || [];
          
          // Process data to add image URLs
          const processedData = records.map(item => ({
            ...item,
            images: (item.images || []).map(image => ({
              ...image,
              imageUrl: API_CONFIG.getImageUrl(this.data.spectrumType, image.fileName)
            }))
          }));
          
          this.setData({
            wasteList: processedData,
            emptyMessage: this.data.keyword ? '未找到相关数据' : '暂无数据'
          });
        } else {
          console.error('API Error:', res.data);
          this.setData({
            wasteList: [],
            emptyMessage: '数据加载失败'
          });
          
          wx.showToast({
            title: '数据加载失败',
            icon: 'none',
            duration: 2000
          });
        }
      },
      fail: (err) => {
        console.error('Request Failed:', err);
        this.setData({
          wasteList: [],
          emptyMessage: '网络连接失败'
        });
        
        wx.showToast({
          title: '网络连接失败',
          icon: 'none',
          duration: 2000
        });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },
  
  // Enhanced image preview with native WeChat API
  onImageTap(e) {
    const { image, wasteIndex } = e.currentTarget.dataset;
    
    // Build image URLs array for the current waste item
    const currentWaste = this.data.wasteList[wasteIndex];
    const imageUrls = this.buildImageUrlsArray(currentWaste);
    
    // Use WeChat native preview for better user experience
    wx.previewImage({
      current: image.imageUrl,
      urls: imageUrls,
      success: () => {
        console.log('TG-DSC image preview opened successfully');
      },
      fail: (err) => {
        console.error('TG-DSC preview failed:', err);
        // Fallback to custom modal if native preview fails
        this.viewImageFallback(image);
      }
    });
  },
  
  // Build image URLs array for preview browsing
  buildImageUrlsArray(wasteItem) {
    if (!wasteItem || !wasteItem.images) return [];
    return wasteItem.images.map(img => img.imageUrl).filter(url => url);
  },
  
  // Enhanced method to preview all images in the dataset
  previewAllImages(e) {
    const { startImageUrl } = e.currentTarget.dataset;
    
    // Collect all image URLs from all waste items
    const allImageUrls = [];
    this.data.wasteList.forEach(waste => {
      if (waste.images && waste.images.length > 0) {
        waste.images.forEach(img => {
          if (img.imageUrl) {
            allImageUrls.push(img.imageUrl);
          }
        });
      }
    });
    
    if (allImageUrls.length === 0) {
      wx.showToast({
        title: '暂无图片数据',
        icon: 'none'
      });
      return;
    }
    
    wx.previewImage({
      current: startImageUrl || allImageUrls[0],
      urls: allImageUrls,
      success: () => {
        console.log('All TG-DSC images preview opened');
      },
      fail: (err) => {
        console.error('All TG-DSC images preview failed:', err);
        wx.showToast({
          title: '图片预览失败',
          icon: 'error'
        });
      }
    });
  },
  
  // Legacy method kept for fallback compatibility
  viewImage(e) {
    this.onImageTap(e);
  },
  
  // Fallback modal viewer (kept for compatibility)
  viewImageFallback(image) {
    this.setData({
      currentImage: {
        ...image,
        wasteCode: image.wasteCode,
        wasteName: image.wasteName,
        testName: image.testName,
        imageUrl: image.imageUrl,
        remark: image.remark || ''
      },
      showImageViewer: true
    });
    
    wx.showToast({
      title: '已使用备用查看器',
      icon: 'none',
      duration: 1500
    });
  },
  
  closeImageViewer() {
    this.setData({
      showImageViewer: false,
      currentImage: null
    });
  },
  
  onImageError(e) {
    console.error('Image load error:', e);
  },
  
  onModalImageError(e) {
    console.error('Modal image load error:', e);
    wx.showToast({
      title: '图片加载失败',
      icon: 'none'
    });
  },
  
  onPullDownRefresh() {
    this.loadData();
    setTimeout(() => {
      wx.stopPullDownRefresh();
    }, 1000);
  },

  // Scroll to top functionality for floating action button
  scrollToTop() {
    wx.pageScrollTo({
      scrollTop: 0,
      duration: 300
    });
  }
}); 