Page({
  data: {
    modules: [
      {
        id: 'spectral-fingerprint',
        name: '光谱指纹',
        desc: '通过光谱分析识别危废成分特征，建立独特的"指纹"标识',
        icon: '🌈',
        path: '/pages/thermal-properties/spectral-fingerprint/spectral-fingerprint'
      },
      {
        id: 'thermal-behavior',
        name: '热行为指纹',
        desc: '分析危废在不同温度下的热行为特性和反应规律',
        icon: '🔬',
        path: '/pages/thermal-properties/thermal-behavior/thermal-behavior'
      }
    ]
  },

  onLoad() {
    wx.setNavigationBarTitle({
      title: '热力学特性'
    });
  },

  onModuleTap(e) {
    const module = e.currentTarget.dataset.module;
    
    wx.navigateTo({
      url: module.path,
      fail: (error) => {
        wx.showToast({
          title: '页面开发中',
          icon: 'none'
        });
      }
    });
  },

  goToSpectrum() {
    wx.navigateTo({
      url: '/pages/thermal-properties/spectrum/spectrum?type=FTIR'
    });
  },
  
  goToThermal() {
    wx.navigateTo({
      url: '/pages/thermal-properties/thermal/thermal?type=TG-DSC'
    });
  }
}); 