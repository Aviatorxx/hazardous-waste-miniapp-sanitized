// API Configuration
// 修改此处的BASE_URL即可切换开发/生产环境
const BASE_URL = 'http://localhost:8080';
// const BASE_URL = 'https://your-server.example.com';
// API endpoints configuration
const API_CONFIG = {
  // 基础服务器地址
  BASE_URL: BASE_URL,
  
  // API路径配置
  MATCHING_API: '/api/matching',
  WASTE_DIRECTORY_API: '/api/v1/waste-directory',
  THERMAL_PROPERTIES_API: '/api/thermal-properties',
  PHYSICAL_PROPERTIES_API: '/api/physical-properties',
  
  // 静态资源路径
  STATIC_IMAGES: '/static/thermal-images',
  
  // 完整API地址
  getFullUrl: (path) => `${BASE_URL}${path}`,
  
  // 获取静态图片URL
  // getImageUrl: (spectrumType, fileName) => `${BASE_URL}/static/thermal-images/${spectrumType}/${fileName}`

  getImageUrl: (spectrumType, fileName) => `${BASE_URL}/static/thermal-images/${spectrumType}/${fileName}?_t=${Date.now()}`
};

module.exports = API_CONFIG; 
