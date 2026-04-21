Page({
  data: {
    categoryType: '',
    searchText: '',
    originalData: [],
    filteredData: [],
    loading: true,
    error: null
  },

  // Field mapping for different categories to handle backend field names
  FIELD_MAPPINGS: {
    ALKALI_METALS: {
      'kMgPerL': { displayName: 'K', frontendField: 'k_mg_per_l', unit: 'mg/L' },
      'naMgPerL': { displayName: 'Na', frontendField: 'na_mg_per_l', unit: 'mg/L' },
      'mgMgPerL': { displayName: 'Mg', frontendField: 'mg_mg_per_l', unit: 'mg/L' }
    },
    HEAVY_METALS: {
      'cdMgPerL': { displayName: 'Cd', frontendField: 'cd_mg_per_l', unit: 'mg/L' },
      'niMgPerL': { displayName: 'Ni', frontendField: 'ni_mg_per_l', unit: 'mg/L' },
      'pbMgPerL': { displayName: 'Pb', frontendField: 'pb_mg_per_l', unit: 'mg/L' },
      'cuMgPerL': { displayName: 'Cu', frontendField: 'cu_mg_per_l', unit: 'mg/L' },
      'crMgPerL': { displayName: 'Cr', frontendField: 'cr_mg_per_l', unit: 'mg/L' },
      'feMgPerL': { displayName: 'Fe', frontendField: 'fe_mg_per_l', unit: 'mg/L' },
      'mnMgPerL': { displayName: 'Mn', frontendField: 'mn_mg_per_l', unit: 'mg/L' },
      'asMgPerL': { displayName: 'As', frontendField: 'as_mg_per_l', unit: 'mg/L' },
      'sbMgPerL': { displayName: 'Sb', frontendField: 'sb_mg_per_l', unit: 'mg/L' },
      'coMgPerL': { displayName: 'Co', frontendField: 'co_mg_per_l', unit: 'mg/L' },
      'tlMgPerL': { displayName: 'Tl', frontendField: 'tl_mg_per_l', unit: 'mg/L' },
      'snMgPerL': { displayName: 'Sn', frontendField: 'sn_mg_per_l', unit: 'mg/L' }
    },
    ELEMENT_COMPOSITION: {
      'cPercent': { displayName: 'C', frontendField: 'c_percent', unit: '%' },
      'hPercent': { displayName: 'H', frontendField: 'h_percent', unit: '%' },
      'oPercent': { displayName: 'O', frontendField: 'o_percent', unit: '%' },
      'nPercent': { displayName: 'N', frontendField: 'n_percent', unit: '%' },
      'sPercent': { displayName: 'S', frontendField: 's_percent', unit: '%' },
      'pPercent': { displayName: 'P', frontendField: 'p_percent', unit: '%' },
      'clPercent': { displayName: 'Cl', frontendField: 'cl_percent', unit: '%' },
      'fPercent': { displayName: 'F', frontendField: 'f_percent', unit: '%' }
    }
  },

  onLoad(options) {
    console.log('Detail page loaded with options:', options);
    const category = options.category || 'heat_value';
    console.log('Category selected:', category);
    this.setData({ categoryType: category });
    
    wx.setNavigationBarTitle({
      title: this.getCategoryTitle(category)
    });
    
    this.loadData();
  },

  getCategoryTitle(type) {
    const titles = {
      'elements': '元素组成',
      'heavy_metals': '重金属',
      'alkali_metals': '碱金属',
      'heat_value': '热值',
      'ph_value': 'pH值',
      'water_content': '含水率',
      'flash_point': '闪点',
      // NEW HAZARD PROPERTIES TITLES
      'hazard_oxidizing': '氧化性',
      'hazard_reducing': '还原性',
      'hazard_volatile': '挥发性',
      'hazard_flammable': '易燃性',
      'hazard_toxic': '毒性',
      'hazard_reactive': '反应性',
      'hazard_infectious': '感染性',
      'hazard_corrosive': '腐蚀性',
      'hazard_halogenated': '卤化烃类',
      'hazard_cyanide': '含氰化物'
    };
    console.log('Getting title for category:', type, 'Result:', titles[type]);
    return titles[type] || '理化特性详情';
  },

  // ENHANCED: Map frontend category to backend API parameter (hazard properties use boolean filters)
  getCategoryCode(frontendCategory) {
    const categoryMapping = {
      'elements': 'ELEMENT_COMPOSITION',      // ✅ Fixed: was 'ELEMENTS'
      'heavy_metals': 'HEAVY_METALS',         // ✅ Correct
      'alkali_metals': 'ALKALI_METALS',       // ✅ Correct  
      'heat_value': 'HEAT_VALUE',             // ✅ Correct
      'ph_value': 'PH',                       // ✅ Fixed: was 'PH_VALUE'
      'water_content': 'WATER_CONTENT',       // ✅ Correct
      'flash_point': 'FLASH_POINT'            // ✅ Correct
    };
    return categoryMapping[frontendCategory] || 'HEAT_VALUE';
  },

  // NEW: Map hazard property categories to API parameters
  getHazardApiParam(frontendCategory) {
    const hazardMapping = {
      'hazard_oxidizing': 'oxidizing',
      'hazard_reducing': 'reducing',
      'hazard_volatile': 'volatileProperty',
      'hazard_flammable': 'flammable',
      'hazard_toxic': 'toxic',
      'hazard_reactive': 'reactive',
      'hazard_infectious': 'infectious',
      'hazard_corrosive': 'corrosive',
      'hazard_halogenated': 'halogenatedHydrocarbon',
      'hazard_cyanide': 'cyanideContaining'
    };
    return hazardMapping[frontendCategory];
  },

  // NEW: Check if category is a hazard property
  isHazardCategory(categoryType) {
    return categoryType.startsWith('hazard_');
  },

  async loadData() {
    try {
      this.setData({ loading: true, error: null });
      
      let response;
      
      // ENHANCED: Handle hazard properties differently from traditional categories
      if (this.isHazardCategory(this.data.categoryType)) {
        // For hazard properties, use boolean filter API
        const hazardParam = this.getHazardApiParam(this.data.categoryType);
        console.log('Loading hazard data for:', this.data.categoryType, 'with param:', hazardParam);
        response = await this.callHazardAPI(hazardParam);
      } else {
        // For traditional categories, use existing category-based API
        const categoryCode = this.getCategoryCode(this.data.categoryType);
        console.log('Loading traditional category data for:', categoryCode);
        response = await this.callRealAPI(categoryCode);
      }
      
      if (response && response.success && response.data) {
        const records = response.data.records || [];
        console.log('Loaded real data:', records.length, 'items');
        console.log('Sample data:', records[0]);
        
        // Transform data to match frontend structure
        const transformedData = this.transformApiData(records);
        console.log('Transformed data:', transformedData.length, 'items');
        
        // Use more lenient filtering - show more data
        const filteredData = this.filterDataByCategory(transformedData, this.data.categoryType);
        console.log('Filtered data for', this.data.categoryType, ':', filteredData.length, 'items');
        
        this.setData({
          originalData: transformedData,
          filteredData: filteredData,
          loading: false
        });
      } else {
        throw new Error(response.message || 'API returned invalid format');
      }
    } catch (error) {
      console.error('Load data error:', error);
      wx.showToast({
        title: '数据加载失败',
        icon: 'none',
        duration: 2000
      });
      
      this.setData({ 
        originalData: [],
        filteredData: [],
        loading: false,
        error: error.message || '数据加载失败'
      });
    }
  },

  // API call with correct URL
  callRealAPI(categoryCode) {
    return new Promise((resolve, reject) => {
      const { searchText } = this.data;
      
      // Build query parameters
      let url = require('../../../config/api-config.js').getFullUrl(`/api/physical-properties/${categoryCode}`);
      const params = [];
      
      if (searchText) {
        params.push(`search=${encodeURIComponent(searchText)}`);
      }
      params.push('page=1');
      params.push('size=50'); // Get more items
      
      if (params.length > 0) {
        url += '?' + params.join('&');
      }
      
      console.log('API Request URL:', url);
      
      wx.request({
        url: url,
        method: 'GET',
        header: {
          'Content-Type': 'application/json'
        },
        success: (res) => {
          console.log('API Response:', res);
          if (res.statusCode === 200) {
            resolve(res.data);
          } else {
            reject(new Error(`API Error: ${res.statusCode}`));
          }
        },
        fail: (error) => {
          console.error('API Request failed:', error);
          reject(error);
        }
      });
    });
  },

  // NEW: API call for hazard properties with boolean filtering
  callHazardAPI(hazardParam) {
    return new Promise((resolve, reject) => {
      const { searchText } = this.data;
      
      // Use ELEMENT_COMPOSITION as base endpoint for hazard property filtering
      let url = require('../../../config/api-config.js').getFullUrl(`/api/physical-properties/ELEMENT_COMPOSITION`);
      const params = [];
      
      // Add hazard property boolean filter
      params.push(`${hazardParam}=true`);
      
      if (searchText) {
        params.push(`search=${encodeURIComponent(searchText)}`);
      }
      params.push('page=1');
      params.push('size=50'); // Get more items
      
      if (params.length > 0) {
        url += '?' + params.join('&');
      }
      
      console.log('Hazard API Request URL:', url);
      
      wx.request({
        url: url,
        method: 'GET',
        header: {
          'Content-Type': 'application/json'
        },
        success: (res) => {
          console.log('Hazard API Response:', res);
          if (res.statusCode === 200) {
            resolve(res.data);
          } else {
            reject(new Error(`Hazard API Error: ${res.statusCode}`));
          }
        },
        fail: (error) => {
          console.error('Hazard API Request failed:', error);
          reject(error);
        }
      });
    });
  },

  // Transform properties using field mappings
  transformProperties(properties, categoryCode) {
    const mapping = this.FIELD_MAPPINGS[categoryCode];
    if (!mapping) return {};
    
    const transformed = {};
    Object.keys(mapping).forEach(backendField => {
      const config = mapping[backendField];
      const value = this.extractValue(properties, [backendField]);
      transformed[config.frontendField] = value;
      
      if (value !== null && value !== undefined) {
        console.log(`Mapped ${backendField} -> ${config.frontendField}: ${value} ${config.unit}`);
      }
    });
    
    // Filter heavy metals to show only non-zero values
    if (categoryCode === 'HEAVY_METALS') {
      return this.filterNonZeroMetals(transformed);
    }
    
    return transformed;
  },

  // Filter function for heavy metals to show only non-zero values
  filterNonZeroMetals(heavyMetalsData) {
    const filtered = {};
    Object.keys(heavyMetalsData).forEach(key => {
      const value = heavyMetalsData[key];
      if (value !== null && value !== undefined && parseFloat(value) > 0) {
        filtered[key] = value;
        console.log(`Heavy metal ${key}: ${value} (non-zero, included)`);
      } else {
        console.log(`Heavy metal ${key}: ${value} (zero/null, filtered out)`);
      }
    });
    
    console.log(`Filtered heavy metals: ${Object.keys(filtered).length} out of ${Object.keys(heavyMetalsData).length} metals`);
    return filtered;
  },

  // Enhanced data transformation
  transformApiData(records) {
    return records.map(record => {
      const properties = record.properties || {};
      
      console.log('Transforming record:', record.wasteCode, 'Properties structure:', properties);
      console.log('Properties keys:', Object.keys(properties));
      
      // Log nested property structure for debugging
      Object.keys(properties).forEach(key => {
        if (properties[key] && typeof properties[key] === 'object') {
          console.log(`Property ${key} contains:`, Object.keys(properties[key]));
        }
      });
      
      // Use field mappings to transform properties for each category
      const elementProperties = this.transformProperties(properties, 'ELEMENT_COMPOSITION');
      const heavyMetalProperties = this.transformProperties(properties, 'HEAVY_METALS');
      const alkaliMetalProperties = this.transformProperties(properties, 'ALKALI_METALS');
      
      // Transform the data structure using the mapped properties
      const transformed = {
        id: record.id,
        waste_code: record.wasteCode,
        waste_name: record.wasteName,
        source_unit: record.sourceUnit,
        
        // Single properties (keeping the old extraction method for now)
        heat_value_cal_per_g: this.extractValue(properties, [
          'heatValue', 'heat_value_cal_per_g', 'heatValueCalPerG', 'heatVal', 'heat_value', 
          'calorificValue', 'heat_content', 'heatContent', 'HeatValue', 'calorific_value'
        ]),
        
        ph: this.extractValue(properties, [
          'ph', 'phValue', 'pH', 'PH', 'Ph', 'ph_value', 'phLevel', 'pH_value', 'acidity'
        ]),
        
        water_content_percent: this.extractValue(properties, [
          'waterContent', 'water_content_percent', 'waterContentPercent', 'moisture', 
          'moistureContent', 'water_content', 'WaterContent', 'moisture_percent', 'humidity'
        ]),
        
        flash_point_celsius: this.extractValue(properties, [
          'flashPoint', 'flash_point_celsius', 'flashPointCelsius', 'flash_point',
          'FlashPoint', 'flashpoint', 'ignitionPoint', 'flash_temp', 'flashTemperature'
        ]),
        
        // Use the mapped properties for multi-property categories
        ...elementProperties,
        ...heavyMetalProperties,
        ...alkaliMetalProperties
      };
      
      console.log('Transformed record result:', {
        wasteCode: transformed.waste_code,
        heatValue: transformed.heat_value_cal_per_g,
        ph: transformed.ph,
        elements: {
          c: transformed.c_percent,
          h: transformed.h_percent,
          o: transformed.o_percent,
          n: transformed.n_percent
        },
        heavyMetals: {
          fe: transformed.fe_mg_per_l,
          cu: transformed.cu_mg_per_l,
          pb: transformed.pb_mg_per_l
        },
        alkaliMetals: {
          k: transformed.k_mg_per_l,
          na: transformed.na_mg_per_l,
          mg: transformed.mg_mg_per_l
        }
      });
      
      return transformed;
    });
  },

  // Utility function to recursively search for a field in nested objects
  searchNestedValue(obj, fieldNames, path = '') {
    if (!obj || typeof obj !== 'object') return null;
    
    // Check current level
    for (const fieldName of fieldNames) {
      if (obj.hasOwnProperty(fieldName) && obj[fieldName] !== null && obj[fieldName] !== undefined && obj[fieldName] !== '') {
        console.log(`Found ${fieldName} = ${obj[fieldName]} at path: ${path}`);
        return obj[fieldName];
      }
    }
    
    // Search recursively in nested objects
    for (const [key, value] of Object.entries(obj)) {
      if (value && typeof value === 'object') {
        const result = this.searchNestedValue(value, fieldNames, path ? `${path}.${key}` : key);
        if (result !== null) return result;
      }
    }
    
    return null;
  },

  // Helper function to extract value from nested properties structure
  extractValue(properties, fieldNames) {
    // First, try to extract from the first level (direct properties)
    for (const fieldName of fieldNames) {
      const value = properties[fieldName];
      if (value !== null && value !== undefined && value !== '' && value !== 'undefined') {
        return value;
      }
    }
    
    // If not found at first level, search within nested property objects
    // The backend returns properties as: { "additionalProp1": {}, "additionalProp2": {}, ... }
    const propertyKeys = Object.keys(properties || {});
    
    for (const propKey of propertyKeys) {
      const propObject = properties[propKey];
      if (propObject && typeof propObject === 'object') {
        // Search within each nested property object
        for (const fieldName of fieldNames) {
          const value = propObject[fieldName];
          if (value !== null && value !== undefined && value !== '' && value !== 'undefined') {
            console.log(`Found ${fieldName} = ${value} in property object ${propKey}`);
            return value;
          }
        }
        
        // Also try with lowercase variations
        for (const fieldName of fieldNames) {
          const lowerFieldName = fieldName.toLowerCase();
          const value = propObject[lowerFieldName];
          if (value !== null && value !== undefined && value !== '' && value !== 'undefined') {
            console.log(`Found ${lowerFieldName} = ${value} in property object ${propKey}`);
            return value;
          }
        }
        
        // Try with camelCase to snake_case conversion
        for (const fieldName of fieldNames) {
          const snakeFieldName = fieldName.replace(/([A-Z])/g, '_$1').toLowerCase();
          const value = propObject[snakeFieldName];
          if (value !== null && value !== undefined && value !== '' && value !== 'undefined') {
            console.log(`Found ${snakeFieldName} = ${value} in property object ${propKey}`);
            return value;
          }
        }
      }
    }
    
    // As a final fallback, use recursive search
    console.log(`Falling back to recursive search for fields: ${fieldNames.join(', ')}`);
    return this.searchNestedValue(properties, fieldNames);
  },

  // More lenient filtering - allow items with at least some data
  filterDataByCategory(data, categoryType) {
    if (!data || !Array.isArray(data)) {
      console.warn('Invalid data format for filtering');
      return [];
    }

    const filtered = data.filter(item => {
      // ENHANCED: Handle hazard properties and traditional categories
      if (this.isHazardCategory(categoryType)) {
        // For hazard properties, the API already filters by boolean property
        // so we just need to show all returned items
        return true;
      }
      
      // Traditional category filtering
      switch(categoryType) {
        case 'elements':
          return this.hasElementsData(item);
        case 'heavy_metals':
          return this.hasHeavyMetalsData(item);
        case 'alkali_metals':
          return this.hasAlkaliMetalsData(item);
        case 'heat_value':
          return this.hasSinglePropertyData(item, 'heat_value_cal_per_g');
        case 'ph_value':
          return this.hasSinglePropertyData(item, 'ph');
        case 'water_content':
          return this.hasSinglePropertyData(item, 'water_content_percent');
        case 'flash_point':
          return this.hasSinglePropertyData(item, 'flash_point_celsius');
        default:
          return true; // Show all if unknown category
      }
    });

    console.log(`Filtered ${categoryType} data:`, filtered.length, 'out of', data.length, 'items');
    
    // If no items pass the filter, show all items for debugging
    if (filtered.length === 0 && data.length > 0) {
      console.warn('No items passed filter, showing all items for debugging');
      return data;
    }
    
    return filtered;
  },

  hasElementsData(item) {
    const fields = ['c_percent', 'h_percent', 'o_percent', 'n_percent', 's_percent', 'p_percent', 'cl_percent', 'f_percent'];
    const hasData = fields.some(field => this.hasValidValue(item[field]));
    if (!hasData) {
      console.log(`No elements data for ${item.waste_code}. Values:`, {
        c: item.c_percent,
        h: item.h_percent,
        o: item.o_percent,
        n: item.n_percent
      });
    } else {
      console.log(`Elements data found for ${item.waste_code}:`, {
        c: item.c_percent,
        h: item.h_percent,
        o: item.o_percent,
        n: item.n_percent
      });
    }
    return hasData;
  },

  hasHeavyMetalsData(item) {
    const fields = ['fe_mg_per_l', 'cu_mg_per_l', 'pb_mg_per_l', 'cd_mg_per_l', 'cr_mg_per_l', 'ni_mg_per_l', 'as_mg_per_l', 'mn_mg_per_l', 'sn_mg_per_l', 'co_mg_per_l', 'sb_mg_per_l', 'tl_mg_per_l'];
    
    // Check if any heavy metals have data (after filtering, only non-zero values should remain)
    const hasData = fields.some(field => this.hasValidValue(item[field]));
    
    if (!hasData) {
      console.log(`No heavy metals data for ${item.waste_code} (all values are zero or null)`);
    } else {
      const nonZeroMetals = {};
      fields.forEach(field => {
        if (this.hasValidValue(item[field])) {
          nonZeroMetals[field] = item[field];
        }
      });
      console.log(`Heavy metals data found for ${item.waste_code}:`, nonZeroMetals);
    }
    return hasData;
  },

  hasAlkaliMetalsData(item) {
    const fields = ['k_mg_per_l', 'na_mg_per_l', 'mg_mg_per_l'];
    const hasData = fields.some(field => this.hasValidValue(item[field]));
    if (!hasData) {
      console.log(`No alkali metals data for ${item.waste_code}. Values:`, {
        k: item.k_mg_per_l,
        na: item.na_mg_per_l,
        mg: item.mg_mg_per_l
      });
    }
    return hasData;
  },

  hasSinglePropertyData(item, field) {
    const hasData = this.hasValidValue(item[field]);
    if (!hasData) {
      console.log(`No ${field} data for ${item.waste_code}. Value:`, item[field]);
    }
    return hasData;
  },

  // More lenient validation - accept more values including zero
  hasValidValue(value) {
    return value !== null && 
           value !== undefined && 
           value !== '-' && 
           value !== '' && 
           value !== 'undefined' &&
           value !== 'null' &&
           !isNaN(value); // Accept numbers including 0
  },

  onSearchInput(e) {
    const searchText = e.detail.value;
    this.setData({ searchText });
    
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => {
      this.performSearch(searchText);
    }, 300);
  },

  performSearch(searchText) {
    // For now, do client-side search
    // Later you can implement server-side search by calling API again
    const { originalData, categoryType } = this.data;
    
    let categoryFiltered = this.filterDataByCategory(originalData, categoryType);
    
    if (!searchText.trim()) {
      this.setData({ filteredData: categoryFiltered });
      return;
    }
    
    const searchFiltered = categoryFiltered.filter(item => 
      (item.waste_code && item.waste_code.toLowerCase().includes(searchText.toLowerCase())) ||
      (item.waste_name && item.waste_name.toLowerCase().includes(searchText.toLowerCase())) ||
      (item.source_unit && item.source_unit.toLowerCase().includes(searchText.toLowerCase()))
    );
    
    this.setData({ filteredData: searchFiltered });
  },

  navigateBack() {
    wx.navigateBack();
  }
}); 