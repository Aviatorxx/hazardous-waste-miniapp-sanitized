Page({
  data: {},
  
  onLoad() {
    wx.setNavigationBarTitle({
      title: '热力学特性'
    });
  },
  
  goToSpectrum() {
    wx.navigateTo({
      url: '/pages/thermal-properties/spectrum/spectrum?type=FTIR&title=FTIR红外光谱'
    });
  },
  
  goToThermal() {
    wx.navigateTo({
      url: '/pages/thermal-properties/thermal/thermal?type=TG-DSC&title=TGA-DSC热分析'
    });
  }
}); 