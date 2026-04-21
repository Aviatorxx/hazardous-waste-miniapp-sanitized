// app.js
App({
  onLaunch() {
    console.log('危废热处理智库小程序启动');
    
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync();
    this.globalData.systemInfo = systemInfo;
    
    // 初始化全局数据
    this.globalData.selectedWastes = [];
    
    // Initialize session management
    this.initializeSessionManagement();
    
    // 检查更新
    if (wx.canIUse('getUpdateManager')) {
      const updateManager = wx.getUpdateManager();
      updateManager.onCheckForUpdate((result) => {
        if (result.hasUpdate) {
          console.log('发现新版本');
        }
      });
    }
  },

  // Initialize session management
  initializeSessionManagement() {
    console.log('=== INITIALIZING SESSION MANAGEMENT ===');
    
    // Check for existing session in storage
    const storedSessionId = wx.getStorageSync('currentSessionId');
    const sessionCreatedAt = wx.getStorageSync('sessionCreatedAt');
    
    console.log('Stored session data:', {
      sessionId: storedSessionId,
      createdAt: sessionCreatedAt
    });
    
    // Check if session is too old (more than 24 hours)
    const now = Date.now();
    const maxAge = 24 * 60 * 60 * 1000; // 24 hours
    
    if (storedSessionId && sessionCreatedAt) {
      const age = now - sessionCreatedAt;
      if (age > maxAge) {
        console.log('Session is too old, clearing:', age / (60 * 60 * 1000), 'hours');
        this.clearStoredSession();
      } else {
        console.log('Restoring session from storage:', storedSessionId);
        this.globalData.currentSessionId = storedSessionId;
      }
    } else {
      console.log('No valid session found in storage');
      this.globalData.currentSessionId = null;
    }
  },

  // Clear stored session data
  clearStoredSession() {
    console.log('Clearing stored session data');
    this.globalData.currentSessionId = null;
    wx.removeStorageSync('currentSessionId');
    wx.removeStorageSync('sessionCreatedAt');
  },

  onShow() {
    console.log('小程序显示');
  },

  onHide() {
    console.log('小程序隐藏');
  },

  onError(error) {
    console.error('小程序错误:', error);
  },

  globalData: {
    userInfo: null,
    systemInfo: null,
    selectedWastes: [],
    currentSessionId: null,
    appName: '危废热处理智库',
    version: '1.0.0'
  }
});
