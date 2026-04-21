Page({
  data: {
    wasteList: [],
    currentPage: 1,
    pageSize: 20,
    total: 0,
    hasMore: true,
    loading: false,
    searchKeyword: ''
  },

  onLoad() {
    console.log('Loading waste directory...');
    this.loadWasteList(true);
  },

  onPullDownRefresh() {
    this.loadWasteList(true);
    setTimeout(() => {
      wx.stopPullDownRefresh();
    }, 1000);
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ currentPage: this.data.currentPage + 1 }, () => {
        this.loadWasteList(false);
      });
    }
  },

  onShow() {
    console.log('List page shown');
  },

  onReady() {
    console.log('List page ready');
  },

  // REAL API CALL - Remove test data
  loadWasteList(isRefresh = false) {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    const page = isRefresh ? 1 : this.data.currentPage;
    
    wx.request({
              url: require('../../../config/api-config.js').getFullUrl('/api/v1/waste-directory/list'),
      method: 'GET',
      data: {
        current: page,
        size: this.data.pageSize,
        keyword: this.data.searchKeyword
      },
      success: (res) => {
        console.log('API Response:', res.data);
        if (res.data.success) {
          const processedList = this.processWasteList(res.data.data.records);
          const newList = isRefresh ? processedList : [...this.data.wasteList, ...processedList];
          
          this.setData({
            wasteList: newList,
            currentPage: page,
            total: res.data.data.total,
            hasMore: newList.length < res.data.data.total,
            loading: false
          });
        }
      },
      fail: (err) => {
        console.error('Failed to load waste list:', err);
        this.setData({ loading: false });
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    });
  },

  processWasteList(wasteList) {
    return wasteList.map(item => {
      const storage = parseFloat(item.remainingStorage) || 0;
      const storageInfo = this.getStorageDisplayInfo(storage);
      
      // Process properties for tags
      const properties = [];
      if (item.flammable) properties.push('flammable');
      if (item.toxic) properties.push('toxic');
      if (item.corrosive) properties.push('corrosive');
      if (item.oxidizing) properties.push('oxidizing');
      if (item.volatileProperty) properties.push('volatile');
      if (item.reactive) properties.push('reactive');
      
      return {
        ...item,
        remainingStorage: storage,
        remainingStorageDisplay: this.formatStorage(storage),
        storageStatus: storageInfo.status,
        storageBgColor: storageInfo.bgColor,
        storageTextColor: storageInfo.textColor,
        storageBorderColor: storageInfo.borderColor,
        properties: properties
      };
    });
  },

  getStorageDisplayInfo(remainingStorage) {
    const storage = parseFloat(remainingStorage) || 0;
    
    if (storage === 0) {
      return {
        bgColor: '#f5f5f5',
        textColor: '#999',
        borderColor: '#e0e0e0',
        status: 'empty'
      };
    } else if (storage < 100) {
      return {
        bgColor: '#fff3e0',
        textColor: '#f57c00',
        borderColor: '#ffb74d',
        status: 'low'
      };
    } else {
      return {
        bgColor: '#e3f2fd',
        textColor: '#1976d2',
        borderColor: '#64b5f6',
        status: 'high'
      };
    }
  },

  formatStorage(amount) {
    if (amount === 0) {
      return '0.00 kg';
    } else if (amount < 1) {
      return `${(amount * 1000).toFixed(0)} g`;
    } else if (amount >= 1000) {
      const tons = Math.floor((amount / 1000) * 1000) / 1000;
      return `${tons.toFixed(3)} t`;
    } else {
      return `${amount.toFixed(2)} kg`;
    }
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearchConfirm() {
    this.loadWasteList(true);
  },

  onWasteItemTap(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/waste/detail/detail?id=${id}`
    });
  }
}); 