// pages/compatibility/compatibility-check/compatibility-check.js
const { compatibilityAPI, sessionAPI, wasteAPI, utils } = require('../../../utils/matching-api.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    sessionId: null,
    sessionData: null,
    compatibilityResults: null,
    checkStatus: 'idle', // idle, checking, completed, failed
    compatibilityPassed: null, // Changed from false to null to avoid false "failed" display
    incompatiblePairs: [],
    compatiblePairs: [], // Add compatible pairs to initial data
    riskFactors: [],
    loading: false,
    canProceed: null, // Changed from true to null to avoid false states
    
    // Add compatibility summary data for better display
    compatibility: {
      checked: false,
      compatible: null, // Changed from false to null
      incompatiblePairs: 0,
      riskFactors: []
    },
    
    // Error handling
    errorMessage: '',
    hasError: false,
    
    // Polling control
    pollingInterval: null,
    pollingCount: 0,
    maxPollingAttempts: 30, // 30 attempts = 60 seconds max
    
    // State synchronization control
    compatibilityCheckInProgress: false,
    lastStateRefresh: 0,
    stateDebugMode: true, // Enable detailed logging
    sessionWastes: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.sessionId) {
      this.setData({
        sessionId: options.sessionId
      });
      this.initializeCompatibilityPage();
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    console.log('=== COMPATIBILITY CHECK PAGE SHOW ===');
    
    // ALWAYS force refresh when page shows to ensure we have the latest data
    // This is critical when wastes have been added/removed from the session
    console.log('Forcing refresh of session data and clearing cached results');
    
    // Check if compatibility cache has been invalidated by another page
    const cacheInvalidatedTime = wx.getStorageSync('compatibilityCacheInvalidated');
    const lastCacheCheck = this.data.lastCacheCheck || 0;
    
    if (cacheInvalidatedTime && cacheInvalidatedTime > lastCacheCheck) {
      console.log('Compatibility cache was invalidated by another page, clearing all data');
      this.setData({
        lastCacheCheck: Date.now()
      });
    }
    
    // Clear ALL cached compatibility data first to prevent stale results
    this.clearCompatibilityCache();
    
    // Force refresh session data to get current waste list
    this.loadSessionData(true).then(() => {
      console.log('Session data refreshed on page show');
      
      // Re-run intelligent check with fresh data
      // This will either fetch valid existing results or start a new check
      this.intelligentCompatibilityCheck();
      
    }).catch(error => {
      console.error('Failed to refresh session data on show:', error);
      this.setData({
        hasError: true,
        errorMessage: '无法加载会话数据，请重试'
      });
    });
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {
    // Clear polling when page is hidden
    this.clearPolling();
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {
    // Clear polling when page is unloaded
    this.clearPolling();
  },

  // Initialize compatibility page with enhanced auto-check logic
  async initializeCompatibilityPage() {
    console.log('=== INITIALIZING COMPATIBILITY PAGE WITH AUTO-CHECK ===');
    console.log('Session ID:', this.data.sessionId);
    
    // Test the wastesCombination parsing logic
    const testPassed = this.testWastesCombinationParsing();
    console.log('wastesCombination parsing test passed:', testPassed);
    
    if (!this.data.sessionId) {
      this.setData({
        hasError: true,
        errorMessage: '会话ID无效'
      });
      return;
    }

    try {
      // ALWAYS force refresh session data to get current state
      // This ensures we have the latest waste list after any modifications
      await this.loadSessionData(true);
      
      // Enhanced compatibility state management with auto-check
      await this.intelligentCompatibilityCheck();
      
    } catch (error) {
      console.error('Initialize compatibility page error:', error);
      this.handleCompatibilityError(error, 'initialization');
    }
    
    console.log('=== COMPATIBILITY PAGE INITIALIZATION COMPLETE ===');
  },

  // Intelligent compatibility checking - auto-fetch existing or auto-start new check
  async intelligentCompatibilityCheck() {
    console.log('=== INTELLIGENT COMPATIBILITY CHECK ===');
    
    // Step 1: Check if we have sufficient wastes for compatibility checking
    const sessionWastes = this.data.sessionWastes || [];
    if (sessionWastes.length < 2) {
      console.log('Insufficient wastes for compatibility check:', sessionWastes.length);
      this.setData({
        checkStatus: 'idle',
        hasError: true,
        errorMessage: '至少需要2种危废才能进行相容性检查'
      });
      return;
    }
    
    console.log(`Found ${sessionWastes.length} wastes, proceeding with compatibility check logic`);
    
    // Step 2: Try to fetch existing compatibility results with session state validation
    const existingResults = await this.tryFetchExistingResults();
    
    if (existingResults) {
      console.log('✅ Found valid existing compatibility results, displaying them');
      this.updateCompatibilityState(existingResults);
      return;
    }
    
    // Step 3: No valid existing results, ALWAYS start a new check
    // This ensures fresh results that match the current session state
    console.log('❌ No valid results found or session state changed, starting fresh compatibility check');
    await this.autoStartCompatibilityCheck();
  },

  // Try to fetch existing compatibility results with session state validation
  async tryFetchExistingResults() {
    try {
      console.log('=== TRYING TO FETCH EXISTING RESULTS ===');
      
      const result = await compatibilityAPI.getCompatibilityResult(this.data.sessionId);
      const resultsData = utils.formatResponse(result);
      
      if (resultsData && (resultsData.pairResults || resultsData.incompatiblePairs || resultsData.compatible !== undefined)) {
        console.log('Found existing compatibility results:', resultsData);
        
        // CRITICAL: Validate that existing results match current session state
        const currentWastes = this.data.sessionWastes || [];
        const isResultsValid = this.validateCompatibilityResultsForCurrentSession(resultsData, currentWastes);
        
        if (isResultsValid) {
          console.log('✅ Existing results are valid for current session state');
          
          // Additional verification: check if result pair count matches expected count
          const currentWastes = this.data.sessionWastes || [];
          const expectedPairCount = currentWastes.length >= 2 ? (currentWastes.length * (currentWastes.length - 1)) / 2 : 0;
          
          // Count actual pairs in results
          let actualPairCount = 0;
          if (resultsData.pairResults && Array.isArray(resultsData.pairResults)) {
            actualPairCount = resultsData.pairResults.length;
          } else {
            const incompatiblePairs = resultsData.incompatiblePairs || [];
            const compatiblePairs = resultsData.compatiblePairs || [];
            actualPairCount = incompatiblePairs.length + compatiblePairs.length;
          }
          
          if (expectedPairCount !== actualPairCount) {
            console.log('❌ Pair count mismatch in existing results, rejecting them');
            console.log('Expected pairs:', expectedPairCount, 'Actual pairs:', actualPairCount);
            return null;
          }
          
          return resultsData;
        } else {
          console.log('❌ Existing results are outdated for current session state, ignoring them');
          return null;
        }
      } else {
        console.log('API returned empty or invalid results');
        return null;
      }
      
    } catch (error) {
      console.log('No existing compatibility results available (expected for new sessions):', error.message);
      return null;
    }
  },

  // Validate if compatibility results match current session waste configuration
  validateCompatibilityResultsForCurrentSession(resultsData, currentWastes) {
    console.log('=== VALIDATING COMPATIBILITY RESULTS AGAINST CURRENT SESSION ===');
    console.log('Current session wastes:', currentWastes.length, currentWastes.map(w => w.wasteCode));
    
    // Extract waste codes from current session
    const currentWasteCodes = new Set(currentWastes.map(w => w.wasteCode).filter(Boolean));
    console.log('Current waste codes:', Array.from(currentWasteCodes));
    
    // Calculate expected number of pairs for current waste count
    const currentWasteCount = currentWastes.length;
    const expectedPairCount = currentWasteCount >= 2 ? (currentWasteCount * (currentWasteCount - 1)) / 2 : 0;
    
    // Get actual pair count from results
    let actualPairCount = 0;
    let resultWasteCodes = new Set();
    
    // Extract pair information from results
    if (resultsData.pairResults && Array.isArray(resultsData.pairResults)) {
      actualPairCount = resultsData.pairResults.length;
      
      // Extract waste codes from pair results
      resultsData.pairResults.forEach(pair => {
        const code1 = this.findWasteCode(pair, 'waste1');
        const code2 = this.findWasteCode(pair, 'waste2');
        if (code1) resultWasteCodes.add(code1);
        if (code2) resultWasteCodes.add(code2);
      });
    } else {
      // For other result formats, check incompatible and compatible pairs
      const incompatiblePairs = resultsData.incompatiblePairs || resultsData.incompatiblePairDetails || [];
      const compatiblePairs = resultsData.compatiblePairs || [];
      actualPairCount = incompatiblePairs.length + compatiblePairs.length;
      
      [...incompatiblePairs, ...compatiblePairs].forEach(pair => {
        const code1 = this.findWasteCode(pair, 'waste1');
        const code2 = this.findWasteCode(pair, 'waste2');
        if (code1) resultWasteCodes.add(code1);
        if (code2) resultWasteCodes.add(code2);
      });
    }
    
    console.log('Expected pair count for current session:', expectedPairCount);
    console.log('Actual pair count in results:', actualPairCount);
    console.log('Waste codes in results:', Array.from(resultWasteCodes));
    
    // Validation criteria (STRICT):
    // 1. Exact pair count match for current waste configuration
    // 2. All waste codes in results must exist in current session
    // 3. All current waste codes must be represented in results
    // 4. Waste count must match exactly (no tolerance for discrepancy)
    
    const pairCountMatches = actualPairCount === expectedPairCount;
    const allResultCodesExistInSession = Array.from(resultWasteCodes).every(code => currentWasteCodes.has(code));
    const allSessionCodesInResults = Array.from(currentWasteCodes).every(code => resultWasteCodes.has(code));
    const wasteCountMatches = resultWasteCodes.size === currentWasteCodes.size;
    
    console.log('Validation results:', {
      pairCountMatches,
      allResultCodesExistInSession,
      allSessionCodesInResults,
      wasteCountMatches,
      expectedPairCount,
      actualPairCount,
      currentWasteCount,
      resultWasteCount: resultWasteCodes.size
    });
    
    // ALL criteria must pass for results to be considered valid
    const isValid = pairCountMatches && allResultCodesExistInSession && allSessionCodesInResults && wasteCountMatches;
    
    if (!isValid) {
      console.log('❌ Compatibility results validation failed - session state has changed');
      console.log('Reasons:');
      if (!pairCountMatches) console.log('  - Pair count mismatch:', expectedPairCount, 'vs', actualPairCount);
      if (!allResultCodesExistInSession) console.log('  - Results contain codes not in current session');
      if (!allSessionCodesInResults) console.log('  - Current session has codes not in results');
      if (!wasteCountMatches) console.log('  - Waste count mismatch:', currentWasteCodes.size, 'vs', resultWasteCodes.size);
      
      // Log detailed comparison for debugging
      console.log('Current session codes:', Array.from(currentWasteCodes));
      console.log('Result codes:', Array.from(resultWasteCodes));
      const missingInResults = Array.from(currentWasteCodes).filter(code => !resultWasteCodes.has(code));
      const extraInResults = Array.from(resultWasteCodes).filter(code => !currentWasteCodes.has(code));
      if (missingInResults.length > 0) console.log('Missing in results:', missingInResults);
      if (extraInResults.length > 0) console.log('Extra in results:', extraInResults);
    }
    
    return isValid;
  },

  // Auto-start compatibility check without user interaction
  async autoStartCompatibilityCheck() {
    console.log('=== AUTO-STARTING COMPATIBILITY CHECK ===');
    
    // Prevent concurrent checks
    if (this.data.compatibilityCheckInProgress) {
      console.log('Compatibility check already in progress, skipping auto-start');
      return;
    }
    
    // Final validation: ensure we have the expected waste count
    const sessionWastes = this.data.sessionWastes || [];
    console.log('Final waste count validation before starting check:', sessionWastes.length);
    
    if (sessionWastes.length < 2) {
      console.error('Cannot start compatibility check - insufficient wastes:', sessionWastes.length);
      this.setData({
        hasError: true,
        errorMessage: `危废数量不足：当前${sessionWastes.length}种，至少需要2种`
      });
      return;
    }
    
    // Set loading state
    this.setData({
      compatibilityCheckInProgress: true,
      loading: true,
      checkStatus: 'checking',
      hasError: false,
      errorMessage: '',
      pollingCount: 0
    });

    try {
      console.log('Automatically starting compatibility check for session:', this.data.sessionId);
      console.log('Processing', sessionWastes.length, 'wastes for compatibility check');
      
      // Start the compatibility check via API
      await compatibilityAPI.startCompatibilityCheck(this.data.sessionId);
      
      console.log('✅ Auto-compatibility check started successfully');
      
      // Start polling for results
      this.pollCompatibilityStatus();
      
    } catch (error) {
      console.error('Auto-start compatibility check failed:', error);
      this.handleCompatibilityError(error, 'autoStartCompatibilityCheck');
      this.setData({
        loading: false,
        checkStatus: 'failed',
        compatibilityCheckInProgress: false
      });
    }
  },

  // Clear compatibility cache and reset state
  clearCompatibilityCache() {
    console.log('=== CLEARING COMPATIBILITY CACHE ===');
    this.setData({
      // Clear all compatibility results
      compatibilityResults: null,
      compatibilityPassed: null,
      incompatiblePairs: [],
      compatiblePairs: [],
      riskFactors: [],
      
      // Reset status and flags
      checkStatus: 'idle',
      hasError: false,
      errorMessage: '',
      compatibilityCheckInProgress: false,
      canProceed: null,
      
      // Clear nested compatibility object completely
      'compatibility.checked': false,
      'compatibility.compatible': null,
      'compatibility.incompatiblePairs': 0,
      'compatibility.compatiblePairs': 0,
      'compatibility.riskFactors': [],
      'compatibility.totalPairs': 0,
      
      // Reset polling state
      pollingCount: 0,
      loading: false
      
      // Keep sessionWastes and sessionData intact for recheck scenarios
    });
    
    console.log('Compatibility cache cleared completely');
  },

  // Validate session exists and is accessible
  async validateSession(sessionId) {
    try {
      console.log('=== VALIDATING SESSION ===', sessionId);
      const result = await sessionAPI.getSessionStatus(sessionId);
      console.log('Session validation result:', result);
      return true;
    } catch (error) {
      console.error('Session validation failed:', error);
      throw new Error(`Session ${sessionId} is invalid or inaccessible`);
    }
  },



  // Parse compatibility results with robust data extraction
  parseCompatibilityResults(apiResponse) {
    console.log('=== PARSING COMPATIBILITY RESULTS ===');
    console.log('Raw API Response:', JSON.stringify(apiResponse, null, 2));
    
    // Debug API response structure
    this.debugApiResponse(apiResponse);
    
    // Check multiple possible data structures for incompatible pairs
    let incompatiblePairs = [];
    let compatiblePairs = [];
    let riskFactors = [];
    let totalPairs = 0;
    
    // Method 1: Check direct incompatiblePairs array (pre-filtered)
    if (apiResponse.incompatiblePairs && Array.isArray(apiResponse.incompatiblePairs)) {
      console.log('Found incompatiblePairs array:', apiResponse.incompatiblePairs);
      incompatiblePairs = apiResponse.incompatiblePairs;
    }
    
    // Method 2: Check incompatiblePairDetails array (pre-filtered)
    else if (apiResponse.incompatiblePairDetails && Array.isArray(apiResponse.incompatiblePairDetails)) {
      console.log('Found incompatiblePairDetails array:', apiResponse.incompatiblePairDetails);
      incompatiblePairs = apiResponse.incompatiblePairDetails;
    }
    
    // Method 3: Check pairResults for ALL pairs, then filter (MAIN METHOD)
    else if (apiResponse.pairResults && Array.isArray(apiResponse.pairResults)) {
      console.log('Found pairResults array with all pairs:', apiResponse.pairResults);
      totalPairs = apiResponse.pairResults.length;
      
      // Filter for incompatible pairs (compatible: false)
      incompatiblePairs = apiResponse.pairResults.filter(pair => pair.compatible === false);
      compatiblePairs = apiResponse.pairResults.filter(pair => pair.compatible === true);
      
      console.log('Total pairs from backend:', totalPairs);
      console.log('Compatible pairs:', compatiblePairs.length, compatiblePairs);
      console.log('Incompatible pairs:', incompatiblePairs.length, incompatiblePairs);
    }
    
    // Method 4: Check compatibilityResults (fallback)
    else if (apiResponse.compatibilityResults && Array.isArray(apiResponse.compatibilityResults)) {
      console.log('Found compatibilityResults array, filtering for incompatible:', apiResponse.compatibilityResults);
      incompatiblePairs = apiResponse.compatibilityResults.filter(result => result.compatible === false);
      compatiblePairs = apiResponse.compatibilityResults.filter(result => result.compatible === true);
    }
    
    // Method 5: Check details array (fallback)
    else if (apiResponse.details && Array.isArray(apiResponse.details)) {
      console.log('Found details array, filtering for incompatible:', apiResponse.details);
      incompatiblePairs = apiResponse.details.filter(detail => detail.compatible === false);
      compatiblePairs = apiResponse.details.filter(detail => detail.compatible === true);
    }
    
    // Method 6: Check results array (fallback)
    else if (apiResponse.results && Array.isArray(apiResponse.results)) {
      console.log('Found results array, filtering for incompatible:', apiResponse.results);
      incompatiblePairs = apiResponse.results.filter(result => result.compatible === false);
      compatiblePairs = apiResponse.results.filter(result => result.compatible === true);
    }
    
    // Extract risk factors from incompatible pairs
    if (incompatiblePairs.length > 0) {
      riskFactors = this.extractRiskFactors(incompatiblePairs);
    }
    
    console.log('=== FINAL PARSING RESULTS ===');
    console.log('Total pairs:', totalPairs || incompatiblePairs.length + compatiblePairs.length);
    console.log('Compatible pairs:', compatiblePairs.length);
    console.log('Incompatible pairs:', incompatiblePairs.length);
    console.log('Risk factors extracted:', riskFactors.length);
    console.log('Overall compatible flag from backend:', apiResponse.compatible);
    
    // Validate the results
    const shouldBeCompatible = incompatiblePairs.length === 0;
    if (apiResponse.compatible !== shouldBeCompatible) {
      console.warn('⚠️ COMPATIBILITY FLAG MISMATCH:');
      console.warn('Backend says compatible:', apiResponse.compatible);
      console.warn('Should be compatible (no incompatible pairs):', shouldBeCompatible);
    }
    
    return {
      compatible: apiResponse.compatible || false,
      incompatiblePairs: incompatiblePairs,
      compatiblePairs: compatiblePairs,
      totalPairs: totalPairs || incompatiblePairs.length + compatiblePairs.length,
      incompatiblePairsCount: incompatiblePairs.length,
      compatiblePairsCount: compatiblePairs.length,
      riskFactors: riskFactors,
      riskFactorsCount: riskFactors.length
    };
  },

  // Debug API response structure
  debugApiResponse(response) {
    console.log('=== API RESPONSE STRUCTURE DEBUG ===');
    console.log('Response keys:', Object.keys(response));
    console.log('Has incompatiblePairs?', 'incompatiblePairs' in response);
    console.log('Has incompatiblePairDetails?', 'incompatiblePairDetails' in response);
    console.log('Has pairResults?', 'pairResults' in response);
    console.log('Has compatibilityResults?', 'compatibilityResults' in response);
    console.log('Has details?', 'details' in response);
    console.log('Has results?', 'results' in response);
    
    // Log all array fields with detailed content
    Object.keys(response).forEach(key => {
      if (Array.isArray(response[key])) {
        console.log(`${key} (array with ${response[key].length} items):`, response[key]);
        response[key].forEach((item, index) => {
          console.log(`  ${key}[${index}]:`, JSON.stringify(item, null, 2));
        });
      }
    });
    
    // Special check for pairResults structure
    if (response.pairResults && Array.isArray(response.pairResults)) {
      console.log('=== DETAILED pairResults ANALYSIS ===');
      response.pairResults.forEach((pair, index) => {
        console.log(`Pair ${index}:`, pair);
        console.log(`  waste1:`, pair.waste1);
        console.log(`  waste2:`, pair.waste2);
        console.log(`  waste1.name:`, pair.waste1?.name);
        console.log(`  waste2.name:`, pair.waste2?.name);
        console.log(`  compatible:`, pair.compatible);
      });
    }
  },

  // Enhance incompatible pairs with additional information for display
  enhanceIncompatiblePairs(rawPairs) {
    console.log('=== ENHANCING INCOMPATIBLE PAIRS ===');
    console.log('Raw incompatible pairs:', rawPairs);
    console.log('Number of pairs to enhance:', rawPairs.length);
    
    if (!Array.isArray(rawPairs) || rawPairs.length === 0) {
      console.log('No incompatible pairs to enhance');
      return [];
    }

    // Get current session wastes for lookup
    const sessionWastes = this.data.sessionWastes || [];
    console.log('Session wastes available for lookup:', sessionWastes.length);
    
    return rawPairs.map((pair, index) => {
      console.log(`=== ENHANCING INCOMPATIBLE PAIR ${index} ===`);
      console.log('Pair data:', pair);
      
      // Extract waste names using the enhanced function
      const wasteName1 = this.findWasteName(pair, 'waste1', sessionWastes);
      const wasteName2 = this.findWasteName(pair, 'waste2', sessionWastes);
      
      console.log('Extracted waste names:', { wasteName1, wasteName2 });
      
      // Extract waste codes for fallback
      const wasteCode1 = this.findWasteCode(pair, 'waste1');
      const wasteCode2 = this.findWasteCode(pair, 'waste2');
      
      console.log('Extracted waste codes:', { wasteCode1, wasteCode2 });
      
      // Create display names with priority: name → code → '未知危废'
      const displayName1 = wasteName1 || wasteCode1 || '未知危废';
      const displayName2 = wasteName2 || wasteCode2 || '未知危废';
      
      console.log('Final display names:', { displayName1, displayName2 });
      
      // Format conflict reason
      const conflictReason = this.formatConflictReason(pair);
      
      // Parse and extract risk factors  
      const riskFactors = this.parseRiskFactors(pair);
      
      const enhancedPair = {
        ...pair,
        wasteName1,
        wasteName2,
        wasteCode1,
        wasteCode2,
        displayName1,
        displayName2,
        conflictReason,
        riskFactors
      };
      
      console.log('Enhanced incompatible pair result:', enhancedPair);
      return enhancedPair;
    });
  },

  // Enhance compatible pairs with additional information for display
  enhanceCompatiblePairs(rawPairs) {
    console.log('=== ENHANCING COMPATIBLE PAIRS ===');
    console.log('Raw compatible pairs:', rawPairs);
    console.log('Number of pairs to enhance:', rawPairs.length);
    
    if (!Array.isArray(rawPairs) || rawPairs.length === 0) {
      console.log('No compatible pairs to enhance');
      return [];
    }

    // Get current session wastes for lookup
    const sessionWastes = this.data.sessionWastes || [];
    console.log('Session wastes available for lookup:', sessionWastes.length);
    
    return rawPairs.map((pair, index) => {
      console.log(`=== ENHANCING COMPATIBLE PAIR ${index} ===`);
      console.log('Pair data:', pair);
      
      // Extract waste names using the enhanced function
      const wasteName1 = this.findWasteName(pair, 'waste1', sessionWastes);
      const wasteName2 = this.findWasteName(pair, 'waste2', sessionWastes);
      
      console.log('Extracted waste names:', { wasteName1, wasteName2 });
      
      // Extract waste codes for fallback
      const wasteCode1 = this.findWasteCode(pair, 'waste1');
      const wasteCode2 = this.findWasteCode(pair, 'waste2');
      
      console.log('Extracted waste codes:', { wasteCode1, wasteCode2 });
      
      // Create display names with priority: name → code → '未知危废'
      const displayName1 = wasteName1 || wasteCode1 || '未知危废';
      const displayName2 = wasteName2 || wasteCode2 || '未知危废';
      
      console.log('Final display names:', { displayName1, displayName2 });
      
      const enhancedPair = {
        ...pair,
        wasteName1,
        wasteName2,
        wasteCode1,
        wasteCode2,
        displayName1,
        displayName2
      };
      
      console.log('Enhanced compatible pair result:', enhancedPair);
      return enhancedPair;
    });
  },

  // Find waste code from pair data
  findWasteCode(pair, wasteNumber) {
    console.log(`=== FINDING WASTE CODE FOR ${wasteNumber} ===`);
    console.log('Pair data for code lookup:', pair);
    
    // FIRST: Check if wastesCombination JSON contains the codes
    const combinationData = this.parseWastesCombination(pair);
    if (combinationData) {
      if (wasteNumber === 'waste1' && combinationData.waste1) {
        const code = combinationData.waste1.code || combinationData.waste1.wasteCode;
        console.log('Extracted code from wastesCombination waste1:', code);
        if (code) return code;
      } else if (wasteNumber === 'waste2' && combinationData.waste2) {
        const code = combinationData.waste2.code || combinationData.waste2.wasteCode;
        console.log('Extracted code from wastesCombination waste2:', code);
        if (code) return code;
      }
    }
    
    // SECOND: Handle nested waste1/waste2 objects from backend API
    if (wasteNumber === 'waste1' && pair.waste1) {
      const code = pair.waste1.code || pair.waste1.wasteCode;
      console.log('Extracted code from waste1 object:', code);
      if (code) return code;
    } else if (wasteNumber === 'waste2' && pair.waste2) {
      const code = pair.waste2.code || pair.waste2.wasteCode;
      console.log('Extracted code from waste2 object:', code);
      if (code) return code;
    }
    
    // THIRD: Check for flat structure fields
    const codeFields = wasteNumber === 'waste1'
      ? ['wasteCode1', 'waste1Code', 'code1']
      : ['wasteCode2', 'waste2Code', 'code2'];
      
    console.log('Checking flat structure code fields:', codeFields);
    for (const field of codeFields) {
      if (pair[field]) {
        console.log(`Found code in flat field ${field}:`, pair[field]);
        return pair[field];
      }
    }

    // FOURTH: Check for direct waste ID fields and look up in session
    const sessionWastes = this.data.sessionWastes || [];
    if (sessionWastes.length > 0) {
      let wasteId = null;
      
      // Extract waste ID
      const idFields = wasteNumber === 'waste1'
        ? ['wasteId1', 'waste1Id', 'id1']
        : ['wasteId2', 'waste2Id', 'id2'];
        
      console.log('Checking for waste ID fields:', idFields);
      for (const field of idFields) {
        if (pair[field]) {
          wasteId = pair[field];
          console.log(`Found waste ID in field ${field}:`, wasteId);
          break;
        }
      }
      
      // Look up by ID in session data
      if (wasteId) {
        console.log('Looking up waste by ID:', wasteId, 'in session wastes');
        const matchingWaste = sessionWastes.find(w => w.id === wasteId || w.wasteId === wasteId);
        if (matchingWaste) {
          console.log('Found matching waste by ID:', matchingWaste.wasteCode);
          return matchingWaste.wasteCode;
        }
      }
    }

    console.log(`No code found for ${wasteNumber}`);
    return '';
  },

  // Helper function to parse wastesCombination JSON safely
  parseWastesCombination(pair) {
    console.log('=== PARSING WASTES COMBINATION ===');
    console.log('Raw pair data:', pair);
    
    if (!pair.wastesCombination) {
      console.log('No wastesCombination field found');
      return null;
    }
    
    console.log('wastesCombination field type:', typeof pair.wastesCombination);
    console.log('wastesCombination raw value:', pair.wastesCombination);
    
    try {
      const combinationData = typeof pair.wastesCombination === 'string' 
        ? JSON.parse(pair.wastesCombination) 
        : pair.wastesCombination;
      
      console.log('Successfully parsed wastesCombination:', combinationData);
      console.log('Waste1 data:', combinationData.waste1);
      console.log('Waste2 data:', combinationData.waste2);
      
      if (combinationData.waste1) {
        console.log('Waste1 details:', {
          id: combinationData.waste1.id,
          name: combinationData.waste1.name,
          code: combinationData.waste1.code,
          category: combinationData.waste1.category
        });
      }
      
      if (combinationData.waste2) {
        console.log('Waste2 details:', {
          id: combinationData.waste2.id,
          name: combinationData.waste2.name,
          code: combinationData.waste2.code,
          category: combinationData.waste2.category
        });
      }
      
      return combinationData;
      
    } catch (error) {
      console.error('Error parsing wastesCombination JSON:', error);
      console.error('JSON string that failed to parse:', pair.wastesCombination);
      return null;
    }
  },

  // Find waste name from pair data or session data
  findWasteName(pair, wasteNumber, sessionWastes) {
    console.log(`=== FINDING WASTE NAME FOR ${wasteNumber} ===`);
    console.log('Pair data:', pair);
    console.log('Session wastes available:', sessionWastes.length);
    
    // FIRST: Check if wastesCombination JSON contains the names
    const combinationData = this.parseWastesCombination(pair);
    if (combinationData) {
      if (wasteNumber === 'waste1' && combinationData.waste1) {
        const name = combinationData.waste1.name;
        console.log('Extracted name from wastesCombination waste1:', name);
        if (name) return name;
      } else if (wasteNumber === 'waste2' && combinationData.waste2) {
        const name = combinationData.waste2.name;
        console.log('Extracted name from wastesCombination waste2:', name);
        if (name) return name;
      }
    }
    
    // SECOND: Handle nested waste1/waste2 objects from backend API
    if (wasteNumber === 'waste1' && pair.waste1) {
      console.log('Found waste1 object:', pair.waste1);
      const name = pair.waste1.name || pair.waste1.wasteName || pair.waste1.wasteCode;
      console.log('Extracted name from waste1:', name);
      if (name) return name;
    } else if (wasteNumber === 'waste2' && pair.waste2) {
      console.log('Found waste2 object:', pair.waste2);
      const name = pair.waste2.name || pair.waste2.wasteName || pair.waste2.wasteCode;
      console.log('Extracted name from waste2:', name);
      if (name) return name;
    }
    
    // THIRD: Fallback to flat structure (for backward compatibility)
    const nameFields = wasteNumber === 'waste1'
      ? ['wasteName1', 'waste1Name', 'name1']
      : ['wasteName2', 'waste2Name', 'name2'];
    
    console.log('Checking flat structure fields:', nameFields);
    for (const field of nameFields) {
      if (pair[field]) {
        console.log(`Found name in flat field ${field}:`, pair[field]);
        return pair[field];
      }
    }

    // FOURTH: Look up from session wastes using code
    const wasteCode = this.findWasteCode(pair, wasteNumber);
    console.log('Looking up by waste code:', wasteCode);
    if (wasteCode && sessionWastes.length > 0) {
      const matchingWaste = sessionWastes.find(w => w.wasteCode === wasteCode);
      if (matchingWaste) {
        console.log('Found matching waste in session:', matchingWaste.wasteName);
        return matchingWaste.wasteName;
      }
    }

    console.log(`No name found for ${wasteNumber}`);
    return '';
  },

  // Format conflict reason from various sources
  formatConflictReason(pair) {
    let conflictReason = pair.conflictReason || pair.reason || pair.riskDescription || pair.description;
    
    // If we have a conflict reason with codes, translate them
    if (conflictReason) {
      // Handle comma-separated codes like "H,F,GT"
      if (typeof conflictReason === 'string' && /^[A-Z,\s]+$/.test(conflictReason)) {
        const codes = conflictReason.split(',').map(code => code.trim());
        const codeTranslations = {
          'H': '产生热量',
          'F': '火警风险', 
          'GT': '产生有毒气体',
          'P': '强烈聚合作用',
          'O': '氧化性反应',
          'C': '腐蚀性不相容',
          'E': '爆炸风险',
          'G': '产生气体'
        };
        
        const translations = codes.map(code => codeTranslations[code] || code);
        return translations.join('、');
      }
      return conflictReason;
    }
    
    // If we only have risk factors, format them nicely
    const riskFactors = this.parseRiskFactors(pair);
    if (riskFactors.length > 0) {
      const riskMap = {
        'P': 'pH不相容',
        'H': '热反应风险',
        'GT': '产气反应',
        'F': '易燃性冲突',
        'O': '氧化性反应',
        'C': '腐蚀性不相容'
      };
      
      const descriptions = riskFactors.map(risk => riskMap[risk] || risk);
      return descriptions.join('，');
    }
    
    return '危废间存在相容性风险';
  },

  // Parse risk factors from various formats
  parseRiskFactors(pair) {
    let factors = [];
    
    // Check various field names
    const riskSources = [
      pair.riskFactors,
      pair.riskCodes, 
      pair.risks,
      pair.conflictFactors
    ];
    
    for (const source of riskSources) {
      if (source) {
        if (Array.isArray(source)) {
          factors = factors.concat(source);
        } else if (typeof source === 'string') {
          // Split comma-separated values
          factors = factors.concat(source.split(',').map(f => f.trim()).filter(f => f.length > 0));
        }
      }
    }
    
    // Remove duplicates
    return [...new Set(factors)];
  },

  // Extract risk factors from incompatible pairs
  extractRiskFactors(incompatiblePairs) {
    let riskFactors = [];
    
    incompatiblePairs.forEach(pair => {
      if (pair.riskFactors && Array.isArray(pair.riskFactors)) {
        riskFactors = riskFactors.concat(pair.riskFactors);
      }
    });
    
    // Remove duplicates
    riskFactors = [...new Set(riskFactors)];
    
    return riskFactors;
  },

  // Validate compatibility response
  validateCompatibilityResponse(response) {
    const issues = [];
    
    if (response.compatible === false) {
      // If marked as incompatible, we should have details
      const parsedResults = this.parseCompatibilityResults(response);
      if (parsedResults.incompatiblePairsCount === 0) {
        issues.push('Marked as incompatible but no incompatible pairs found');
        console.warn('VALIDATION ISSUE: Marked as incompatible but no incompatible pairs found');
        
        // Try to reconstruct from other data sources
        return this.reconstructCompatibilityData(response);
      }
    }
    
    if (issues.length > 0) {
      console.warn('Compatibility response validation issues:', issues);
    }
    
    return response;
  },

  // Fallback data reconstruction
  reconstructCompatibilityData(originalResponse) {
    console.log('=== ATTEMPTING DATA RECONSTRUCTION ===');
    console.log('Original response:', originalResponse);
    
    // If we have any pairResults or similar, use those
    if (originalResponse.pairResults) {
      const incompatiblePairs = originalResponse.pairResults
        .filter(pair => !pair.compatible)
        .map(pair => ({
          wasteCode1: pair.wasteCode1 || pair.waste1Code,
          wasteCode2: pair.wasteCode2 || pair.waste2Code,
          wasteName1: pair.wasteName1 || pair.waste1Name,
          wasteName2: pair.wasteName2 || pair.waste2Name,
          reason: pair.reason || pair.conflictReason || pair.reasons,
          riskLevel: pair.riskLevel || 'UNKNOWN',
          riskFactors: pair.riskFactors || pair.risks
        }));
      
      console.log('Reconstructed incompatible pairs:', incompatiblePairs);
      
      return {
        ...originalResponse,
        incompatiblePairs: incompatiblePairs,
        incompatiblePairsCount: incompatiblePairs.length
      };
    }
    
    console.log('Unable to reconstruct data, returning original');
    return originalResponse;
  },

  // Update compatibility state atomically with enhanced parsing
  updateCompatibilityState(resultsData) {
    console.log('=== UPDATING COMPATIBILITY STATE ATOMICALLY ===');
    console.log('Results data:', resultsData);
    
    // CRITICAL: Clear ALL existing compatibility data first to prevent stale data
    console.log('Clearing all existing compatibility data before update');
    this.clearCompatibilityCache();
    
    // Parse results using the enhanced parsing logic
    const parsedResults = this.parseCompatibilityResults(resultsData);
    console.log('Parsed results from enhanced parser:', parsedResults);
    
    // Extract and enhance both incompatible and compatible pairs
    const incompatiblePairs = this.enhanceIncompatiblePairs(parsedResults.incompatiblePairs || []);
    const compatiblePairs = this.enhanceCompatiblePairs(parsedResults.compatiblePairs || []);
    const incompatiblePairsCount = incompatiblePairs.length;
    
    // Handle compatibility flag logic
    let compatible = parsedResults.compatible;
    
    // If we have incompatible pairs but compatible is true, prioritize the actual data
    if (incompatiblePairsCount > 0 && compatible) {
      console.warn('=== COMPATIBILITY FLAG MISMATCH ===');
      console.warn('Backend says compatible=true but found incompatible pairs:', incompatiblePairsCount);
      compatible = false; // Override to be safe
    }
    
    // Extract risk factors
    const riskFactors = this.extractRiskFactors(incompatiblePairs);
    
    // Determine final compatibility status
    const compatibilityPassed = compatible && incompatiblePairsCount === 0;
    
    console.log('=== FINAL PARSED DATA ===');
    console.log('Compatible flag (corrected):', compatible);
    console.log('Incompatible pairs count:', incompatiblePairsCount);
    console.log('Compatible pairs count:', parsedResults.compatiblePairsCount);
    console.log('Total pairs:', parsedResults.totalPairs);
    console.log('Final compatibility passed:', compatibilityPassed);
    console.log('Can proceed:', resultsData.canProceed);
    console.log('Enhanced incompatible pairs:', incompatiblePairs);
    console.log('Enhanced compatible pairs:', compatiblePairs);
    console.log('Risk factors:', riskFactors);
    
    // Create enhanced results object with both enhanced pair arrays
    const enhancedResults = {
      ...parsedResults,
      incompatiblePairs: incompatiblePairs,
      compatiblePairs: compatiblePairs,
      enhancedCompatiblePairs: compatiblePairs, // Extra reference for template
      enhancedIncompatiblePairs: incompatiblePairs // Extra reference for template
    };
    
    // Update all related states atomically with the complete parsed results
    // This is a COMPLETE replacement, not a merge
    this.setData({
      compatibilityResults: enhancedResults, // Store the enhanced results
      compatibilityPassed: compatibilityPassed,
      incompatiblePairs: incompatiblePairs,
      compatiblePairs: compatiblePairs, // Add compatible pairs to state
      riskFactors: riskFactors,
      checkStatus: 'completed',
      loading: false,
      hasError: false,
      errorMessage: '',
      canProceed: compatibilityPassed && resultsData.canProceed !== false,
      compatibilityCheckInProgress: false,
      
      // Ensure nested compatibility object is completely updated
      'compatibility.checked': true,
      'compatibility.compatible': compatibilityPassed,
      'compatibility.incompatiblePairs': incompatiblePairsCount,
      'compatibility.compatiblePairs': parsedResults.compatiblePairsCount,
      'compatibility.totalPairs': parsedResults.totalPairs,
      'compatibility.riskFactors': riskFactors,
      
      // Force UI refresh timestamp
      lastStateUpdate: Date.now()
    });
    
    console.log('=== STATE UPDATE COMPLETE ===');
    console.log('Updated state - Compatibility Passed:', compatibilityPassed);
    console.log('Updated state - Incompatible Count:', incompatiblePairsCount);
    console.log('Updated state - Compatible Count:', parsedResults.compatiblePairsCount);
    console.log('Updated state - Total Pairs:', parsedResults.totalPairs);
    console.log('Updated state - Can Proceed:', compatibilityPassed && resultsData.canProceed !== false);
    
    // Update main page button states to reflect new compatibility status
    this.updateMainPageButtonStates(compatibilityPassed);
    
    // Track completion time for recent activity detection
    if (compatibilityPassed) {
      wx.setStorageSync('compatibilityCompletedAt', Date.now());
      console.log('Compatibility check completed successfully, timestamp saved');
    }
    
    // Final validation: ensure results match current session state
    const currentWastes = this.data.sessionWastes || [];
    const expectedPairCount = currentWastes.length >= 2 ? (currentWastes.length * (currentWastes.length - 1)) / 2 : 0;
    const actualTotalPairs = incompatiblePairsCount + parsedResults.compatiblePairsCount;
    
    console.log('=== FINAL RESULT VALIDATION ===');
    console.log('Current waste count:', currentWastes.length);
    console.log('Expected pair count:', expectedPairCount);
    console.log('Actual total pairs in results:', actualTotalPairs);
    
    if (expectedPairCount !== actualTotalPairs && currentWastes.length >= 2) {
      console.warn('⚠️ PAIR COUNT MISMATCH IN FINAL RESULTS');
      console.warn('This indicates the compatibility check may not have processed all current wastes');
      console.warn('Consider forcing a new compatibility check');
    }
  },

  // Debug compatibility state
  debugCompatibilityState(action, data = null) {
    if (!this.data.stateDebugMode) return;
    
    console.log(`=== COMPATIBILITY DEBUG [${action}] ===`);
    console.log('Session ID:', this.data.sessionId);
    console.log('Current Time:', new Date().toISOString());
    console.log('Check Status:', this.data.checkStatus);
    console.log('Compatibility Passed:', this.data.compatibilityPassed);
    console.log('Incompatible Pairs:', this.data.incompatiblePairs.length);
    console.log('Risk Factors:', this.data.riskFactors.length);
    console.log('Last Refresh:', new Date(this.data.lastStateRefresh).toISOString());
    if (data) {
      console.log('Additional Data:', data);
    }
    console.log('========================================');
  },

  // Validate state before actions
  async validateStateBeforeAction(action) {
    console.log(`=== VALIDATING STATE FOR: ${action} ===`);
    
    try {
      // Get fresh session state
      const sessionResult = await sessionAPI.getSessionSummary(this.data.sessionId);
      const sessionState = utils.formatResponse(sessionResult);
      
      console.log('Session Status:', sessionState.status);
      
      // Try to get compatibility state
      let compatibilityState = null;
      try {
        const compatibilityResult = await compatibilityAPI.getCompatibilityResult(this.data.sessionId);
        compatibilityState = utils.formatResponse(compatibilityResult);
        console.log('Compatibility Status:', compatibilityState);
      } catch (error) {
        console.log('No compatibility results available (expected for new sessions)');
      }
      
      // Check for major state inconsistencies and handle them intelligently
      if (compatibilityState && sessionState.compatibility) {
        const sessionCompatible = Boolean(sessionState.compatibility.compatible);
        const resultCompatible = Boolean(compatibilityState.compatible);
        
        if (sessionCompatible !== resultCompatible) {
          console.warn('=== COMPATIBILITY STATE MISMATCH DETECTED ===');
          console.warn('Session says compatible:', sessionCompatible);
          console.warn('Result says compatible:', resultCompatible);
          
          // Prefer session data as it's usually more up-to-date
          if (sessionState.compatibility.incompatiblePairs > 0 || sessionState.compatibility.incompatiblePairDetails) {
            console.warn('Session has incompatible pair data, using session state');
            // Use session data for incompatible pairs
            compatibilityState.compatible = sessionCompatible;
            compatibilityState.incompatiblePairDetails = sessionState.compatibility.incompatiblePairDetails || [];
            compatibilityState.details = sessionState.compatibility.details || [];
          }
        }
      }
      
      return { sessionState, compatibilityState };
      
    } catch (error) {
      console.error('State validation failed:', error);
      
      // Show state synchronization message
      wx.showToast({
        title: '状态同步中，请稍等...',
        icon: 'none'
      });
      
      // Force refresh
      await this.refreshCompatibilityState();
      throw error;
    }
  },

  // Handle compatibility errors with auto-retry
  handleCompatibilityError(error, context) {
    console.error(`Compatibility Error [${context}]:`, error);
    
    this.setData({
      hasError: true,
      errorMessage: '状态异常，正在重新加载...',
      loading: false,
      compatibilityCheckInProgress: false // Release lock on error
    });
    
    // Show user-friendly error message
    wx.showToast({
      title: '检测状态异常，正在重新加载...',
      icon: 'none'
    });
    
    // Auto-retry mechanism after 2 seconds
    setTimeout(() => {
      this.refreshCompatibilityState();
    }, 2000);
  },

  // Clear polling interval and reset counters
  clearPolling() {
    if (this.data.pollingInterval) {
      clearTimeout(this.data.pollingInterval);
      this.setData({
        pollingInterval: null,
        pollingCount: 0
      });
    }
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // Load session summary data with enhanced integration and direct waste loading
  async loadSessionData(forceRefresh = false) {
    try {
      const loadingMessage = forceRefresh ? '强制刷新会话数据...' : '加载会话数据...';
      console.log(`=== ${loadingMessage} ===`);
      
      if (!forceRefresh) {
        utils.showLoading(loadingMessage);
      }
      
      // Load session summary and session wastes separately to ensure we get all data
      console.log('Loading session summary...');
      const sessionResult = await sessionAPI.getSessionSummary(this.data.sessionId);
      const sessionData = utils.formatResponse(sessionResult);
      console.log('Session summary loaded:', sessionData);
      
      // CRITICAL: Use dedicated API to get ALL session wastes
      console.log('Loading ALL session wastes directly...');
      const wastesResult = await wasteAPI.getSessionWastes(this.data.sessionId);
      const sessionWastes = utils.formatResponse(wastesResult);
      console.log('Direct session wastes loaded:', sessionWastes.length, sessionWastes);
      
      // Validate that we got the wastes
      if (!Array.isArray(sessionWastes)) {
        console.error('Session wastes is not an array:', sessionWastes);
        throw new Error('Failed to load session wastes - invalid response format');
      }
      
      // Enhanced logging for debugging
      console.log('=== SESSION DATA COMPARISON ===');
      console.log('Session summary wastes count:', (sessionData.wastes || []).length);
      console.log('Direct API wastes count:', sessionWastes.length);
      
      // Use the direct API result as the source of truth for wastes
      this.setData({
        sessionData: sessionData,
        sessionWastes: sessionWastes, // Use direct API result
        hasError: false,
        errorMessage: ''
      });
      
      console.log('Session data updated - wastes count:', sessionWastes.length);
      
      // Validate consistency between different data sources
      const summaryWasteCount = (sessionData.wastes || []).length;
      const directWasteCount = sessionWastes.length;
      
      if (summaryWasteCount !== directWasteCount) {
        console.warn('⚠️ WASTE COUNT MISMATCH DETECTED:');
        console.warn('Session summary reports:', summaryWasteCount, 'wastes');
        console.warn('Direct API reports:', directWasteCount, 'wastes');
        console.warn('Using direct API count as source of truth');
      }
      
      // Enhanced: Check session compatibility state for potential existing results
      if (sessionData.compatibility && sessionData.compatibility.checked) {
        console.log('⚠️ Session shows compatibility was already checked:', sessionData.compatibility);
        
        // If the session says compatibility was checked but we might not have the details,
        // this will be handled by the tryFetchExistingResults() in intelligentCompatibilityCheck()
      }
      
    } catch (error) {
      console.error('Load session data error:', error);
      this.setData({
        hasError: true,
        errorMessage: '加载会话数据失败',
        sessionWastes: [] // Reset on error
      });
      this.handleCompatibilityError(error, 'loadSessionData');
    } finally {
      if (!forceRefresh) {
        utils.hideLoading();
      }
    }
  },

  // Start compatibility check manually (when auto-check failed or user wants to restart)
  async onStartCheck() {
    console.log('=== MANUAL COMPATIBILITY CHECK START ===');
    
    // Prevent concurrent compatibility checks
    if (this.data.compatibilityCheckInProgress) {
      console.warn('Compatibility check already in progress');
      wx.showToast({
        title: '检查正在进行中，请稍等...',
        icon: 'none'
      });
      return;
    }

    // Validate we have sufficient wastes
    const sessionWastes = this.data.sessionWastes || [];
    if (sessionWastes.length < 2) {
      wx.showToast({
        title: '至少需要2种危废才能进行相容性检查',
        icon: 'none'
      });
      return;
    }

    // Validate state before starting check
    try {
      await this.validateStateBeforeAction('startCompatibilityCheck');
    } catch (error) {
      console.error('State validation failed:', error);
      return;
    }

    // Set concurrency lock and loading state
    this.setData({
      compatibilityCheckInProgress: true,
      loading: true,
      checkStatus: 'checking',
      hasError: false,
      errorMessage: '',
      pollingCount: 0
    });

    // Clear any existing polling first
    this.clearPolling();

    try {
      this.debugCompatibilityState('manualStartCompatibilityCheck');
      
      console.log('Starting manual compatibility check for session:', this.data.sessionId);
      
      // Start compatibility check
      await compatibilityAPI.startCompatibilityCheck(this.data.sessionId);
      
      wx.showToast({
        title: '开始相容性检查',
        icon: 'success'
      });
      
      // Start polling for results
      this.pollCompatibilityStatus();
      
    } catch (error) {
      console.error('Manual start compatibility check error:', error);
      this.handleCompatibilityError(error, 'manualStartCompatibilityCheck');
      this.clearPolling();
      this.setData({
        loading: false,
        checkStatus: 'failed',
        compatibilityCheckInProgress: false // Release lock on error
      });
    }
  },

  // Poll compatibility status with timeout protection
  pollCompatibilityStatus() {
    // Reset polling counter
    this.setData({
      pollingCount: 0
    });
    
    const poll = async () => {
      try {
        // Check timeout protection
        if (this.data.pollingCount >= this.data.maxPollingAttempts) {
          console.log('Polling timeout reached, stopping...');
          this.clearPolling();
          this.setData({
            checkStatus: 'failed',
            loading: false,
            hasError: true,
            errorMessage: '检查超时，请重试',
            compatibilityCheckInProgress: false // Release concurrency lock
          });
          wx.showToast({
            title: '检查超时，请重试',
            icon: 'none'
          });
          return;
        }
        
        // Increment polling counter
        this.setData({
          pollingCount: this.data.pollingCount + 1
        });
        
        const statusResult = await compatibilityAPI.getCompatibilityStatus(this.data.sessionId);
        const statusData = utils.formatResponse(statusResult);
        
        console.log('Compatibility status:', statusData);
        console.log('Polling attempt:', this.data.pollingCount);
        
        // CRITICAL FIX: Check if checking is complete
        if (statusData.isChecking === false) {
          console.log('Checking complete, stopping polling');
          this.clearPolling();
          this.setData({
            compatibilityCheckInProgress: false // Release concurrency lock
          });
          await this.getCompatibilityResults();
          return;
        }
        
        // Check specific completion statuses
        if (statusData.status === 'completed' || 
            statusData.status === 'compatible' || 
            statusData.status === 'incompatible' ||
            statusData.status === 'calculation_success') {
          console.log('Final status reached:', statusData.status);
          this.clearPolling();
          this.setData({
            compatibilityCheckInProgress: false // Release concurrency lock
          });
          await this.getCompatibilityResults();
          return;
        }
        
        // Check for failure states
        if (statusData.status === 'failed' || statusData.status === 'calculation_failed') {
          console.log('Check failed with status:', statusData.status);
          this.clearPolling();
          this.setData({
            checkStatus: 'failed',
            loading: false,
            compatibilityCheckInProgress: false // Release concurrency lock
          });
          wx.showToast({
            title: '相容性检查失败',
            icon: 'none'
          });
          return;
        }
        
        // Continue polling only for active states
        if (statusData.status === 'checking' || 
            statusData.status === 'processing' || 
            statusData.status === 'pending' ||
            statusData.isChecking === true) {
          console.log('Still checking, continue polling...');
          const timeoutId = setTimeout(poll, 2000);
          this.setData({
            pollingInterval: timeoutId
          });
          return;
        }
        
        // For unknown statuses, log and continue polling with caution
        console.warn('Unknown status received:', statusData.status, 'continuing polling...');
        const timeoutId = setTimeout(poll, 2000);
        this.setData({
          pollingInterval: timeoutId
        });
        
      } catch (error) {
        console.error('Poll compatibility status error:', error);
        this.clearPolling();
        this.setData({
          checkStatus: 'failed',
          loading: false
        });
        utils.showError(error, '检查状态失败');
      }
    };
    
    // Start polling
    poll();
  },

  // Get compatibility results with enhanced parsing
  async getCompatibilityResults() {
    try {
      console.log('=== GETTING COMPATIBILITY RESULTS ===');
      const result = await compatibilityAPI.getCompatibilityResult(this.data.sessionId);
      const resultsData = utils.formatResponse(result);
      
      console.log('Raw compatibility result from API:', resultsData);
      
      // Add enhanced debugging for the API response structure
      this.debugApiResponse(resultsData);
      
      // Use the enhanced parsing logic
      this.updateCompatibilityState(resultsData);
      
      // Show result message based on parsed data
      const incompatiblePairsCount = this.data.incompatiblePairs.length;
      if (this.data.compatibilityPassed) {
        wx.showToast({
          title: '相容性检查通过',
          icon: 'success'
        });
      } else {
        wx.showToast({
          title: `发现${incompatiblePairsCount}个不相容组合`,
          icon: 'none'
        });
      }
      
      // Update main page button states by triggering a global state update
      this.updateMainPageButtonStates(this.data.compatibilityPassed);
      
    } catch (error) {
      console.error('Get compatibility results error:', error);
      this.setData({
        checkStatus: 'failed',
        loading: false,
        hasError: true,
        errorMessage: '获取检查结果失败',
        compatibilityCheckInProgress: false // Release lock on error
      });
      this.handleCompatibilityError(error, 'getCompatibilityResults');
    }
  },

  // Update main page button states
  updateMainPageButtonStates(compatibilityPassed) {
    try {
      // Get the previous page data and update button states
      const pages = getCurrentPages();
      if (pages.length > 1) {
        const prevPage = pages[pages.length - 2];
        if (prevPage && prevPage.setData) {
          prevPage.setData({
            compatibilityPassed: compatibilityPassed,
            'buttonStates.blending': compatibilityPassed,
            'buttonStates.view': false // Reset view button until calculation is done
          });
        }
      }
    } catch (error) {
      console.error('Update main page button states error:', error);
    }
  },

  // View detailed compatibility results
  async onViewDetails() {
    try {
      utils.showLoading('加载详细信息...');
      
      const result = await compatibilityAPI.getCompatibilityDetails(this.data.sessionId);
      const detailData = utils.formatResponse(result);
      
      // Format details for display
      const detailText = this.formatCompatibilityDetails(detailData);
      
      wx.showModal({
        title: '相容性详细信息',
        content: detailText,
        showCancel: false,
        confirmText: '关闭'
      });
      
    } catch (error) {
      console.error('Get compatibility details error:', error);
      utils.showError(error, '获取详细信息失败');
    } finally {
      utils.hideLoading();
    }
  },

  // Format compatibility details for display
  formatCompatibilityDetails(details) {
    let text = '';
    
    if (details.summary) {
      text += `检查摘要：${details.summary}\n\n`;
    }
    
    if (details.compatiblePairs && details.compatiblePairs.length > 0) {
      text += `相容组合 (${details.compatiblePairs.length})：\n`;
      details.compatiblePairs.forEach(pair => {
        text += `• ${pair.waste1Name} + ${pair.waste2Name}\n`;
      });
      text += '\n';
    }
    
    if (details.incompatiblePairs && details.incompatiblePairs.length > 0) {
      text += `不相容组合 (${details.incompatiblePairs.length})：\n`;
      details.incompatiblePairs.forEach(pair => {
        text += `• ${pair.waste1Name} + ${pair.waste2Name}\n`;
        text += `  风险：${pair.riskDescription}\n`;
      });
      text += '\n';
    }
    
    if (details.recommendations) {
      text += `建议：${details.recommendations}`;
    }
    
    return text || '暂无详细信息';
  },

  // Restart compatibility check with intelligent re-initialization
  onRestartCheck() {
    // Prevent restart if check is in progress
    if (this.data.compatibilityCheckInProgress) {
      wx.showToast({
        title: '检查正在进行中，请稍等...',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '确认重新检查',
      content: '确定要重新进行相容性检查吗？这将清除当前结果并重新开始。',
      success: async (res) => {
        if (res.confirm) {
          this.debugCompatibilityState('restartCheck');
          
          // Clear any existing polling first
          this.clearPolling();
          
          // Clear compatibility cache but PRESERVE session data
          this.setData({
            compatibilityResults: null,
            compatibilityPassed: null,
            incompatiblePairs: [],
            riskFactors: [],
            checkStatus: 'idle',
            hasError: false,
            errorMessage: '',
            pollingCount: 0,
            compatibilityCheckInProgress: false,
            lastStateRefresh: 0,
            'compatibility.checked': false,
            'compatibility.compatible': null,
            'compatibility.incompatiblePairs': 0,
            'compatibility.riskFactors': []
            // DO NOT clear sessionWastes and sessionData - preserve them!
          });
          
          // Update main page to reflect the reset
          this.updateMainPageButtonStates(false);
          
          // FIX: Reload session data before starting recheck to ensure we have current waste list
          try {
            console.log('=== RESTARTING WITH FRESH SESSION DATA ===');
            await this.loadSessionData(true); // Force refresh session data
            
            // Validate we still have sufficient wastes after refresh
            if (!this.data.sessionWastes || this.data.sessionWastes.length < 2) {
              throw new Error('会话中的危废数量不足，至少需要2种危废才能进行相容性检查');
            }
          
          // Use the intelligent compatibility check system for restart
            await this.intelligentCompatibilityCheck();
            
          } catch (error) {
            console.error('Restart with fresh data failed:', error);
            this.setData({
              hasError: true,
              errorMessage: error.message || '重新检查失败，请刷新页面重试'
            });
          }
        }
      }
    });
  },

  // Return to main page
  onReturn() {
    wx.navigateBack();
  },

  // Continue to next step (blending calculation) with state validation
  async onContinue() {
    try {
      // Validate state before continuing
      await this.validateStateBeforeAction('continue');
    } catch (error) {
      console.error('State validation failed before continue:', error);
      return;
    }

    // Enhanced validation checks
    if (!this.data.compatibilityPassed) {
      wx.showModal({
        title: '相容性检查不通过',
        content: '当前配伍方案存在相容性问题，无法继续进行配伍计算。请调整危废配比或移除冲突项后重新检查。',
        showCancel: false,
        confirmText: '我知道了'
      });
      return;
    }

    // Check for incompatible pairs
    if (this.data.incompatiblePairs && this.data.incompatiblePairs.length > 0) {
      wx.showModal({
        title: '存在不相容组合',
        content: `发现${this.data.incompatiblePairs.length}个不相容组合，存在安全风险。请先解决相容性问题。`,
        showCancel: false,
        confirmText: '我知道了'
      });
      return;
    }

    // Check canProceed flag if available
    if (this.data.canProceed === false) {
      wx.showModal({
        title: '无法继续',
        content: '系统检测到当前配伍方案不允许继续，请重新检查或调整配伍方案。',
        showCancel: false,
        confirmText: '我知道了'
      });
      return;
    }
    
    this.debugCompatibilityState('continue');
    
    // Update main page state before returning
    this.updateMainPageButtonStates(true);
    
    wx.showToast({
      title: '相容性检查通过，开始配伍计算',
      icon: 'success'
    });
    
    // Return to main page to proceed with blending
    wx.navigateBack();
  },

  // Get risk level color
  getRiskLevelColor(riskLevel) {
    switch (riskLevel) {
      case 'HIGH':
        return '#dc3545';
      case 'MEDIUM':
        return '#ffc107';
      case 'LOW':
        return '#28a745';
      default:
        return '#6c757d';
    }
  },

  // Get risk level text
  getRiskLevelText(riskLevel) {
    switch (riskLevel) {
      case 'HIGH':
        return '高风险';
      case 'MEDIUM':
        return '中等风险';
      case 'LOW':
        return '低风险';
      default:
        return '未知风险';
    }
  },

  // Helper method to get polling progress percentage
  getPollingProgress() {
    return Math.min((this.data.pollingCount / this.data.maxPollingAttempts) * 100, 100);
  },

  // Helper method to check if polling should show timeout warning
  shouldShowTimeoutWarning() {
    return this.data.pollingCount > (this.data.maxPollingAttempts * 0.7); // 70% threshold
  },

  // State synchronization checkpoint
  async syncStateCheckpoint() {
    try {
      console.log('=== STATE SYNCHRONIZATION CHECKPOINT ===');
      
      const backendResult = await sessionAPI.getSessionSummary(this.data.sessionId);
      const backendState = utils.formatResponse(backendResult);
      const frontendState = this.getCurrentFrontendState();
      
      console.log('Backend state:', backendState.compatibility);
      console.log('Frontend state:', frontendState);
      
      if (!this.statesMatch(backendState, frontendState)) {
        console.warn('States out of sync, forcing refresh');
        await this.refreshCompatibilityState();
        return false;
      }
      
      console.log('States are synchronized');
      return true;
      
    } catch (error) {
      console.error('State synchronization checkpoint failed:', error);
      return false;
    }
  },

  // Get current frontend state summary
  getCurrentFrontendState() {
    return {
      compatible: this.data.compatibilityPassed,
      checked: this.data.compatibility.checked,
      incompatiblePairs: this.data.incompatiblePairs.length,
      riskFactors: this.data.riskFactors.length,
      checkStatus: this.data.checkStatus
    };
  },

  // Check if states match
  statesMatch(backendState, frontendState) {
    if (!backendState.compatibility) {
      // No backend compatibility data, frontend should be in idle state
      return frontendState.checkStatus === 'idle' && !frontendState.checked;
    }
    
    const backendCompatible = Boolean(backendState.compatibility.compatible);
    const frontendCompatible = Boolean(frontendState.compatible);
    
    return backendCompatible === frontendCompatible &&
           backendState.compatibility.checked === frontendState.checked;
  },

  // Test function to validate wastesCombination parsing (can be removed in production)
  testWastesCombinationParsing() {
    console.log('=== TESTING WASTES COMBINATION PARSING ===');
    
    // Test with the expected JSON structure from the user's console logs
    const testPair = {
      wastesCombination: '{"waste2":{"code":"900-252-12","category":"AMIDE_ORGANIC","id":37,"name":"油漆渣"},"waste1":{"code":"900-999-49","category":"FLAMMABLE_MISC","id":41,"name":"废香精"}}'
    };
    
    console.log('Testing with sample data:', testPair);
    
    const combinationData = this.parseWastesCombination(testPair);
    if (combinationData) {
      const waste1Name = combinationData.waste1?.name;
      const waste2Name = combinationData.waste2?.name;
      const waste1Code = combinationData.waste1?.code;
      const waste2Code = combinationData.waste2?.code;
      
      console.log('Test Results:');
      console.log('  Waste1 Name:', waste1Name, '(expected: 废香精)');
      console.log('  Waste2 Name:', waste2Name, '(expected: 油漆渣)');
      console.log('  Waste1 Code:', waste1Code, '(expected: 900-999-49)');
      console.log('  Waste2 Code:', waste2Code, '(expected: 900-252-12)');
      
      // Test the name finding functions
      const foundName1 = this.findWasteName(testPair, 'waste1', []);
      const foundName2 = this.findWasteName(testPair, 'waste2', []);
      
      console.log('  findWasteName test results:');
      console.log('    Found waste1 name:', foundName1, '(should be: 废香精)');
      console.log('    Found waste2 name:', foundName2, '(should be: 油漆渣)');
      
      return foundName1 === '废香精' && foundName2 === '油漆渣';
    } else {
      console.error('Failed to parse test data');
      return false;
    }
  },
})