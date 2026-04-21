Component({
  properties: {
    wasteData: {
      type: Object,
      value: {}
    },
    showSelectButton: {
      type: Boolean,
      value: false
    },
    selected: {
      type: Boolean,
      value: false
    }
  },

  data: {
    
  },

  methods: {
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

    getTagClass(property) {
      const tagMap = {
        '易燃性': 'tag-flammable',
        '毒性': 'tag-toxic',
        '剧毒性': 'tag-toxic',
        '腐蚀性': 'tag-corrosive',
        '强腐蚀性': 'tag-corrosive',
        '反应性': 'tag-reactive',
        '含重金属': 'tag-heavy-metal',
        '可燃性': 'tag-flammable',
        '易挥发': 'tag-reactive',
        '含PCBs': 'tag-toxic',
        '高盐分': 'tag-default',
        '含VOCs': 'tag-reactive',
        '溴化物': 'tag-toxic',
        '混合性': 'tag-default',
        '未知性': 'tag-default',
        '含油': 'tag-default',
        '生物降解难': 'tag-reactive',
        '含苯并芘': 'tag-toxic',
        '致癌性': 'tag-toxic',
        '高毒性': 'tag-toxic',
        '持久性': 'tag-reactive',
        '含添加剂': 'tag-default',
        '含氰化物': 'tag-toxic',
        '生物毒性': 'tag-toxic'
      };
      
      return tagMap[property] || 'tag-default';
    },

    onCardTap() {
      this.triggerEvent('cardtap', {
        wasteData: this.properties.wasteData
      });
    },

    onSelectTap(e) {
      e.stopPropagation();
      const selected = e.currentTarget.dataset.selected === 'true';
      
      this.triggerEvent('select', {
        wasteData: this.properties.wasteData,
        selected: !selected
      });
    }
  }
}); 