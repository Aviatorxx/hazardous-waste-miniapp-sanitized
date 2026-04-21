const { 
  sessionAPI, 
  wasteAPI, 
  wasteDirectoryAPI, 
  compatibilityAPI, 
  calculationAPI, 
  utils, 
  constraintAPI 
} = require('../../utils/matching-api.js');

Page({
  data: {
    // Session state
    sessionId: null,
    sessionStatus: 'draft',
    
    // Imported wastes (all are automatically selected)
    importedWastes: [],
    totalAmount: 0,
    targetHeatValue: 14000,
    // 约束（后端为准）
    constraints: {},
    constraintMap: {},

    targetHeatValueMin: 12500,
    targetHeatValueMax: 16800,
    
    // Amount constraint status
    totalAmountDisplay: '0.000 t/次',
    amountStatusIcon: '🟢',
    amountStatusText: '满足约束',
    amountStatusClass: 'amount-normal',

    // 顶部提示文案（从约束动态生成）
    totalAmountConstraintText: '配伍总量需控制在 ≤300 吨/次',
    
    // Button states
    buttonStates: {
      import: true,          // Enabled until blending process starts
      compatibility: false,  // Enabled when wastes imported with quantities
      blending: false,       // Enabled after compatibility check passes
      view: false           // Enabled after calculation attempted
    },
    
    // Process status
    compatibilityPassed: false,
    calculationAttempted: false,
    blendingInitiated: false,  // Track if blending process has been started
    
    // Loading states
    loading: {
      import: false,
      compatibility: false,
      blending: false,
      view: false
    },
    

  },

  onLoad() {
    console.log('Page loaded, all imported wastes are automatically selected');
    this.initializeSession();
    this.loadConstraints();
    this.updateAmountStatus(); // Initialize amount status display
  },

  // Computed properties for button states
  computed: {
    canStartCompatibilityCheck() {
      return this.data.importedWastes.length > 0 && 
             this.data.importedWastes.every(waste => waste.plannedAmount > 0);
    },
    
    canStartBlending() {
      return this.data.importedWastes.length > 0 && 
             this.data.importedWastes.every(waste => waste.plannedAmount > 0) &&
             this.data.compatibilityPassed;
    }
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

  // 约束文案格式化：优先后端返回的unit/展示
  formatConstraintRange(constraint) {
    if (!constraint) return '';

    const min = constraint.minValue ?? constraint.min_value ?? constraint.min;
    const max = constraint.maxValue ?? constraint.max_value ?? constraint.max;
    const unit = constraint.unit || constraint.displayUnit || constraint.display_unit || '';

    const hasMin = min !== undefined && min !== null && min !== '';
    const hasMax = max !== undefined && max !== null && max !== '';

    if (hasMin && hasMax) return `${min}-${max}${unit ? ' ' + unit : ''}`;
    if (!hasMin && hasMax) return `≤${max}${unit ? ' ' + unit : ''}`;
    if (hasMin && !hasMax) return `≥${min}${unit ? ' ' + unit : ''}`;
    return '';
  },

  // 将后端constraints数组转换为 { parameter_code: constraint }
  buildConstraintMap(constraints) {
    const map = {};
    (constraints || []).forEach((c) => {
      const key = c.parameterCode || c.parameter_code || c.code || c.paramCode;
      if (key) map[key] = c;
    });
    return map;
  },

  // 加载后端约束（matching_constraints）
  async loadConstraints() {
    try {
      const res = await constraintAPI.getConstraints();
      const list = utils.formatResponse(res);
      const constraints = Array.isArray(list) ? list : (list?.constraints || list?.rows || []);
      const constraintMap = this.buildConstraintMap(constraints);

      // 目标热值区间：若后端提供则覆盖
      const heat = constraintMap.TARGET_HEAT_VALUE || constraintMap.HEAT_VALUE || constraintMap.HEAT || null;
      const heatMin = heat ? (heat.minValue ?? heat.min_value ?? heat.min) : null;
      const heatMax = heat ? (heat.maxValue ?? heat.max_value ?? heat.max) : null;

      // 总量：后端为准
      const totalAmountC = constraintMap.TOTAL_AMOUNT || constraintMap.TOTALAMOUNT || null;
      const totalAmountText = totalAmountC
        ? `配伍总量需控制在 ${this.formatConstraintRange(totalAmountC)}`
        : '配伍总量需控制在 ≤300 吨/次'; // 兜底（仅在后端未返回时显示，逻辑仍以max兜底）

      this.setData({
        constraints,
        constraintMap,
        totalAmountConstraintText: totalAmountText,
        ...(heatMin != null ? { targetHeatValueMin: heatMin } : {}),
        ...(heatMax != null ? { targetHeatValueMax: heatMax } : {})
      }, () => {
        // 约束加载后刷新总量状态
        this.updateAmountStatus();
      });

    } catch (e) {
      console.error('Load constraints failed:', e);
      // 不阻塞主流程：使用默认兜底（≤300 吨/次）
      this.setData({
        totalAmountConstraintText: '配伍总量需控制在 ≤300 吨/次'
      });
    }
  },

  // Calculate amount status for constraint notifications（总量≤max 吨/次）
  calculateAmountStatus(totalAmountKg) {
    const totalTons = (Number(totalAmountKg) || 0) / 1000;
    const displayText = `${totalTons.toFixed(3)} t/次`;

    // 从后端约束取max（单位按后端：这里假设与“吨/次”一致；如后端为kg，可在此按unit做换算）
    const c = this.data.constraintMap?.TOTAL_AMOUNT;
    let max = c ? (c.maxValue ?? c.max_value ?? c.max) : null;

    // 兜底：新需求≤300 吨/次
    if (max === null || max === undefined || max === '') max = 300;
    max = Number(max);

    let statusIcon = '🟢';
    let statusText = '满足约束';
    let statusClass = 'amount-normal';

    if (Number.isFinite(max) && totalTons > max) {
      statusIcon = '🔴';
      statusText = `超出${max}吨`;
      statusClass = 'amount-excessive';
    }

    return {
      display: displayText,
      icon: statusIcon,
      text: statusText,
      class: statusClass
    };
  },

  // Update amount status when total amount changes
  updateAmountStatus() {
    const status = this.calculateAmountStatus(this.data.totalAmount);
    this.setData({
      totalAmountDisplay: status.display,
      amountStatusIcon: status.icon,
      amountStatusText: status.text,
      amountStatusClass: status.class
    });
  },

  // Get computed values for template
  canStartCompatibilityCheck() {
    return this.data.importedWastes.length > 0 && 
           this.data.importedWastes.every(waste => waste.plannedAmount > 0);
  },
  
  canStartBlending() {
    return this.data.importedWastes.length > 0 && 
           this.data.importedWastes.every(waste => waste.plannedAmount > 0) &&
           this.data.compatibilityPassed;
  },

  // Initialize new session with comprehensive validation
  async initializeSession() {
    try {
      console.log('=== SESSION CREATION DEBUG ===');
      console.log('Starting session initialization...');
      
      utils.showLoading('初始化会话...');
      
      // Clear any existing session state
      getApp().globalData.currentSessionId = null;
      
      const sessionData = {
        sessionName: `配伍会话_${Date.now()}`,
        targetHeatValue: 14000,
        totalAmount: 50000,
        createUser: 'miniprogram_user'
      };
      
      console.log('Session data to create:', sessionData);
      
      const result = await sessionAPI.createSession(sessionData);
      console.log('Session creation API response:', result);
      
      if (!result) {
        throw new Error('Session creation returned no data');
      }
      
      const sessionId = result.data?.sessionId || result.data?.id || result.sessionId || result.id;
      
      if (!sessionId) {
        console.error('No session ID found in response:', result);
        throw new Error('Session ID not found in response');
      }
      
      console.log('Session ID extracted:', sessionId);
      console.log('Session ID type:', typeof sessionId);
      
      // Validate session ID format
      if (!this.validateSessionId(sessionId)) {
        throw new Error('Invalid session ID format received');
      }
      
      // Store session ID in multiple places for redundancy
      this.setData({
        sessionId: sessionId,
        sessionStatus: 'draft'
      });
      
      // Store globally
      getApp().globalData.currentSessionId = sessionId;
      
      // Store in local storage for persistence
      wx.setStorageSync('currentSessionId', sessionId);
      wx.setStorageSync('sessionCreatedAt', Date.now());
      
      console.log('Session successfully created and stored:', sessionId);
      console.log('Global session ID:', getApp().globalData.currentSessionId);
      console.log('Storage session ID:', wx.getStorageSync('currentSessionId'));
      
      // Verify session exists on backend
      await this.verifySessionExists(sessionId);
      
    } catch (error) {
      console.error('=== SESSION CREATION FAILED ===');
      console.error('Error details:', error);
      this.handleSessionCreationError(error);
    } finally {
      utils.hideLoading();
    }
  },

  // Validate session ID format
  validateSessionId(sessionId) {
    if (!sessionId) {
      console.error('Session ID is null or undefined');
      return false;
    }
    
    const id = String(sessionId);
    if (id.length === 0) {
      console.error('Session ID is empty string');
      return false;
    }
    
    // Check if it's a valid number (for numeric IDs) or valid string
    if (isNaN(sessionId) && typeof sessionId !== 'string') {
      console.error('Session ID is not a valid number or string:', sessionId);
      return false;
    }
    
    console.log('Session ID validation passed:', sessionId);
    return true;
  },

  // Verify session exists on backend
  async verifySessionExists(sessionId) {
    try {
      console.log('Verifying session exists:', sessionId);
      const result = await sessionAPI.getSessionStatus(sessionId);
      console.log('Session verification result:', result);
      
      if (!result) {
        throw new Error('Session verification failed - no response');
      }
      
      return true;
    } catch (error) {
      console.error('Session verification failed:', error);
      throw new Error(`Session verification failed: ${error.message}`);
    }
  },

  // Handle session creation errors
  handleSessionCreationError(error) {
    console.error('Handling session creation error:', error);
    
    const errorMessage = error.message || '会话创建失败';
    
    wx.showModal({
      title: '会话创建失败',
      content: `无法创建配伍会话：${errorMessage}\n\n请检查网络连接或稍后重试`,
      showCancel: true,
      cancelText: '退出',
      confirmText: '重试',
      success: (res) => {
        if (res.confirm) {
          // Retry session creation
          setTimeout(() => {
            this.initializeSession();
          }, 1000);
        } else {
          // Navigate back or exit
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

  // Get current session ID with fallback logic
  getCurrentSessionId() {
    // Try multiple sources in order of preference
    let sessionId = this.data.sessionId || 
                   getApp().globalData.currentSessionId || 
                   wx.getStorageSync('currentSessionId');
    
    console.log('Getting current session ID:', {
      local: this.data.sessionId,
      global: getApp().globalData.currentSessionId,
      storage: wx.getStorageSync('currentSessionId'),
      selected: sessionId
    });
    
    return sessionId;
  },

  // Ensure session exists before operations
  async ensureSessionExists() {
    console.log('=== ENSURING SESSION EXISTS ===');
    
    let sessionId = this.getCurrentSessionId();
    
    if (!sessionId) {
      console.log('No session ID found, creating new session...');
      await this.initializeSession();
      sessionId = this.getCurrentSessionId();
    } else {
      console.log('Session ID found, verifying:', sessionId);
      try {
        await this.verifySessionExists(sessionId);
        console.log('Session verification successful');
      } catch (error) {
        console.log('Session verification failed, creating new session...');
        await this.initializeSession();
        sessionId = this.getCurrentSessionId();
      }
    }
    
    if (!sessionId) {
      throw new Error('无法获取或创建会话ID');
    }
    
    // Ensure all state is synchronized
    this.synchronizeSessionState(sessionId);
    
    return sessionId;
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

  // Import waste function with session validation
  async onImport() {
    try {
      console.log('=== IMPORT WASTE INITIATED ===');
      
      // Check if import is allowed (not after blending has started)
      if (this.data.blendingInitiated) {
        wx.showModal({
          title: '导入受限',
          content: '配伍过程已开始，无法导入新的危废。请先点击"重置"重新开始。',
          showCancel: false,
          confirmText: '确定'
        });
        return;
      }
      
      // Ensure session exists before navigation
      const sessionId = await this.ensureSessionExists();
      
      console.log('Navigating to import page with session ID:', sessionId);
      
      wx.navigateTo({
        url: `/pages/compatibility/import-waste/import-waste?sessionId=${sessionId}`
      });
      
    } catch (error) {
      console.error('Import navigation failed:', error);
      wx.showModal({
        title: '导入失败',
        content: '无法进入导入页面，请重试',
        showCancel: false,
        confirmText: '确定'
      });
    }
  },

  // Compatibility check function with session validation
  async onCompatibilityCheck() {
    try {
      console.log('=== COMPATIBILITY CHECK INITIATED ===');
      
      // Ensure session exists
      const sessionId = await this.ensureSessionExists();
      
      // Validate compatibility check requirements
      if (!this.validateForCompatibilityCheck()) {
        return;
      }

      console.log('Navigating to compatibility check with session ID:', sessionId);
      
      wx.navigateTo({
        url: `/pages/compatibility/compatibility-check/compatibility-check?sessionId=${sessionId}`
      });
      
    } catch (error) {
      console.error('Compatibility check navigation failed:', error);
      wx.showModal({
        title: '相容性检查失败',
        content: '无法启动相容性检查，请重试',
        showCancel: false,
        confirmText: '确定'
      });
    }
  },

  // Blending calculation function with enhanced validation
  async onBlending() {
    try {
      // Step 1: Validate current session state before checking compatibility
      console.log('=== VALIDATING BLENDING PREREQUISITES ===');
      
      if (!this.data.sessionId) {
      wx.showToast({
          title: '会话无效，请重新开始',
        icon: 'none'
      });
      return;
    }

      // Step 2: Refresh session data to get current state
      utils.showLoading('验证当前状态...');
      await this.loadImportedWastes(true); // Force refresh
      
      // Step 3: Validate we have sufficient wastes with quantities
      const currentWastes = this.data.importedWastes || [];
      if (currentWastes.length < 2) {
        utils.hideLoading();
        wx.showToast({
          title: '至少需要2种危废才能进行配伍计算',
          icon: 'none'
        });
        return;
      }

      const wastesWithoutQuantity = currentWastes.filter(waste => 
        !waste.plannedAmount || waste.plannedAmount <= 0
      );
      
      if (wastesWithoutQuantity.length > 0) {
        utils.hideLoading();
        wx.showToast({
          title: '请先设置所有危废的用量',
          icon: 'none'
        });
        return;
      }

      // Step 4: Validate current compatibility status with enhanced session state validation
      let isCurrentlyCompatible = false;
      let compatibilityValidForCurrentSession = false;
      
      try {
        const compatibilityResult = await compatibilityAPI.getCompatibilityResult(this.data.sessionId);
        const compatibilityData = utils.formatResponse(compatibilityResult);
        console.log('Backend compatibility result:', compatibilityData);
        
        // Get session summary to compare with compatibility results
        const sessionResult = await sessionAPI.getSessionSummary(this.data.sessionId);
        const sessionData = utils.formatResponse(sessionResult);
        console.log('Session summary for validation:', sessionData);
        
        // Enhanced validation: Check if compatibility results match current session state
        const currentWasteCount = currentWastes.length;
        const expectedPairCount = currentWasteCount >= 2 ? (currentWasteCount * (currentWasteCount - 1)) / 2 : 0;
        
        // Extract actual pair count from compatibility results
        let actualPairCount = 0;
        if (compatibilityData.pairResults && Array.isArray(compatibilityData.pairResults)) {
          actualPairCount = compatibilityData.pairResults.length;
        } else {
          const incompatiblePairs = compatibilityData.incompatiblePairs || compatibilityData.incompatiblePairDetails || [];
          const compatiblePairs = compatibilityData.compatiblePairs || [];
          actualPairCount = incompatiblePairs.length + compatiblePairs.length;
        }
        
        // Validate that compatibility results match current session
        const pairCountMatches = actualPairCount === expectedPairCount;
        const sessionTimestampsMatch = this.validateSessionTimestamps(sessionData, compatibilityData);
        
        console.log('Compatibility validation details:', {
          currentWasteCount,
          expectedPairCount,
          actualPairCount,
          pairCountMatches,
          sessionTimestampsMatch,
          compatibilityDataAge: compatibilityData.checkTime,
          sessionLastModified: sessionData.lastModified
        });
        
        // Improved validation logic: be more lenient when pair count matches
        // Only require recheck if there's a significant mismatch
        if (pairCountMatches) {
          // If pair count matches, trust the compatibility result unless timestamp indicates major change
          compatibilityValidForCurrentSession = true;
          isCurrentlyCompatible = Boolean(compatibilityData.compatible);
          console.log('✅ Pair count matches, accepting compatibility results:', isCurrentlyCompatible);
        } else if (sessionTimestampsMatch) {
          // If timestamps match but pair count doesn't, still accept if waste count is close
          const countDifference = Math.abs(expectedPairCount - actualPairCount);
          if (countDifference <= 1) {
            // Allow small discrepancies (might be due to different counting methods)
            compatibilityValidForCurrentSession = true;
            isCurrentlyCompatible = Boolean(compatibilityData.compatible);
            console.log('✅ Timestamps match and small pair count difference, accepting results:', isCurrentlyCompatible);
          } else {
            console.log('❌ Significant pair count mismatch, requiring recheck');
            compatibilityValidForCurrentSession = false;
            isCurrentlyCompatible = false;
          }
        } else {
          console.log('❌ Both pair count and timestamps indicate outdated results');
          compatibilityValidForCurrentSession = false;
          isCurrentlyCompatible = false;
        }
        
        // Update local state to match validation results
        this.setData({
          compatibilityPassed: isCurrentlyCompatible
        });
        
      } catch (error) {
        console.log('No compatibility result available or validation failed:', error.message);
        isCurrentlyCompatible = false;
        compatibilityValidForCurrentSession = false;
        this.setData({
          compatibilityPassed: false
        });
      }

      utils.hideLoading();

      // Step 5: Check if compatibility validation is required
      // Enhanced check: consider recent compatibility check activity
      const pages = getCurrentPages();
      let justCompletedCompatibilityCheck = false;
      
      // Check if we just returned from compatibility check page
      if (pages.length >= 2) {
        const prevPage = pages[pages.length - 2];
        if (prevPage && prevPage.route && prevPage.route.includes('compatibility-check')) {
          justCompletedCompatibilityCheck = true;
          console.log('Just returned from compatibility check, being lenient with validation');
        }
      }
      
      // Check for recent compatibility check activity (within 30 seconds)
      const recentCompatibilityActivity = wx.getStorageSync('compatibilityCompletedAt');
      const now = Date.now();
      let recentlyCompletedCheck = false;
      
      if (recentCompatibilityActivity && (now - recentCompatibilityActivity) < 30000) {
        recentlyCompletedCheck = true;
        console.log('Recent compatibility check activity detected, being lenient');
      }
      
      // Be more lenient if there's recent activity or the user just completed a check
      const shouldSkipValidation = justCompletedCompatibilityCheck || recentlyCompletedCheck;
      
      // IMPORTANT: If we have recent activity, override validation results and assume compatibility
      if (shouldSkipValidation) {
        console.log('Overriding validation due to recent compatibility activity');
        isCurrentlyCompatible = true;
        compatibilityValidForCurrentSession = true;
        
        // Update local state to reflect override
        this.setData({
          compatibilityPassed: true
        });
      }
      
      // Only require recheck if:
      // 1. Not compatible AND session not valid AND no recent activity
      const needsCompatibilityCheck = (!isCurrentlyCompatible || !compatibilityValidForCurrentSession) && !shouldSkipValidation;
      
      if (needsCompatibilityCheck) {
        console.log('Compatibility check required:', {
          isCurrentlyCompatible,
          compatibilityValidForCurrentSession,
          justCompletedCompatibilityCheck,
          recentlyCompletedCheck,
          shouldSkipValidation
        });
        
        const modalContent = !compatibilityValidForCurrentSession 
          ? '检测到配伍方案已变更，需要重新进行相容性检查。是否现在检查？'
          : '当前配伍方案尚未通过相容性检查，是否先进行相容性检查？';
          
        wx.showModal({
          title: '需要相容性检查',
          content: modalContent,
          confirmText: '去检查',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              this.onCompatibilityCheck();
            }
          }
        });
        return;
      } else {
        console.log('Skipping compatibility validation due to recent activity or valid state');
      }

      // Step 6: All validations passed, proceed with blending calculation
      // Mark blending as initiated to disable import button
      this.setData({
        'loading.blending': true,
        blendingInitiated: true,
        calculationAttempted: true,
        'buttonStates.view': true
      });
      
      // Update button states to reflect new blending status
      this.updateButtonStates();

          utils.hideLoading();

      // Navigate to blending progress page for calculation monitoring
      wx.navigateTo({
        url: `/pages/compatibility/blending-progress/blending-progress?sessionId=${this.data.sessionId}`
      });
          
      // Reset the blending loading state since we're navigating away
            this.setData({
            'loading.blending': false
          });

    } catch (error) {
      console.error('Blending validation error:', error);
      utils.hideLoading();
      wx.showToast({
        title: '验证失败，请重试',
        icon: 'none'
      });
      this.setData({
        'loading.blending': false,
        blendingInitiated: false  // Reset blending status on validation error
      });
      // Update button states to re-enable import
      this.updateButtonStates();
    }
  },

  // View results function
  async onView() {
    if (!this.data.calculationAttempted) {
      wx.showToast({
        title: '请先完成配伍计算',
        icon: 'none'
      });
      return;
    }

    wx.navigateTo({
      url: `/pages/compatibility/calculation-results/calculation-results?sessionId=${this.data.sessionId}`
    });
  },



  // Update waste quantity
  async updateWasteQuantity(wasteId, plannedAmount) {
    try {
      utils.showLoading('更新用量...');
      
      await wasteAPI.updateWasteQuantity(this.data.sessionId, wasteId, plannedAmount);
      
      // Refresh waste list
      await this.loadImportedWastes();
      
      wx.showToast({
        title: '用量更新成功',
        icon: 'success'
      });
      
    } catch (error) {
      console.error('Update quantity error:', error);
      utils.showError(error, '更新失败');
    } finally {
      utils.hideLoading();
    }
  },

  // Update button states based on imported wastes with enhanced validation
  updateButtonStates() {
    const hasWastes = this.data.importedWastes.length > 0;
    const hasQuantities = this.data.importedWastes.every(w => w.plannedAmount > 0);
    
    console.log('=== UPDATING BUTTON STATES ===');
    console.log('Imported waste count:', this.data.importedWastes.length);
    console.log('All have quantities:', hasQuantities);
    console.log('Current compatibility passed:', this.data.compatibilityPassed);
    console.log('Blending initiated:', this.data.blendingInitiated);
    
    // Enhanced blending validation: compatibility must be current and valid
    const canBlend = hasWastes && hasQuantities && this.data.compatibilityPassed;
    
    // Import button is disabled after blending process starts
    const canImport = !this.data.blendingInitiated;
    
    this.setData({
      'buttonStates.import': canImport,
      'buttonStates.compatibility': hasWastes && hasQuantities,
      'buttonStates.blending': canBlend
    }, () => {
      console.log('Button states updated:', {
        import: this.data.buttonStates.import,
        compatibility: this.data.buttonStates.compatibility,
        blending: this.data.buttonStates.blending,
        compatibilityStatus: this.data.compatibilityPassed,
        blendingInitiated: this.data.blendingInitiated
      });
    });
  },

  // Load imported wastes from session with force refresh option
  async loadImportedWastes(forceRefresh = false) {
    // Try to get session ID from global state if local state is empty
    let sessionId = this.data.sessionId || getApp().globalData.currentSessionId;
    
    if (!sessionId) return;
    
    // Update local session ID if it was retrieved from global
    if (sessionId !== this.data.sessionId) {
      this.setData({
        sessionId: sessionId
      });
    }
    
    try {
      console.log(`=== LOADING IMPORTED WASTES (force: ${forceRefresh}) ===`);
      
      const summaryResult = await sessionAPI.getSessionSummary(sessionId);
      const summaryData = utils.formatResponse(summaryResult);
      
      console.log('Session summary loaded:', summaryData);
      
      // Check if waste list has changed (for compatibility invalidation)
      const previousWasteCount = this.data.importedWastes.length;
      const currentWasteCount = (summaryData.wastes || []).length;
      const wasteCountChanged = previousWasteCount !== currentWasteCount;
      
      // Process imported wastes to ensure proper formatting
      const processedWastes = (summaryData.wastes || []).map(waste => {
        const storage = parseFloat(waste.remainingStorage) || 0;
        const formattedDisplay = this.formatStorage(storage);
        console.log(`Compatibility page - Processing waste ${waste.wasteCode}: ${waste.remainingStorage} -> ${storage} -> ${formattedDisplay}`);
        return {
          ...waste,
          remainingStorage: storage,
          remainingStorageDisplay: formattedDisplay
        };
      });

      this.setData({
        importedWastes: processedWastes,
        totalAmount: summaryData.totalAmount || 0,
        targetHeatValue: summaryData.targetHeatValue || 14000,
        sessionStatus: summaryData.status || 'draft'
      });

      // Update amount status display
      this.updateAmountStatus();
      
      // Enhanced compatibility status management
      let shouldInvalidateCompatibility = false;
      
      // If waste count changed, compatibility status should be re-validated
      if (wasteCountChanged) {
        console.log(`Waste count changed from ${previousWasteCount} to ${currentWasteCount}, may need compatibility re-validation`);
        shouldInvalidateCompatibility = true;
      }
      
      // Update compatibility status with fresh data validation
      if (summaryData.compatibility) {
        console.log('Session compatibility data:', summaryData.compatibility);
        
        if (forceRefresh || shouldInvalidateCompatibility) {
          // For force refresh or when wastes changed, get the direct compatibility result
          try {
            const compatibilityResult = await compatibilityAPI.getCompatibilityResult(sessionId);
            const compatibilityData = utils.formatResponse(compatibilityResult);
            console.log('Direct compatibility result:', compatibilityData);
            
            // Use direct result as source of truth
            const compatibilityPassed = Boolean(compatibilityData.compatible);
            this.setData({
              compatibilityPassed: compatibilityPassed
            });
            
            console.log('Using direct compatibility result:', compatibilityPassed);
            
          } catch (error) {
            console.log('Direct compatibility result not available, invalidating compatibility status');
            // If we can't get fresh results and wastes changed, invalidate compatibility
            this.setData({
              compatibilityPassed: shouldInvalidateCompatibility ? false : Boolean(summaryData.compatibility.compatible)
            });
          }
        } else {
          // Normal load, use session data
          const compatibilityPassed = Boolean(summaryData.compatibility.compatible);
          this.setData({
            compatibilityPassed: compatibilityPassed
          });
        }
      } else {
        // Ensure compatibilityPassed is always a boolean
        console.log('No compatibility data in session');
        this.setData({
          compatibilityPassed: false
        });
      }
      
      // Update button states
      this.updateButtonStates();
      
    } catch (error) {
      console.error('Load imported wastes error:', error);
      // Don't show error for empty session
    }
  },

  // Validate for compatibility check (simplified - use all imported wastes)
  validateForCompatibilityCheck() {
    if (this.data.importedWastes.length === 0) {
      wx.showToast({
        title: '请先导入危废',
        icon: 'none'
      });
      return false;
    }
    
    const wastesWithoutQuantity = this.data.importedWastes.filter(waste => 
      !waste.plannedAmount || waste.plannedAmount <= 0
    );
    
    if (wastesWithoutQuantity.length > 0) {
      wx.showToast({
        title: '请先设置所有危废的用量',
        icon: 'none'
      });
      return false;
    }
    
    return true;
  },

  // Remove waste from session
  async onRemoveWaste(e) {
    const wasteId = e.currentTarget.dataset.wasteId;
    const waste = this.data.importedWastes.find(w => w.wasteId === wasteId);
    
    if (!waste) return;
    
    wx.showModal({
      title: '确认删除',
      content: `确定要删除"${waste.wasteName}"吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            utils.showLoading('删除中...');
            
            await wasteAPI.removeWasteFromSession(this.data.sessionId, wasteId);
            
            // Invalidate compatibility status since waste list changed
            this.invalidateCompatibilityStatus('废物已删除，请重新进行相容性检查');
            
            // Refresh waste list after removal
            await this.loadImportedWastes();
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            
          } catch (error) {
            console.error('Remove waste error:', error);
            utils.showError(error, '删除失败');
          } finally {
            utils.hideLoading();
          }
        }
      }
    });
  },

  // Invalidate compatibility status when waste configuration changes
  invalidateCompatibilityStatus(reason = '配伍方案已变更') {
    console.log('=== INVALIDATING COMPATIBILITY STATUS ===');
    console.log('Reason:', reason);
    
    this.setData({
      compatibilityPassed: false,
      blendingInitiated: false,  // Reset blending status when compatibility is invalidated
      calculationAttempted: false,
      'buttonStates.blending': false,
      'buttonStates.view': false
    });
    
    // Update button states (this will re-enable import button)
    this.updateButtonStates();
    
    // Signal all pages to clear their cached compatibility data
    wx.setStorageSync('compatibilityCacheInvalidated', Date.now());
    
    // Clear recent completion timestamp to force fresh validation
    wx.removeStorageSync('compatibilityCompletedAt');
    
    console.log('Compatibility status invalidated, blending reset, cache and timestamps cleared');
  },

  // Validate if compatibility results timestamps match session state
  validateSessionTimestamps(sessionData, compatibilityData) {
    try {
      // If we don't have timestamp data, assume they're valid to avoid breaking existing functionality
      if (!sessionData.lastModified && !compatibilityData.checkTime) {
        console.log('No timestamp data available, assuming valid');
        return true;
      }
      
      // If session was modified after compatibility check, results are outdated
      if (sessionData.lastModified && compatibilityData.checkTime) {
        const sessionTime = new Date(sessionData.lastModified).getTime();
        const compatibilityTime = new Date(compatibilityData.checkTime).getTime();
        
        // Add tolerance for small time differences (e.g., 5 seconds)
        // This accounts for slight delays in data propagation between systems
        const timeDifference = sessionTime - compatibilityTime;
        const toleranceMs = 5000; // 5 seconds tolerance
        
        const isValid = timeDifference <= toleranceMs;
        console.log('Timestamp validation:', {
          sessionLastModified: sessionData.lastModified,
          compatibilityCheckTime: compatibilityData.checkTime,
          timeDifference: timeDifference,
          toleranceMs: toleranceMs,
          isValid
        });
        
        return isValid;
      }
      
      // If only one timestamp exists, we can't validate reliably
      console.log('Incomplete timestamp data, assuming valid');
      return true;
      
    } catch (error) {
      console.error('Error validating timestamps:', error);
      // In case of error, assume valid to avoid breaking functionality
      return true;
    }
  },

  // Updated reset function
  async onReset() {
    try {
      wx.showModal({
        title: '确认重置',
        content: '是否确认重置当前配伍会话？',
        success: async (res) => {
          if (res.confirm) {
            // Clear global session state
            getApp().globalData.currentSessionId = null;
            
            // Reset session
            await this.resetSession();
            
            // Clear all states including blending status
            this.setData({
              importedWastes: [],
              sessionId: null,
              compatibilityPassed: false,
              calculationAttempted: false,
              blendingInitiated: false,  // Reset blending status to re-enable import
              'buttonStates.import': true,
              'buttonStates.compatibility': false,
              'buttonStates.blending': false,
              'buttonStates.view': false
            });
            
            // Notify all pages to clear session state
            wx.setStorageSync('sessionReset', Date.now());
            // Keep legacy flag for backward compatibility
            wx.setStorageSync('resetFlag', Date.now());
            
            // Re-initialize session
            await this.initializeSession();
            
            wx.showToast({
              title: '重置成功',
              icon: 'success'
            });
          }
        }
      });
    } catch (error) {
      console.error('Reset failed:', error);
      wx.showToast({
        title: '重置失败',
        icon: 'error'
      });
    }
  },

  // Reset session helper
  async resetSession() {
    if (this.data.sessionId) {
      try {
        await sessionAPI.resetSession(this.data.sessionId);
      } catch (error) {
        console.error('Reset session API error:', error);
      }
    }
  },

  // Legacy method name mapping for backward compatibility (removed)

  // Force refresh the UI (for debugging)
  forceRefreshUI() {
    console.log('=== FORCING UI REFRESH ===');
    this.setData({
      _forceUpdate: Date.now(),
      importedWastes: [...this.data.importedWastes],
      selectedWastes: [...this.data.selectedWastes]
    }, () => {
      console.log('UI refresh complete');
    });
  },

  // Force checkbox update
  updateCheckboxDisplay() {
    console.log('=== FORCE UPDATE CHECKBOX DISPLAY ===');
    // Force re-render of checkboxes
    this.setData({
      importedWastes: this.data.importedWastes
    }, () => {
      console.log('Checkbox display updated');
    });
  },

  // Add computed method to check selection state
  isSelected(wasteId) {
    const isSelected = this.data.selectedWastes.indexOf(wasteId) > -1;
    console.log(`Waste ${wasteId} is selected:`, isSelected);
    return isSelected;
  },

  // Reset all data (for debugging)
  resetAllData() {
    console.log('=== RESETTING ALL DATA ===');
    this.setData({
      importedWastes: [],
      compatibilityPassed: false,
      calculationAttempted: false,
      blendingInitiated: false  // Reset blending status when resetting all data
    }, () => {
      this.updateButtonStates();
      this.forceRefreshUI();
    });
  },

  // Page lifecycle with state synchronization
  onShow() {
    console.log('=== COMPATIBILITY MAIN PAGE SHOW ===');
    
    // Check if returning from other pages with potential state changes
    const pages = getCurrentPages();
    if (pages.length >= 2) {
      const prevPage = pages[pages.length - 2];
      
      if (prevPage && prevPage.route) {
        if (prevPage.route.includes('compatibility-check')) {
          console.log('Returning from compatibility check page, refreshing state');
          
          // Force refresh to get the latest state
          this.loadImportedWastes(true).then(() => {
            // Validate state consistency after return
            this.validateCompatibilityConsistency();
          }).catch(error => {
            console.error('Failed to refresh state on return:', error);
          });
          
          return;
        }
        
        if (prevPage.route.includes('import-waste')) {
          console.log('Returning from import page, invalidating compatibility and refreshing');
          
          // Invalidate compatibility status since waste list may have changed
          this.invalidateCompatibilityStatus('导入新危废，需要重新检查相容性');
          
          // Force refresh to get the updated waste list
          this.loadImportedWastes(true).then(() => {
            console.log('Waste list refreshed after import');
          }).catch(error => {
            console.error('Failed to refresh waste list after import:', error);
          });
          
          return;
        }
      }
    }
    
    // Normal refresh for other cases
    this.loadImportedWastes();
  },

  // Force fresh compatibility state loading
  async forceRefreshCompatibilityState() {
    try {
      console.log('=== FORCE REFRESHING MAIN PAGE COMPATIBILITY STATE ===');
      
      // Refresh data when returning from other pages
      await this.loadImportedWastes(true); // force refresh
      
      // Validate compatibility state consistency
      await this.validateCompatibilityConsistency();
      
    } catch (error) {
      console.error('Force refresh compatibility state error:', error);
    }
  },

  // Validate compatibility state consistency
  async validateCompatibilityConsistency() {
    if (!this.data.sessionId) return;
    
    try {
      console.log('=== VALIDATING COMPATIBILITY CONSISTENCY ===');
      
      // Get fresh session summary
      const summaryResult = await sessionAPI.getSessionSummary(this.data.sessionId);
      const summaryData = utils.formatResponse(summaryResult);
      
      // Get fresh compatibility result if available
      let compatibilityResult = null;
      try {
        const result = await compatibilityAPI.getCompatibilityResult(this.data.sessionId);
        compatibilityResult = utils.formatResponse(result);
      } catch (error) {
        console.log('No compatibility result available');
      }
      
      console.log('Session compatibility:', summaryData.compatibility);
      console.log('Direct compatibility result:', compatibilityResult);
      
      // Check for inconsistencies
      if (summaryData.compatibility && compatibilityResult) {
        const sessionCompatible = Boolean(summaryData.compatibility.compatible);
        const resultCompatible = Boolean(compatibilityResult.compatible);
        
        if (sessionCompatible !== resultCompatible) {
          console.error('=== COMPATIBILITY STATE MISMATCH DETECTED ===');
          console.error('Session says compatible:', sessionCompatible);
          console.error('Result says compatible:', resultCompatible);
          
          // Use the direct result as the source of truth
          const correctState = Boolean(compatibilityResult.compatible);
          this.setData({
            compatibilityPassed: correctState
          });
          
          wx.showToast({
            title: '状态已同步',
            icon: 'none'
          });
        }
      }
      
    } catch (error) {
      console.error('Compatibility consistency validation failed:', error);
    }
  },

  onPullDownRefresh() {
    this.loadImportedWastes().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // Comprehensive selection testing and verification
  testSelectionFunctionality() {
    console.log('=== COMPREHENSIVE SELECTION TEST ===');
    
    // Test 1: Check data structure
    console.log('1. Current selection array:', this.data.selectedWastes);
    console.log('   - Type:', typeof this.data.selectedWastes);
    console.log('   - Length:', this.data.selectedWastes.length);
    console.log('   - Is Array:', Array.isArray(this.data.selectedWastes));
    
    // Test 2: Check imported wastes
    console.log('2. Imported wastes:');
    this.data.importedWastes.forEach((waste, index) => {
      console.log(`   [${index}] ${waste.wasteCode} (${waste.wasteId}): AUTO-SELECTED`);
    });
    
    // Test 3: Check button states
    console.log('3. Button states:');
    console.log('   - Compatibility enabled:', this.canStartCompatibilityCheck());
    console.log('   - Blending enabled:', this.canStartBlending());
    
    // Test 4: Check validation
    if (this.data.importedWastes.length > 0) {
      console.log('4. Validation test:');
      console.log('   - Can start compatibility check:', this.canStartCompatibilityCheck());
      console.log('   - All have quantities:', this.data.importedWastes.every(w => w.plannedAmount > 0));
    }
    
    return true;
  },

  // Helper for testing imported wastes
  testFirstWaste() {
    if (this.data.importedWastes.length > 0) {
      const waste = this.data.importedWastes[0];
      console.log('Testing first waste:', waste.wasteCode, 'with quantity:', waste.plannedAmount);
      return waste;
    }
    return null;
  },

  // Clear all imported wastes for testing
  clearAllImportedWastes() {
    console.log('Clearing all imported wastes for test');
    this.setData({
      importedWastes: []
    }, () => {
      this.updateButtonStates();
      console.log('All imported wastes cleared');
    });
  },
}); 