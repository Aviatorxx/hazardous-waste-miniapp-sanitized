Page({
  data: {
    wasteDetail: {},
    propertyList: []
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.loadWasteDetail(id);
    }
  },

  loadWasteDetail(id) {
    wx.showLoading({ title: '加载中...' });

    wx.request({
      url: require('../../../config/api-config.js').getFullUrl(`/api/v1/waste-directory/detail/${id}`),
      method: 'GET',
      success: (res) => {
        wx.hideLoading();
        if (res.data.success) {
          const wasteInfo = res.data.data.wasteInfo;
          
          // Process storage info
          const storage = parseFloat(wasteInfo.remainingStorage) || 0;
          const storageInfo = this.getStorageDisplayInfo(storage);
          
          // Process properties list for better display
          const propertyList = [
            { key: 'oxidizing', name: '氧化性', icon: '🔥', value: wasteInfo.oxidizing },
            { key: 'reducing', name: '还原性', icon: '⚡', value: wasteInfo.reducing },
            { key: 'volatile', name: '挥发性', icon: '💨', value: wasteInfo.volatileProperty },
            { key: 'flammable', name: '易燃性', icon: '🔥', value: wasteInfo.flammable },
            { key: 'toxic', name: '毒性', icon: '☠️', value: wasteInfo.toxic },
            { key: 'reactive', name: '反应性', icon: '⚡', value: wasteInfo.reactive },
            { key: 'infectious', name: '感染性', icon: '🦠', value: wasteInfo.infectious },
            { key: 'corrosive', name: '腐蚀性', icon: '⚗️', value: wasteInfo.corrosive },
            { key: 'halogenated', name: '卤化烃类', icon: '🧪', value: wasteInfo.halogenatedHydrocarbon },
            { key: 'cyanide', name: '含氰化物废物', icon: '☠️', value: wasteInfo.cyanideContaining }
          ];
          
          const processedWasteInfo = {
            ...wasteInfo,
            remainingStorage: storage,
            remainingStorageDisplay: this.formatStorage(storage),
            storageStatus: storageInfo.status,
            storageBgColor: storageInfo.bgColor,
            storageTextColor: storageInfo.textColor,
            storageBorderColor: storageInfo.borderColor
          };
          
          this.setData({
            wasteDetail: processedWasteInfo,
            propertyList: propertyList
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
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

  goBack() {
    console.log('Going back to list page');
    wx.navigateBack({
      delta: 1
    });
  }
}); 