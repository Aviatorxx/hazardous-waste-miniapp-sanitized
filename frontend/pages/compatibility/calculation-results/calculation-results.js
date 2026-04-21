// pages/compatibility/calculation-results/calculation-results.js
const { calculationAPI, sessionAPI, utils } = require('../../../utils/matching-api.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    sessionId: null,
    calculationResults: null,
    sessionData: null,
    loading: false,
    calculationSuccess: false,
    constraintViolations: [],
    indicators: {},
    wasteDetails: [],
    calculationStatus: null,
    showFailureReasons: false,
    failureReasons: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    if (options.sessionId) {
      this.setData({
        sessionId: options.sessionId
      });
      this.loadCalculationResults();
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

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadCalculationResults().then(() => {
      wx.stopPullDownRefresh();
    });
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

  // Format date time to remove the 'T' and make it more readable
  formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    
    try {
      // Replace 'T' with space and remove seconds for cleaner display
      return dateTimeString.replace('T', ' ').substring(0, 16);
    } catch (error) {
      console.warn('Date formatting error:', error);
      return dateTimeString;
    }
  },

  // Clean message content to remove duplicates and fix spacing
  cleanMessage(message) {
    if (!message) return '';
    
    try {
      let cleanedMessage = message;
      
      // Remove English prefixes that duplicate Chinese content
      cleanedMessage = cleanedMessage.replace(/^Calculation failed:\s*/i, '');
      cleanedMessage = cleanedMessage.replace(/^计算失败:\s*/i, '');
      
      // Remove duplicate "配伍失败:" that might appear after "Calculation failed:"
      cleanedMessage = cleanedMessage.replace(/配伍失败:\s*配伍失败:/g, '配伍失败:');
      
      // Fix spacing around Chinese punctuation
      cleanedMessage = cleanedMessage.replace(/:\s+/g, ': ');
      cleanedMessage = cleanedMessage.replace(/，\s+/g, '，');
      cleanedMessage = cleanedMessage.replace(/；\s+/g, '；');
      cleanedMessage = cleanedMessage.replace(/。\s+/g, '。');
      cleanedMessage = cleanedMessage.replace(/：\s+/g, '：');
      
      // Remove extra spaces between Chinese characters
      cleanedMessage = cleanedMessage.replace(/([^\x00-\xff])\s+([^\x00-\xff])/g, '$1$2');
      
      // Fix spacing around numbers and units
      cleanedMessage = cleanedMessage.replace(/(\d+)\s+(cal\/g|%|kJ\/kg|mg\/kg)/g, '$1$2');
      cleanedMessage = cleanedMessage.replace(/(\d+\.\d+)\s+(cal\/g|%|kJ\/kg|mg\/kg)/g, '$1$2');
      
      // Clean up multiple spaces
      cleanedMessage = cleanedMessage.replace(/\s+/g, ' ').trim();
      
      // Ensure proper Chinese formatting
      cleanedMessage = cleanedMessage.replace(/的值\s+(\d+)/g, '的值$1');
      cleanedMessage = cleanedMessage.replace(/控制\s+的值/g, '控制的值');
      
      return cleanedMessage;
    } catch (error) {
      console.warn('Message cleaning error:', error);
      return message;
    }
  },

  // Load calculation results
  async loadCalculationResults() {
    this.setData({ loading: true });

    try {
      utils.showLoading('加载计算结果...');
      
      // Get calculation results
      const result = await calculationAPI.getCalculationResults(this.data.sessionId);
      const resultsData = utils.formatResponse(result);
      
      // Get session summary for additional info
      const sessionResult = await sessionAPI.getSessionSummary(this.data.sessionId);
      const sessionData = utils.formatResponse(sessionResult);

      // Extract calculation status and success/failure information
      const calculationSuccess = resultsData.success || false;
      const calculationStatus = resultsData.status || sessionData.status;
      
      // Format the calculation time and clean the message
      const formattedResults = {
        ...resultsData,
        calculationTime: this.formatDateTime(resultsData.calculationTime),
        message: this.cleanMessage(resultsData.message)
      };
      
      // Convert actual heat value for display
      const actualHeatValueConverted = this.convertHeatValueForDisplay(resultsData.actualHeatValue);
      


      // 🔍 COMPLETE 11-INDICATOR VERIFICATION
      console.log('=== COMPLETE 11-INDICATOR VERIFICATION ===');
      console.log('resultsData.indicators:', resultsData?.indicators);
      console.log('1. Heat Value:', resultsData?.indicators?.heatValue);
      console.log('2. Water Content:', resultsData?.indicators?.waterContent);
      console.log('3. Nitrogen Content:', resultsData?.indicators?.nitrogenContent);
      console.log('4. Sulfur Content:', resultsData?.indicators?.sulfurContent);
      console.log('5. Chlorine Content:', resultsData?.indicators?.chlorineContent);
      console.log('6. Fluorine Content:', resultsData?.indicators?.fluorineContent);
      console.log('7. Mercury Content:', resultsData?.indicators?.mercuryContent);
      console.log('8. Cadmium Content:', resultsData?.indicators?.cadmiumContent);
      console.log('9. Arsenic+Nickel Content:', resultsData?.indicators?.arsenicNickelContent);
      console.log('10. Lead Content:', resultsData?.indicators?.leadContent);
      console.log('11. Total Heavy Metals:', resultsData?.indicators?.totalHeavyMetals);

      // 🚀 COMPLETE 11-INDICATOR API DATA PROCESSING - Using exact backend field names
      const constraintParametersArray = [
        {
          parameter: '热值',
          limit: '12500～16800 kJ/kg',
          actualValue: `${(resultsData?.indicators?.heatValue * 4.184 || 0).toFixed(2)} kJ/kg`,
          status: '✅'
        },
        {
          parameter: '水分',
          limit: '≤45%',
          actualValue: `${(resultsData?.indicators?.waterContent || 0).toFixed(2)}%`,
          status: '✅'
        },
        {
          parameter: 'N',
          limit: '≤2%',
          actualValue: `${(resultsData?.indicators?.nitrogenContent || 0).toFixed(2)}%`,
          status: '✅'
        },
        {
          parameter: 'S',
          limit: '≤3%',
          actualValue: `${(resultsData?.indicators?.sulfurContent || 0).toFixed(2)}%`,
          status: '✅'
        },
        {
          parameter: 'Cl',
          limit: '≤1.5%',
          actualValue: `${(resultsData?.indicators?.chlorineContent || 0).toFixed(2)}%`,
          status: '✅'
        },
        {
          parameter: 'F',
          limit: '≤1%',
          actualValue: `${(resultsData?.indicators?.fluorineContent || 0).toFixed(2)}%`,
          status: '✅'
        },
        {
          parameter: 'Hg',
          limit: '≤4 mg/kg',
          actualValue: `${(resultsData?.indicators?.mercuryContent || 0).toFixed(2)} mg/kg`,
          status: '✅'
        },
        {
          parameter: 'Cd',
          limit: '≤1 mg/kg',
          actualValue: `${(resultsData?.indicators?.cadmiumContent || 0).toFixed(2)} mg/kg`,
          status: '✅'
        },
        {
          parameter: 'As+Ni',
          limit: '≤95 mg/kg',
          actualValue: `${(resultsData?.indicators?.arsenicNickelContent || 0).toFixed(2)} mg/kg`,
          status: '✅'
        },
        {
          parameter: 'Pb',
          limit: '≤70 mg/kg',
          actualValue: `${(resultsData?.indicators?.leadContent || 0).toFixed(2)} mg/kg`,
          status: '✅'
        },
        {
          parameter: 'Cr+Sn+Sb+Cu+Mn',
          limit: '≤800 mg/kg',
          actualValue: `${(resultsData?.indicators?.totalHeavyMetals || 0).toFixed(2)} mg/kg`,
          status: '✅'
        }
      ];

      console.log('=== PROCESSED CONSTRAINT PARAMETERS ===');
      console.log('constraintParametersArray:', constraintParametersArray);
      console.log('constraintParametersArray length:', constraintParametersArray.length);
      
      // Log each parameter for verification
      constraintParametersArray.forEach((param, index) => {
        console.log(`Parameter ${index + 1}: ${param.parameter} = ${param.actualValue} (${param.status})`);
      });

      // Process indicators and convert heat values if present
      const processedIndicators = {};
      if (resultsData.indicators) {
        Object.keys(resultsData.indicators).forEach(key => {
          if (key === 'heatValue') {
            processedIndicators[key] = this.convertHeatValueForDisplay(resultsData.indicators[key]);
          } else {
            processedIndicators[key] = resultsData.indicators[key];
          }
        });
      }
      
      // 🔍 FINAL DATA VERIFICATION BEFORE setData:
      console.log('=== FINAL DATA VERIFICATION ===');
      console.log('constraintParametersArray type:', typeof constraintParametersArray);
      console.log('constraintParametersArray length:', constraintParametersArray?.length);
      console.log('Ready to set page data with', constraintParametersArray.length, 'parameters');
      
      this.setData({
        calculationResults: formattedResults,
        sessionData: sessionData,
        calculationSuccess: calculationSuccess,
        calculationStatus: calculationStatus,
        constraintViolations: (resultsData.constraintViolations || []).map(violation => this.cleanMessage(violation)),
        indicators: resultsData.indicators || {},
        processedIndicators: processedIndicators,
        constraintParameters: constraintParametersArray, // Keep for compatibility
        constraintParametersArray: constraintParametersArray, // Use real API data
        wasteDetails: resultsData.wasteDetails || [],
        failureReasons: resultsData.failureReasons || [],
        showFailureReasons: !calculationSuccess,
        actualHeatValueConverted: actualHeatValueConverted
      });

      console.log('=== AFTER setData ===');
      console.log('Page data constraintParametersArray:', this.data.constraintParametersArray);
      console.log('Page data type:', typeof this.data.constraintParametersArray);
      console.log('Page data length:', this.data.constraintParametersArray?.length);
      
    } catch (error) {
      console.error('Load calculation results error:', error);
      utils.showError(error, '加载计算结果失败');
    } finally {
      utils.hideLoading();
      this.setData({ loading: false });
    }
  },

  // Show detailed constraint info
  onShowConstraintDetail(e) {
    const constraint = e.currentTarget.dataset.constraint;
    
    let content = `约束名称：${constraint.parameter}\n`;
    content += `约束类型：${constraint.limit}\n`;
    content += `是否通过：${constraint.status === '✅' ? '是' : '否'}\n`;
    content += `实际值：${constraint.actualValue}\n`;
    
    wx.showModal({
      title: '约束详情',
      content: content,
      showCancel: false,
      confirmText: '关闭'
    });
  },

  // Show waste detail
  onShowWasteDetail(e) {
    const waste = e.currentTarget.dataset.waste;
    
    let content = `危废代码：${waste.wasteCode}\n`;
    content += `危废名称：${waste.wasteName}\n`;
    content += `用量：${waste.quantity}kg\n`;
    content += `占比：${waste.percentage}%\n`;
    content += `热值：${this.convertHeatValueForDisplay(waste.heatValue)}kJ/kg\n`;
    content += `含水率：${waste.waterContent}%`;
    
    wx.showModal({
      title: '危废详情',
      content: content,
      showCancel: false,
      confirmText: '关闭'
    });
  },

  // Show indicator detail
  onShowIndicatorDetail(e) {
    const { name, value } = e.currentTarget.dataset;
    
    const indicatorNames = {
      'heatValue': '热值',
      'waterContent': '含水率',
      'ashContent': '灰分',
      'chlorineContent': '氯含量',
      'fluorineContent': '氟含量',
      'sulfurContent': '硫含量',
      'nitrogenContent': '氮含量',
      'totalHeavyMetals': '重金属总量'
    };
    
    const displayName = indicatorNames[name] || name;
    let content = `指标名称：${displayName}\n`;
    
    // Convert heat value from cal/g to kJ/kg for display
    if (name === 'heatValue') {
      content += `数值：${this.formatNumber(this.convertHeatValueForDisplay(value))}`;
    } else {
      content += `数值：${this.formatNumber(value)}`;
    }
    
    // Add unit based on indicator type
    if (name === 'heatValue') {
      content += ' kJ/kg';
    } else if (['waterContent', 'ashContent', 'sulfurContent', 'nitrogenContent'].includes(name)) {
      content += ' %';
    } else if (['chlorineContent', 'fluorineContent', 'totalHeavyMetals'].includes(name)) {
      content += ' mg/kg';
    }
    
    wx.showModal({
      title: '指标详情',
      content: content,
      showCancel: false,
      confirmText: '关闭'
    });
  },

  // Refresh calculation results
  onRefresh() {
    this.loadCalculationResults();
  },

  // Return to main page
  onReturn() {
    wx.navigateBack();
  },

  // Get constraint status color
  getConstraintStatusColor(passed) {
    return passed ? '#28a745' : '#dc3545';
  },

  // Get constraint status text
  getConstraintStatusText(passed) {
    return passed ? '通过' : '未通过';
  },

  // Format number with specified precision
  formatNumber(num, precision = 2) {
    if (num === null || num === undefined || isNaN(num)) return '−';
    return Number(num).toFixed(precision);
  },

  // Convert heat value from cal/g to kJ/kg for display
  convertHeatValueForDisplay(calPerG) {
    if (typeof calPerG !== 'number' || calPerG === null || calPerG === undefined) {
      return calPerG;
    }
    // Convert cal/g to kJ/kg using conversion factor 4.184
    return parseFloat((calPerG * 4.184).toFixed(2));
  },

  // Get indicator color based on value and type
  getIndicatorColor(name, value) {
    // This can be customized based on acceptable ranges for different indicators
    return '#2c3e50'; // Default color
  }
});