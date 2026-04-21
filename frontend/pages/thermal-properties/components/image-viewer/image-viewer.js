Component({
  properties: {
    show: {
      type: Boolean,
      value: false
    },
    currentImage: {
      type: Object,
      value: null
    }
  },
  
  methods: {
    closeViewer() {
      this.triggerEvent('close');
    }
  }
}); 