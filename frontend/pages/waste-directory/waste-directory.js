const { wasteData } = require('../../utils/mockData.js');

Page({
  data: {
    wasteList: [],
    filteredWasteList: [],
    searchKeyword: '',
    loading: false
  },

  onLoad() {
    this.setData({
      wasteList: wasteData,
      filteredWasteList: wasteData
    });
  },

  onShow() {
    // Refresh data when page shows
    this.setData({
      filteredWasteList: this.filterWasteList(this.data.searchKeyword)
    });
  },

  onSearchInput(e) {
    const keyword = e.detail.value;
    this.setData({
      searchKeyword: keyword,
      filteredWasteList: this.filterWasteList(keyword)
    });
  },

  onSearch(e) {
    const keyword = e.detail.value;
    this.setData({
      searchKeyword: keyword,
      filteredWasteList: this.filterWasteList(keyword)
    });
  },

  onSearchClear() {
    this.setData({
      searchKeyword: '',
      filteredWasteList: this.data.wasteList
    });
  },

  // Format storage amount with proper units
  formatStorage(amount) {
    if (amount === 0) {
      return '0.00 kg';
    } else if (amount < 1) {
      return `${(amount * 1000).toFixed(0)} g`;
    } else if (amount >= 1000) {
      // Fix: Use Math.floor to preserve precision and avoid incorrect rounding
      // Convert to tons with 3 decimal places, then truncate to avoid rounding up
      const tons = Math.floor((amount / 1000) * 1000) / 1000;
      return `${tons.toFixed(3)} t`;
    } else {
      return `${amount.toFixed(2)} kg`;
    }
  },

  filterWasteList(keyword) {
    if (!keyword) {
      return this.data.wasteList;
    }
    
    const lowerKeyword = keyword.toLowerCase();
    return this.data.wasteList.filter(waste => {
      return waste.wasteCode.toLowerCase().includes(lowerKeyword) ||
             waste.wasteName.toLowerCase().includes(lowerKeyword) ||
             waste.sourceUnit.toLowerCase().includes(lowerKeyword) ||
             waste.appearance.toLowerCase().includes(lowerKeyword) ||
             waste.harmfulComponents.toLowerCase().includes(lowerKeyword);
    });
  },

  onWasteCardTap(e) {
    const wasteData = e.detail.wasteData;
    
    // Create property tags display
    const propertyTags = [];
    if (wasteData.properties.flammable) propertyTags.push('易燃性');
    if (wasteData.properties.toxic) propertyTags.push('毒性');
    if (wasteData.properties.corrosive) propertyTags.push('腐蚀性');
    if (wasteData.properties.reactive) propertyTags.push('反应性');
    if (wasteData.properties.oxidizing) propertyTags.push('氧化性');
    if (wasteData.properties.volatile) propertyTags.push('挥发性');
    if (wasteData.properties.infectious) propertyTags.push('感染性');
    
    // Show waste detail modal
    wx.showModal({
      title: wasteData.wasteName,
      content: `危废代码：${wasteData.wasteCode}\n来源：${wasteData.sourceUnit}\n库存：${this.formatStorage(wasteData.remainingStorage)}\n热值：${wasteData.heatValue} cal/g\n外观：${wasteData.appearance}\n有害成分：${wasteData.harmfulComponents}\n特性：${propertyTags.join(', ')}`,
      showCancel: false,
      confirmText: '确定'
    });
  },

  onPullDownRefresh() {
    this.setData({
      loading: true
    });
    
    // Simulate API call
    setTimeout(() => {
      this.setData({
        wasteList: wasteData,
        filteredWasteList: this.filterWasteList(this.data.searchKeyword),
        loading: false
      });
      wx.stopPullDownRefresh();
    }, 1000);
  },

  onReachBottom() {
    // Handle pagination if needed
    console.log('Reached bottom');
  }
}); 