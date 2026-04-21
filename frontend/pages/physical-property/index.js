Page({
  data: {
    physicalPropertyModules: [
      { 
        id: 'elements', 
        name: '元素组成', 
        icon: '🧪', 
        desc: '碳、氢、氧、氮、硫、磷等元素含量'
      },
      { 
        id: 'heat_value', 
        name: '热值', 
        icon: '🔥', 
        desc: '危废的热值特性'
      },
      { 
        id: 'ph_value', 
        name: 'pH', 
        icon: '⚗️', 
        desc: '酸碱度特性'
      },
      { 
        id: 'viscosity',
        name: '粘度',
        icon: '🍯',
        desc: '流体粘度特性'
      },
      { 
        id: 'water_content', 
        name: '含水率', 
        icon: '💧', 
        desc: '水分含量特性'
      },
      { 
        id: 'flash_point', 
        name: '闪点', 
        icon: '⚡', 
        desc: '易燃性指标'
      },
      { 
        id: 'heavy_metals', 
        name: '重金属', 
        icon: '☢️', 
        desc: '有害重金属含量'
      },
      { 
        id: 'alkali_metals', 
        name: '碱金属', 
        icon: '🔹', 
        desc: 'K、Na、Mg等碱金属含量'
      }
    ]
  },

  onLoad() {
    console.log('Physical Property main page loaded');
    wx.setNavigationBarTitle({
      title: '理化特性'
    });
  },

  handleModuleClick(e) {
    const moduleId = e.currentTarget.dataset.id;
    console.log('Navigate to module:', moduleId);
    
    // CORRECT navigation path (singular 'physical-property')
    wx.navigateTo({
      url: `/pages/physical-property/detail/index?category=${moduleId}`,
      success: function() {
        console.log('Navigation successful to:', moduleId);
      },
      fail: function(error) {
        console.error('Navigation failed:', error);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  }
}); 