// API Configuration for Thermal Properties Module
const API_CONFIG = require('../config/api-config.js');
const API_BASE = API_CONFIG.getFullUrl(API_CONFIG.THERMAL_PROPERTIES_API);

const API = {
  getTypes: `${API_BASE}/types`,
  search: (spectrumType, keyword, pageNum = 1, pageSize = 10) => 
    `${API_BASE}/search?spectrumType=${spectrumType}&keyword=${encodeURIComponent(keyword)}&pageNum=${pageNum}&pageSize=${pageSize}`,
  getDetail: (id) => `${API_BASE}/${id}`,
  getImageUrl: (spectrumType, fileName) => API_CONFIG.getImageUrl(spectrumType, fileName)
};

module.exports = API; 