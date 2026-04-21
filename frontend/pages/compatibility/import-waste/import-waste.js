const { wasteDirectoryAPI, wasteAPI, utils } = require('../../../utils/matching-api.js');

Page({
  data: {
    sessionId: null,
    wasteList: [],
    searchKeyword: '',
    currentPage: 1,
    pageSize: 20,
    totalRecords: 0,
    loading: false,
    hasMore: true,
    selectedWastes: [],
    addedWasteIds: [], // Array of waste IDs already added to session
    lastResetCheck: 0, // Track when reset was last checked
    // Modal data
    showQuantityModal: false,
    currentWaste: null,
    quantityInput: '',
    quantityError: ''
  },

  onLoad(options) {
    console.log('=== IMPORT WASTE PAGE LOAD ===');
    console.log('Load options:', options);
    
    if (options.sessionId) {
      console.log('Session ID from URL:', options.sessionId);
      this.setData({
        sessionId: options.sessionId
      });
      
      // Store in global state and local storage
      getApp().globalData.currentSessionId = options.sessionId;
      wx.setStorageSync('currentSessionId', options.sessionId);
    }
    
    // Initialize session management
    this.initializeImportPage();
  },

  // Initialize import page with comprehensive session management
  async initializeImportPage() {
    try {
      console.log('=== INITIALIZING IMPORT PAGE ===');
      
      // Ensure we have a valid session
      const sessionId = await this.ensureValidSession();
      
      if (!sessionId) {
        throw new Error('Unable to establish valid session');
      }
      
      // Load existing session data
      await this.loadSessionData(sessionId);
      
      // Load waste list
      await this.loadWasteList();
      
      console.log('Import page initialization complete');
      
    } catch (error) {
      console.error('Import page initialization failed:', error);
      this.handleInitializationError(error);
    }
  },

  // Ensure valid session exists
  async ensureValidSession() {
    console.log('=== ENSURING VALID SESSION ===');
    
    // Try to get session ID from multiple sources
    let sessionId = this.data.sessionId || 
                   getApp().globalData.currentSessionId || 
                   wx.getStorageSync('currentSessionId');
    
    console.log('Session ID sources:', {
      local: this.data.sessionId,
      global: getApp().globalData.currentSessionId,
      storage: wx.getStorageSync('currentSessionId'),
      selected: sessionId
    });
    
    if (sessionId) {
      // Validate existing session
      const isValid = await this.validateSession(sessionId);
      if (isValid) {
        this.synchronizeSessionState(sessionId);
        return sessionId;
      } else {
        console.log('Session validation failed, clearing invalid session');
        this.clearSessionState();
      }
    }
    
    // Create new session if needed
    console.log('Creating new session...');
    return await this.createNewSession();
  },

  // Validate session with backend
  async validateSession(sessionId) {
    try {
      console.log('Validating session:', sessionId);
      
      if (!sessionId) {
        console.log('Session ID is empty');
        return false;
      }
      
      const { sessionAPI } = require('../../../utils/matching-api.js');
      const response = await sessionAPI.getSessionStatus(sessionId);
      
      console.log('Session validation response:', response);
      
      if (response && (response.success !== false)) {
        console.log('Session validation successful');
        return true;
      } else {
        console.log('Session validation failed - session not found');
        return false;
      }
      
    } catch (error) {
      console.error('Session validation error:', error);
      return false;
    }
  },

  // Load session data
  async loadSessionData(sessionId) {
    try {
      console.log('Loading session data for:', sessionId);
      await this.loadAddedWastes();
    } catch (error) {
      console.error('Load session data error:', error);
      // Don't throw here, just log the error
    }
  },

  // Handle initialization errors
  handleInitializationError(error) {
    console.error('Handling initialization error:', error);
    
    wx.showModal({
      title: '页面初始化失败',
      content: `初始化导入页面时出现问题：${error.message}\n\n请重新进入或检查网络连接`,
      showCancel: true,
      cancelText: '返回',
      confirmText: '重试',
      success: (res) => {
        if (res.confirm) {
          // Retry initialization
          setTimeout(() => {
            this.initializeImportPage();
          }, 1000);
        } else {
          // Go back
          wx.navigateBack({
            fail: () => {
              wx.switchTab({
                url: '/pages/index/index'
              });
            }
          });
        }
      }
    });
  },

  onShow() {
    console.log('=== Data Type Debug ===');
    console.log('Current addedWasteIds:', this.data.addedWasteIds);
    console.log('AddedWasteIds types:', this.data.addedWasteIds.map(id => typeof id));
    
    // Check for session reset flag
    const resetTime = wx.getStorageSync('sessionReset') || 0;
    const lastCheck = this.data.lastResetCheck || 0;
    
    if (resetTime > lastCheck) {
      // Reset happened, clear state
      this.clearSessionState();
      this.setData({
        lastResetCheck: resetTime
      });
      console.log('Session reset detected, cleared state');
    }
    
    // Also check old reset flag for backward compatibility
    const legacyResetTime = wx.getStorageSync('resetFlag') || 0;
    if (legacyResetTime > lastCheck) {
      this.clearSessionState();
      this.setData({
        lastResetCheck: legacyResetTime
      });
      console.log('Legacy reset detected, cleared state');
    }
    
    if (this.data.wasteList.length > 0) {
      console.log('Waste list IDs:', this.data.wasteList.slice(0, 3).map(w => w.id));
      console.log('Waste list ID types:', this.data.wasteList.slice(0, 3).map(w => typeof w.id));
    }
    
    // Test first few items
    if (this.data.wasteList.length > 0 && this.data.addedWasteIds.length > 0) {
      const firstWaste = this.data.wasteList[0];
      console.log('Testing first waste:', {
        wasteId: firstWaste.id,
        wasteIdType: typeof firstWaste.id,
        isAdded: this.isWasteAdded(firstWaste.id)
      });
    }
    
    this.loadCurrentSession();
  },

  // Load current session to check added wastes
  async loadCurrentSession() {
    try {
      // Try to get session ID from global state
      let sessionId = getApp().globalData.currentSessionId || this.data.sessionId;
      
      if (sessionId) {
        // Verify session exists by calling API
        try {
          const { sessionAPI } = require('../../../utils/matching-api.js');
          const response = await sessionAPI.getSessionSummary(sessionId);
          if (response) {
            // Session exists, load added wastes
            this.setData({
              sessionId: sessionId
            });
            await this.loadAddedWastes();
          } else {
            // Session doesn't exist, clear it
            console.log('Session does not exist, clearing...');
            this.clearSessionState();
          }
        } catch (error) {
          console.log('Session verification failed, clearing...');
          this.clearSessionState();
        }
      } else {
        // No session ID, start fresh
        this.clearSessionState();
      }
    } catch (error) {
      console.error('Load session state failed:', error);
      this.clearSessionState();
    }
  },

  // Create new session when needed
  async createNewSession() {
    try {
      console.log('=== CREATING NEW SESSION ===');
      
      const sessionName = `配伍会话_${Date.now()}`;
      const { sessionAPI } = require('../../../utils/matching-api.js');
      
      const sessionData = {
        sessionName: sessionName,
        totalAmount: 50000, // Default 50 tons
        targetHeatValue: 14000, // Default target heat value
        createUser: 'miniprogram_user'
      };

      console.log('Session data to create:', sessionData);
      
      const response = await sessionAPI.createSession(sessionData);
      
      console.log('Session creation API response:', response);
      
      if (!response) {
        throw new Error('Session creation returned no data');
      }
      
      const newSessionId = response.data?.sessionId || response.data?.id || response.sessionId || response.id;
      
      if (!newSessionId) {
        console.error('No session ID found in response:', response);
        throw new Error('Session ID not found in response');
      }
      
      console.log('New session created:', newSessionId);
      console.log('Session ID type:', typeof newSessionId);
      
      // Synchronize session state
      this.synchronizeSessionState(newSessionId);
      
      // Store creation timestamp
      wx.setStorageSync('sessionCreatedAt', Date.now());
      
      console.log('Session state synchronized');
      
      return newSessionId;
      
    } catch (error) {
      console.error('=== SESSION CREATION FAILED ===');
      console.error('Error details:', error);
      throw new Error(`创建会话失败: ${error.message}`);
    }
  },

  // Clear session state
  clearSessionState() {
    console.log('Clearing session state');
    this.setData({
      sessionId: null,
      addedWasteIds: []
    });
    getApp().globalData.currentSessionId = null;
    wx.removeStorageSync('currentSessionId');
  },

  // Synchronize session state across all storage methods
  synchronizeSessionState(sessionId) {
    console.log('Synchronizing session state:', sessionId);
    
    this.setData({
      sessionId: sessionId
    });
    
    getApp().globalData.currentSessionId = sessionId;
    wx.setStorageSync('currentSessionId', sessionId);
  },

  // Handle session creation errors
  async handleSessionError(error = null) {
    console.log('Handling session error:', error);
    
    const errorDetails = error ? `\n\n错误详情: ${error.message}` : '';
    
    wx.showModal({
      title: '会话异常',
      content: `当前会话已失效或无法访问，是否创建新的配伍会话？${errorDetails}`,
      showCancel: true,
      cancelText: '取消',
      confirmText: '创建会话',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({
              title: '创建会话中...'
            });
            
            const newSessionId = await this.createNewSession();
            
            wx.hideLoading();
            
            if (newSessionId) {
              wx.showToast({
                title: '新会话已创建',
                icon: 'success'
              });
              
              // Refresh the page data
              await this.loadSessionData(newSessionId);
            } else {
              throw new Error('Session creation returned empty ID');
            }
            
          } catch (createError) {
            console.error('Session creation in error handler failed:', createError);
            wx.hideLoading();
            
            wx.showModal({
              title: '创建会话失败',
              content: `无法创建新会话: ${createError.message}\n\n请检查网络连接或联系技术支持`,
              showCancel: false,
              confirmText: '确定'
            });
          }
        }
      }
    });
  },

  // Load waste list from API
  async loadWasteList(loadMore = false) {
    if (this.data.loading) return;
    
    this.setData({
      loading: true
    });

    try {
      const params = {
        current: loadMore ? this.data.currentPage + 1 : 1,
        size: this.data.pageSize
      };
      
      if (this.data.searchKeyword) {
        params.keyword = this.data.searchKeyword;
      }

      const result = await wasteDirectoryAPI.getWasteList(params);
      const responseData = utils.formatResponse(result);
      
      console.log('=== IMPORT INTERFACE API RESPONSE ===');
      console.log('API response:', responseData);
      console.log('Records sample:', responseData.records.slice(0, 2));
      
      // Process waste list to include formatted storage display
      const processedRecords = responseData.records.map(item => {
        const storage = parseFloat(item.remainingStorage) || 0;
        const formattedDisplay = this.formatStorage(storage);
        console.log(`Processing waste ${item.wasteCode}: 
          Raw: ${item.remainingStorage} (${typeof item.remainingStorage})
          Parsed: ${storage} (${typeof storage})
          Formatted: ${formattedDisplay}`);
        return {
          ...item,
          remainingStorage: storage,
          remainingStorageDisplay: formattedDisplay
        };
      });
      
      const newWasteList = loadMore ? 
        [...this.data.wasteList, ...processedRecords] : 
        processedRecords;

      this.setData({
        wasteList: newWasteList,
        currentPage: responseData.current,
        totalRecords: responseData.total || newWasteList.length,
        hasMore: responseData.records.length === this.data.pageSize && 
                newWasteList.length < (responseData.total || newWasteList.length)
      });

    } catch (error) {
      console.error('Load waste list error:', error);
      utils.showError(error, '加载失败');
    } finally {
      this.setData({
        loading: false
      });
    }
  },

  // Search input handler
  onSearchInput(e) {
    const keyword = e.detail.value;
    this.setData({
      searchKeyword: keyword
    });
    
    // Debounce search
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => {
      this.performSearch();
    }, 500);
  },

  // Perform search
  performSearch() {
    this.setData({
      currentPage: 1,
      hasMore: true
    });
    this.loadWasteList();
  },

  // Clear search
  onSearchClear() {
    this.setData({
      searchKeyword: '',
      currentPage: 1,
      hasMore: true
    });
    this.loadWasteList();
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

  // Check if waste is already added to current session
  isWasteAdded(wasteId) {
    // Convert both to same data type for reliable comparison
    const numericWasteId = parseInt(wasteId);
    const numericAddedIds = this.data.addedWasteIds.map(id => parseInt(id));
    const result = numericAddedIds.includes(numericWasteId);
    
    console.log('isWasteAdded check:', {
      wasteId: wasteId,
      wasteIdType: typeof wasteId,
      numericWasteId: numericWasteId,
      addedWasteIds: this.data.addedWasteIds,
      numericAddedIds: numericAddedIds,
      result: result
    });
    
    return result;
  },

  // Get button text based on waste status
  getButtonText(wasteId) {
    return this.isWasteAdded(wasteId) ? '✓ 已添加' : '+ 添加到配伍模拟';
  },

  // Get button class based on waste status
  getButtonClass(wasteId) {
    return this.isWasteAdded(wasteId) ? 'add-btn-full added' : 'add-btn-full';
  },

  // Show message for already added waste
  showAlreadyAdded() {
    wx.showToast({
      title: '该危废已添加',
      icon: 'none',
      duration: 2000
    });
  },

  // Add debug method to manually test added state
  debugAddWaste(wasteId) {
    console.log('Debug: manually adding waste', wasteId);
    const numericWasteId = parseInt(wasteId);
    const addedWasteIds = [...this.data.addedWasteIds.map(id => parseInt(id)), numericWasteId];
    this.setData({
      addedWasteIds: addedWasteIds,
      _timestamp: Date.now()
    });
    console.log('Debug: updated addedWasteIds', this.data.addedWasteIds);
  },

  // Force refresh data binding
  forceRefresh() {
    console.log('Force refreshing data binding...');
    const addedWasteIds = this.data.addedWasteIds.map(id => parseInt(id));
    this.setData({
      addedWasteIds: addedWasteIds,
      _timestamp: Date.now(),
      _refresh: Math.random()
    });
  },

  // Add waste to session - show custom modal
  onAddWaste(e) {
    const wasteData = e.currentTarget.dataset.waste;
    
    if (!this.data.sessionId) {
      wx.showToast({
        title: '会话ID不存在',
        icon: 'none'
      });
      return;
    }

    // Check if already added
    if (this.isWasteAdded(wasteData.id)) {
      this.showAlreadyAdded();
      return;
    }

    // Show custom quantity modal
    this.setData({
      showQuantityModal: true,
      currentWaste: wasteData,
      quantityInput: '',
      quantityError: ''
    });
  },

  // Handle quantity input
  onQuantityInput(e) {
    const value = e.detail.value;
    this.setData({
      quantityInput: value,
      quantityError: '' // Clear error when user types
    });
  },

  // Validate quantity input
  validateQuantity(inputValue, remainingStorage) {
    // Check if input is empty
    if (!inputValue || inputValue.trim() === '') {
      return { valid: false, message: '请输入计划用量' };
    }

    // Clean and convert to number - support decimal input
    const cleanedInput = inputValue.trim();
    const quantity = parseFloat(cleanedInput);
    
    // Check if it's a valid number
    if (isNaN(quantity)) {
      return { valid: false, message: '请输入有效数值' };
    }
    
    // Check if it's positive
    if (quantity <= 0) {
      return { valid: false, message: '用量必须大于0' };
    }
    
    // Check if it exceeds remaining storage with specific error message
    if (quantity > remainingStorage) {
      return { 
        valid: false, 
        message: `用量不能超过剩余库存量 ${this.formatStorage(remainingStorage)}`
      };
    }
    
    // Check for reasonable precision (max 3 decimal places)
    const decimalPlaces = (cleanedInput.split('.')[1] || '').length;
    if (decimalPlaces > 3) {
      return { 
        valid: false, 
        message: '用量精度不能超过3位小数'
      };
    }
    
    return { valid: true, value: quantity };
  },

  // Confirm quantity modal
  async onQuantityConfirm() {
    const { quantityInput, currentWaste } = this.data;
    
    if (!currentWaste) return;
    
    // Validate input
    const validation = this.validateQuantity(quantityInput, currentWaste.remainingStorage);
    
    if (!validation.valid) {
      // Show error message and keep modal open
      this.setData({
        quantityError: validation.message
      });
      
      // Show toast for immediate feedback
      wx.showToast({
        title: validation.message,
        icon: 'none',
        duration: 2000
      });
      
      return; // Keep modal open to show error
    }
    
    // Clear any previous errors
    this.setData({
      quantityError: ''
    });
    
    // Close modal and add to session only after successful validation
    this.setData({
      showQuantityModal: false,
      currentWaste: null,
      quantityInput: '',
      quantityError: ''
    });
    
    await this.addWasteToSession(currentWaste.id, validation.value);
  },

  // Cancel quantity modal
  onQuantityCancel() {
    this.setData({
      showQuantityModal: false,
      currentWaste: null,
      quantityInput: '',
      quantityError: ''
    });
  },

  // Close modal when clicking backdrop
  onModalBackdropTap() {
    this.onQuantityCancel();
  },

  // Prevent modal content tap from closing modal
  onModalContentTap() {
    // Do nothing - prevents event bubbling
  },

  // Load wastes already added to current session
  async loadAddedWastes() {
    if (!this.data.sessionId) {
      return;
    }

    try {
      wx.showLoading({
        title: '加载会话数据...'
      });
      
      // Get session wastes from API
      const result = await wasteAPI.getSessionWastes(this.data.sessionId);
      const sessionWastes = utils.formatResponse(result);
      
      // Extract waste IDs - ensure we get the correct IDs and convert to numbers
      let addedWasteIds = [];
      if (Array.isArray(sessionWastes)) {
        addedWasteIds = sessionWastes.map(waste => {
          // Try different possible ID fields and convert to number
          const id = waste.wasteId || waste.id || waste.waste_id;
          return parseInt(id);
        }).filter(id => !isNaN(id));
      }
      
      console.log('Loaded added waste IDs (converted to numbers):', addedWasteIds);
      
      this.setData({
        addedWasteIds: addedWasteIds,
        selectedWastes: addedWasteIds
      });
      
    } catch (error) {
      console.error('Load added wastes error:', error);
      
      // Try to load from local storage as backup
      try {
        const localAddedWastes = wx.getStorageSync(`addedWastes_${this.data.sessionId}`);
        if (localAddedWastes && Array.isArray(localAddedWastes)) {
          console.log('Loaded from local storage:', localAddedWastes);
          this.setData({
            addedWasteIds: localAddedWastes,
            selectedWastes: localAddedWastes
          });
        } else {
          this.setData({
            addedWasteIds: [],
            selectedWastes: []
          });
        }
      } catch (storageError) {
        console.error('Failed to load from storage:', storageError);
        this.setData({
          addedWasteIds: [],
          selectedWastes: []
        });
      }
    } finally {
      wx.hideLoading();
    }
  },

  // Add waste to session with comprehensive validation
  async addWasteToSession(wasteId, plannedAmount) {
    try {
      console.log('=== ADD WASTE TO SESSION ===');
      console.log('Waste ID:', wasteId, 'Amount:', plannedAmount);
      
      wx.showLoading({
        title: '添加中...'
      });

      // Validate inputs
      if (!wasteId) {
        throw new Error('危废ID不能为空');
      }
      
      if (!plannedAmount || plannedAmount <= 0) {
        throw new Error('计划用量必须大于0');
      }

      // Ensure valid session exists
      let sessionId = await this.ensureValidSession();
      
      if (!sessionId) {
        throw new Error('无法获取或创建有效会话');
      }
      
      console.log('Using session ID:', sessionId);
      console.log('Adding waste API call...');
      
      const result = await wasteAPI.addWasteToSession(sessionId, wasteId, plannedAmount);
      
      console.log('Add waste API response:', result);
      
      // Check if the API call was successful
      if (result) {
        // Ensure consistent data types - convert to number
        const numericWasteId = parseInt(wasteId);
        const addedWasteIds = [...this.data.addedWasteIds.map(id => parseInt(id)), numericWasteId];
        const selectedWastes = [...this.data.selectedWastes.map(id => parseInt(id)), numericWasteId];
        
        console.log('Updated added waste IDs:', addedWasteIds);
        console.log('WasteId being added:', wasteId, 'converted to:', numericWasteId);
        
        this.setData({
          addedWasteIds: addedWasteIds,
          selectedWastes: selectedWastes,
          sessionId: sessionId,
          // Force re-render by updating timestamp
          _timestamp: Date.now()
        });
        
        // Store session ID globally
        getApp().globalData.currentSessionId = sessionId;
        
        // Save to local storage as backup
        try {
          wx.setStorageSync(`addedWastes_${this.data.sessionId}`, addedWasteIds);
        } catch (storageError) {
          console.error('Failed to save to storage:', storageError);
        }
        
        // Invalidate compatibility status in main page since waste list changed
        const pages = getCurrentPages();
        const mainPage = pages.find(page => page.route && page.route.includes('pages/compatibility/compatibility'));
        if (mainPage && mainPage.invalidateCompatibilityStatus) {
          mainPage.invalidateCompatibilityStatus('新增危废，需重新进行相容性检查');
        }
        
        wx.showToast({
          title: '添加成功',
          icon: 'success',
          duration: 1500
        });
      }
      
      // Don't navigate back - stay on import page
      
    } catch (error) {
      console.error('=== ADD WASTE FAILED ===');
      console.error('Error details:', error);
      
      // Handle specific error types
      if (error.message?.includes('会话') || error.message?.includes('Session')) {
        console.log('Session-related error detected');
        await this.handleSessionError(error);
      } else if (error.message?.includes('网络') || error.code === -1) {
        console.log('Network error detected');
        wx.showModal({
          title: '网络错误',
          content: '网络连接失败，请检查网络设置后重试',
          showCancel: true,
          cancelText: '取消',
          confirmText: '重试',
          success: (res) => {
            if (res.confirm) {
              // Retry the operation
              setTimeout(() => {
                this.addWasteToSession(wasteId, plannedAmount);
              }, 1000);
            }
          }
        });
      } else {
        // Generic error handling
        const errorMessage = error.message || '添加危废失败，请重试';
        wx.showModal({
          title: '添加失败',
          content: errorMessage,
          showCancel: false,
          confirmText: '确定'
        });
      }
    } finally {
      wx.hideLoading();
    }
  },

  // Load more data (pagination)
  onLoadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadWasteList(true);
    }
  },

  // Pull down refresh
  onPullDownRefresh() {
    this.setData({
      currentPage: 1,
      hasMore: true
    });
    
    this.loadWasteList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // Reach bottom - load more
  onReachBottom() {
    this.onLoadMore();
  },

  // Go back
  onBack() {
    wx.navigateBack();
  },

  // Page lifecycle
  onUnload() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
    }
  }
}); 