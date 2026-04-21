// Session Debug Utility for WeChat MiniProgram
// Helps diagnose session-related issues in the compatibility simulation module

const SessionDebug = {
  // Enable/disable debug logging
  enabled: true,

  // Log session state
  logSessionState(context = 'Unknown') {
    if (!this.enabled) return;

    console.log(`=== SESSION STATE DEBUG [${context}] ===`);
    
    try {
      const app = getApp();
      const localSessionId = wx.getStorageSync('currentSessionId');
      const sessionCreatedAt = wx.getStorageSync('sessionCreatedAt');
      const globalSessionId = app?.globalData?.currentSessionId;
      
      const sessionAge = sessionCreatedAt ? (Date.now() - sessionCreatedAt) / (60 * 60 * 1000) : null;
      
      console.log('Session IDs:', {
        global: globalSessionId,
        storage: localSessionId,
        match: globalSessionId === localSessionId
      });
      
      console.log('Session timing:', {
        createdAt: sessionCreatedAt ? new Date(sessionCreatedAt).toISOString() : null,
        ageHours: sessionAge?.toFixed(2),
        isExpired: sessionAge > 24
      });
      
      console.log('Storage status:', {
        hasSessionId: !!localSessionId,
        hasTimestamp: !!sessionCreatedAt,
        globalDataExists: !!app?.globalData
      });
      
    } catch (error) {
      console.error('Session debug logging failed:', error);
    }
    
    console.log('=== END SESSION STATE ===');
  },

  // Validate session consistency
  validateSessionConsistency() {
    if (!this.enabled) return true;

    try {
      const app = getApp();
      const localSessionId = wx.getStorageSync('currentSessionId');
      const globalSessionId = app?.globalData?.currentSessionId;
      
      const isConsistent = localSessionId === globalSessionId;
      
      if (!isConsistent) {
        console.warn('SESSION INCONSISTENCY DETECTED:', {
          global: globalSessionId,
          storage: localSessionId
        });
      }
      
      return isConsistent;
      
    } catch (error) {
      console.error('Session consistency check failed:', error);
      return false;
    }
  },

  // Check if session is valid
  async checkSessionValidity(sessionId) {
    if (!this.enabled) return true;

    console.log(`=== CHECKING SESSION VALIDITY: ${sessionId} ===`);
    
    if (!sessionId) {
      console.log('Session ID is empty or null');
      return false;
    }
    
    try {
      // Import API here to avoid circular dependencies
      const { sessionAPI } = require('./matching-api.js');
      
      const startTime = Date.now();
      const response = await sessionAPI.getSessionStatus(sessionId);
      const duration = Date.now() - startTime;
      
      console.log('Session validation response:', {
        sessionId: sessionId,
        responseTime: duration + 'ms',
        success: !!response,
        data: response
      });
      
      return !!response;
      
    } catch (error) {
      console.error('Session validation failed:', error);
      return false;
    }
  },

  // Log API call attempts
  logApiCall(apiName, sessionId, additionalData = {}) {
    if (!this.enabled) return;

    console.log(`=== API CALL: ${apiName} ===`);
    console.log('Session ID:', sessionId);
    console.log('Additional data:', additionalData);
    console.log('Timestamp:', new Date().toISOString());
  },

  // Log error details
  logError(context, error, sessionId = null) {
    if (!this.enabled) return;

    console.error(`=== ERROR IN ${context} ===`);
    console.error('Error message:', error?.message);
    console.error('Error code:', error?.code);
    console.error('Session ID:', sessionId);
    console.error('Full error:', error);
    console.error('Timestamp:', new Date().toISOString());
    
    // Also log session state when error occurs
    this.logSessionState(context + ' [ERROR]');
  },

  // Generate session diagnostic report
  generateDiagnosticReport() {
    if (!this.enabled) return null;

    try {
      const app = getApp();
      const report = {
        timestamp: new Date().toISOString(),
        globalData: {
          exists: !!app?.globalData,
          sessionId: app?.globalData?.currentSessionId,
          selectedWastes: app?.globalData?.selectedWastes?.length || 0
        },
        localStorage: {
          sessionId: wx.getStorageSync('currentSessionId'),
          createdAt: wx.getStorageSync('sessionCreatedAt'),
          resetFlag: wx.getStorageSync('sessionReset'),
          legacyResetFlag: wx.getStorageSync('resetFlag')
        },
        systemInfo: {
          platform: wx.getSystemInfoSync().platform,
          version: wx.getSystemInfoSync().version,
          SDKVersion: wx.getSystemInfoSync().SDKVersion
        },
        consistency: this.validateSessionConsistency()
      };
      
      console.log('=== SESSION DIAGNOSTIC REPORT ===');
      console.log(JSON.stringify(report, null, 2));
      
      return report;
      
    } catch (error) {
      console.error('Failed to generate diagnostic report:', error);
      return null;
    }
  },

  // Enable debug mode
  enable() {
    this.enabled = true;
    console.log('Session debugging enabled');
  },

  // Disable debug mode
  disable() {
    this.enabled = false;
  }
};

module.exports = SessionDebug; 