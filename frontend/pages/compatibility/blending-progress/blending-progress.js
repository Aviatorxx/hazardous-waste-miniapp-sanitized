const { calculationAPI, sessionAPI, utils } = require('../../../utils/matching-api.js');

Page({
  data: {
    sessionId: null,
    calculating: true,
    progress: 0,
    statusText: '正在初始化配伍计算...',
    calculationSuccess: null,
    calculationResult: null,
    pollCount: 0,
    maxPollAttempts: 30
  },

  onLoad(options) {
    if (options.sessionId) {
      this.setData({
        sessionId: options.sessionId
      });
      this.startBlendingCalculation();
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  // Start blending calculation
  async startBlendingCalculation() {
    try {
      this.setData({
        statusText: '启动配伍计算...',
        progress: 10
      });

      // Start calculation
      await calculationAPI.startCalculation(this.data.sessionId);
      
      this.setData({
        statusText: '配伍计算进行中...',
        progress: 30
      });

      // Start polling for results
      this.pollCalculationStatus();

    } catch (error) {
      console.error('Failed to start calculation:', error);
      this.setData({
        calculating: false,
        calculationSuccess: false,
        statusText: '启动计算失败'
      });
      utils.showError(error, '计算启动失败');
    }
  },

  // Poll calculation status
  pollCalculationStatus() {
    const pollInterval = setInterval(async () => {
      try {
        this.setData({
          pollCount: this.data.pollCount + 1
        });

        // Update progress based on poll count
        const progress = Math.min(30 + (this.data.pollCount * 2), 90);
        this.setData({
          progress: progress,
          statusText: `配伍计算进行中... (${this.data.pollCount}/${this.data.maxPollAttempts})`
        });

        // Check for timeout
        if (this.data.pollCount >= this.data.maxPollAttempts) {
          clearInterval(pollInterval);
          this.handleCalculationTimeout();
          return;
        }

        const result = await calculationAPI.getCalculationStatus(this.data.sessionId);
        const statusData = utils.formatResponse(result);
        const status = statusData.status;

        console.log(`Calculation status (attempt ${this.data.pollCount}):`, status);

        if (status === 'calculation_success') {
          clearInterval(pollInterval);
          this.handleCalculationSuccess();
        } else if (status === 'calculation_failed' || status === 'failed' || status === 'error') {
          clearInterval(pollInterval);
          this.handleCalculationFailure();
        }
        // Continue polling for other statuses (calculating, pending)

      } catch (error) {
        console.error('Polling error:', error);
        clearInterval(pollInterval);
        this.handleCalculationError(error);
      }
    }, 2000); // Poll every 2 seconds
  },

  // Handle calculation success
  async handleCalculationSuccess() {
    try {
      // Update progress to 100% briefly to show completion
      this.setData({
        progress: 100,
        statusText: '配伍计算完成'
      });

      // Show brief success notification
      wx.showToast({
        title: '配伍计算成功',
        icon: 'success',
        duration: 1000
      });

      // Navigate directly to results page without intermediate screen
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/compatibility/calculation-results/calculation-results?sessionId=${this.data.sessionId}`
        });
      }, 500); // Reduced delay to 500ms for faster navigation

    } catch (error) {
      console.error('Failed to get calculation results:', error);
      this.handleCalculationError(error);
    }
  },

  // Handle calculation failure
  handleCalculationFailure() {
    this.setData({
      calculating: false,
      calculationSuccess: false,
      progress: 100,
      statusText: '配伍计算失败'
    });

    // Show failure icon prominently
    wx.showToast({
      title: '配伍失败',
      icon: 'none',
      duration: 2000
    });
  },

  // Handle calculation timeout
  handleCalculationTimeout() {
    this.setData({
      calculating: false,
      calculationSuccess: false,
      progress: 100,
      statusText: '计算超时'
    });

    wx.showToast({
      title: '计算超时，请重试',
      icon: 'none',
      duration: 2000
    });
  },

  // Handle calculation error
  handleCalculationError(error) {
    this.setData({
      calculating: false,
      calculationSuccess: false,
      progress: 100,
      statusText: '计算出错'
    });

    utils.showError(error, '计算失败');
  },

  // Navigate to view results
  onViewResults() {
    wx.redirectTo({
      url: `/pages/compatibility/calculation-results/calculation-results?sessionId=${this.data.sessionId}`
    });
  },

  // Go back to compatibility page
  onGoBack() {
    wx.navigateBack();
  },

  // Retry calculation
  onRetry() {
    this.setData({
      calculating: true,
      progress: 0,
      statusText: '重新开始配伍计算...',
      calculationSuccess: null,
      pollCount: 0
    });
    this.startBlendingCalculation();
  }
}); 