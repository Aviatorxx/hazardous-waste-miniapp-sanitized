Component({
  properties: {
    placeholder: {
      type: String,
      value: '请输入搜索内容'
    },
    value: {
      type: String,
      value: ''
    }
  },

  data: {
    
  },

  methods: {
    onInput(e) {
      const value = e.detail.value;
      this.setData({
        value: value
      });
      
      // Trigger parent component event
      this.triggerEvent('input', {
        value: value
      });
    },

    onConfirm(e) {
      const value = e.detail.value;
      this.triggerEvent('search', {
        value: value
      });
    },

    onClear() {
      this.setData({
        value: ''
      });
      
      this.triggerEvent('input', {
        value: ''
      });
      
      this.triggerEvent('clear');
    }
  }
}); 