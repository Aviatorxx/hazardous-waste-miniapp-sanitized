// Compatibility Simulation API Service
const API_CONFIG = require('../config/api-config.js');
const BASE_URL = API_CONFIG.BASE_URL;
const MATCHING_API_BASE = API_CONFIG.MATCHING_API;
const WASTE_DIRECTORY_API_BASE = API_CONFIG.WASTE_DIRECTORY_API;

// Request wrapper with error handling
function request(url, options = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject({
            code: res.statusCode,
            message: res.data?.message || '请求失败',
            data: res.data
          });
        }
      },
      fail: (error) => {
        reject({
          code: -1,
          message: '网络请求失败',
          error
        });
      }
    });
  });
}

// Session Management APIs
const sessionAPI = {
  // Create matching session
  createSession(sessionData = {}) {
    const defaultData = {
      sessionName: `配伍会话_${new Date().getTime()}`,
      targetHeatValue: 14000,
      totalAmount: 50000,
      createUser: 'miniprogram_user',
      wasteItems: []
    };
    
    return request(`${MATCHING_API_BASE}/sessions`, {
      method: 'POST',
      data: { ...defaultData, ...sessionData }
    });
  },

  // Get session summary
  getSessionSummary(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/summary`);
  },

  // Get session details
  getSessionDetails(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}`);
  },

  // Get session status
  getSessionStatus(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/status`);
  },

  // Update session
  updateSession(sessionId, sessionData) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}`, {
      method: 'PUT',
      data: sessionData
    });
  },

  // Delete session
  deleteSession(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}`, {
      method: 'DELETE'
    });
  },

  // Reset session
  resetSession(sessionId, reason = '重新开始配伍') {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/reset?reason=${encodeURIComponent(reason)}`, {
      method: 'POST'
    });
  }
};

// Waste Management APIs
const wasteAPI = {
  // Add waste to session
  addWasteToSession(sessionId, wasteId, plannedAmount) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/wastes?wasteId=${wasteId}&plannedAmount=${plannedAmount}`, {
      method: 'POST'
    });
  },

  // Get wastes in session
  getSessionWastes(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/wastes`);
  },

  // Update waste quantity
  updateWasteQuantity(sessionId, wasteId, plannedAmount) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/wastes/${wasteId}?plannedAmount=${plannedAmount}`, {
      method: 'PUT'
    });
  },

  // Remove waste from session
  removeWasteFromSession(sessionId, wasteId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/wastes/${wasteId}`, {
      method: 'DELETE'
    });
  },

  // Validate waste quantities
  validateWasteQuantities(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/wastes/validate`, {
      method: 'POST'
    });
  }
};

// Waste Directory APIs
const wasteDirectoryAPI = {
  // Get waste directory list
  getWasteList(params = {}) {
    const defaultParams = {
      current: 1,
      size: 20
    };
    
    const queryParams = { ...defaultParams, ...params };
    const queryString = Object.keys(queryParams)
      .map(key => `${key}=${encodeURIComponent(queryParams[key])}`)
      .join('&');
    
    return request(`${WASTE_DIRECTORY_API_BASE}/list?${queryString}`);
  },

  // Get waste detail
  getWasteDetail(wasteId) {
    return request(`${WASTE_DIRECTORY_API_BASE}/detail/${wasteId}`);
  }
};

// Compatibility Check APIs
const compatibilityAPI = {
  // Start compatibility check
  startCompatibilityCheck(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/compatibility/check`, {
      method: 'POST'
    });
  },

  // Get compatibility check status
  getCompatibilityStatus(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/compatibility/status`);
  },

  // Get compatibility check result
  getCompatibilityResult(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/compatibility/result`);
  },

  // Get compatibility details
  getCompatibilityDetails(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/compatibility/details`);
  },

  // Get compatibility categories
  getCompatibilityCategories() {
    return request('/api/compatibility/categories');
  },

  // Poll compatibility status until complete
  pollCompatibilityStatus(sessionId, callback) {
    const poll = () => {
      this.getCompatibilityStatus(sessionId)
        .then(result => {
          const status = result.data?.status;
          callback(null, result);
          
          // Continue polling if not complete
          if (status === 'checking' || status === 'pending') {
            setTimeout(poll, 2000);
          }
        })
        .catch(callback);
    };
    
    poll();
  }
};

// Calculation APIs
const calculationAPI = {
  // Start blending calculation
  startCalculation(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/calculate`, {
      method: 'POST'
    });
  },

  // Get calculation status
  getCalculationStatus(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/calculate/status`);
  },

  // Get calculation results
  getCalculationResults(sessionId) {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/results`);
  },

  // Export calculation results
  exportResults(sessionId, format = 'json') {
    return request(`${MATCHING_API_BASE}/sessions/${sessionId}/results/export?format=${format}`);
  },

  // Poll calculation status until complete
  pollCalculationStatus(sessionId, callback) {
    const poll = () => {
      this.getCalculationStatus(sessionId)
        .then(result => {
          const status = result.data?.status;
          callback(null, result);
          
          // Continue polling if not complete
          if (status === 'calculating' || status === 'pending') {
            setTimeout(poll, 2000);
          }
        })
        .catch(callback);
    };
    
    poll();
  }
};

// Constraint APIs
const constraintAPI = {
  // Get constraint parameters
  getConstraints() {
    return request(`${MATCHING_API_BASE}/constraints`);
  },

  // Update constraint parameters
  updateConstraints(constraints) {
    return request(`${MATCHING_API_BASE}/constraints`, {
      method: 'PUT',
      data: constraints
    });
  },

  // Get compatibility matrix
  getCompatibilityMatrix() {
    return request(`${MATCHING_API_BASE}/compatibility/matrix`);
  }
};

// Utility functions
const utils = {
  // Show error message with proper format
  showError(error, title = '操作失败') {
    let message = '网络错误，请稍后重试';
    
    if (error.message) {
      message = error.message;
    } else if (error.data && error.data.message) {
      message = error.data.message;
    }
    
    wx.showToast({
      title: message,
      icon: 'none',
      duration: 3000
    });
  },

  // Show loading with proper message
  showLoading(title = '加载中...') {
    wx.showLoading({
      title: title,
      mask: true
    });
  },

  // Hide loading
  hideLoading() {
    wx.hideLoading();
  },

  // Format API response data
  formatResponse(response) {
    if (response && response.success && response.data) {
      return response.data;
    }
    throw new Error(response?.message || '数据格式错误');
  }
};

module.exports = {
  sessionAPI,
  wasteAPI,
  wasteDirectoryAPI,
  compatibilityAPI,
  calculationAPI,
  constraintAPI,
  utils
}; 