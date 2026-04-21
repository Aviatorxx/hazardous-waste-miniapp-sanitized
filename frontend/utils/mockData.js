// Mock data for hazardous waste - Updated structure as requested
const wasteData = [
  {
    id: 1,
    sequenceNo: 1,
    wasteCode: "271-001-02",
    sourceUnit: "华海立诚",
    wasteName: "高沸物",
    appearance: "浅黄混褐色液体",
    harmfulComponents: "乙醇",
    storageLocation: "A区",
    remainingStorage: 999.0,
    ph: 5.5,
    heatValue: 1408,
    waterContent: null,
    flashPoint: 65,
    properties: {
      oxidizing: false,
      reducing: true,
      volatile: true,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 2,
    sequenceNo: 2,
    wasteCode: "900-252-12",
    sourceUnit: "甬润",
    wasteName: "油漆渣",
    appearance: "黑色粘稠固体",
    harmfulComponents: "",
    storageLocation: "B区",
    remainingStorage: 1500.0,
    ph: 7.0,
    heatValue: 3331,
    waterContent: 26.8,
    flashPoint: null,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 3,
    sequenceNo: 3,
    wasteCode: "900-041-49",
    sourceUnit: "华东制药",
    wasteName: "废活性炭",
    appearance: "黑色颗粒固体",
    harmfulComponents: "有机污染物",
    storageLocation: "C区",
    remainingStorage: 1250.5,
    ph: 6.8,
    heatValue: 6200,
    waterContent: 5.2,
    flashPoint: 420,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: false,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 4,
    sequenceNo: 4,
    wasteCode: "261-084-45",
    sourceUnit: "康恩贝",
    wasteName: "制药废料",
    appearance: "白色粉末状固体",
    harmfulComponents: "有机合成残留物",
    storageLocation: "D区",
    remainingStorage: 756.3,
    ph: 3.2,
    heatValue: 4250,
    waterContent: 18.7,
    flashPoint: 95,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: true
    }
  },
  {
    id: 5,
    sequenceNo: 5,
    wasteCode: "772-006-18",
    sourceUnit: "浙江印染厂",
    wasteName: "印染污泥",
    appearance: "深色泥状物",
    harmfulComponents: "重金属化合物",
    storageLocation: "E区",
    remainingStorage: 2150.8,
    ph: 7.8,
    heatValue: 2800,
    waterContent: 65.2,
    flashPoint: null,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 6,
    sequenceNo: 6,
    wasteCode: "900-013-11",
    sourceUnit: "汽修厂",
    wasteName: "废机油",
    appearance: "黑色粘稠液体",
    harmfulComponents: "多环芳烃",
    storageLocation: "F区",
    remainingStorage: 1800.0,
    ph: null,
    heatValue: 9500,
    waterContent: 2.1,
    flashPoint: 180,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 7,
    sequenceNo: 7,
    wasteCode: "265-103-29",
    sourceUnit: "农化公司",
    wasteName: "农药废料",
    appearance: "棕色粉末",
    harmfulComponents: "有机磷化合物",
    storageLocation: "G区",
    remainingStorage: 450.2,
    ph: 5.5,
    heatValue: 3800,
    waterContent: 8.3,
    flashPoint: 85,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: true,
      flammable: false,
      toxic: true,
      reactive: true,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 8,
    sequenceNo: 8,
    wasteCode: "336-064-17",
    sourceUnit: "电镀园区",
    wasteName: "电镀污泥",
    appearance: "灰绿色泥状物",
    harmfulComponents: "铬、镍、铜",
    storageLocation: "H区",
    remainingStorage: 1680.5,
    ph: 9.8,
    heatValue: 1950,
    waterContent: 58.7,
    flashPoint: null,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: true
    }
  },
  {
    id: 9,
    sequenceNo: 9,
    wasteCode: "900-249-08",
    sourceUnit: "化工厂",
    wasteName: "废溶剂",
    appearance: "无色透明液体",
    harmfulComponents: "苯系物",
    storageLocation: "I区",
    remainingStorage: 890.3,
    ph: null,
    heatValue: 7200,
    waterContent: 3.8,
    flashPoint: 28,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: true,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 10,
    sequenceNo: 10,
    wasteCode: "251-015-10",
    sourceUnit: "塑料厂",
    wasteName: "树脂废料",
    appearance: "白色块状固体",
    harmfulComponents: "聚合物单体",
    storageLocation: "J区",
    remainingStorage: 1200.7,
    ph: 7.2,
    heatValue: 8800,
    waterContent: 1.2,
    flashPoint: 380,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: false,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 11,
    sequenceNo: 11,
    wasteCode: "900-047-49",
    sourceUnit: "石化厂",
    wasteName: "废催化剂",
    appearance: "灰色颗粒状固体",
    harmfulComponents: "贵金属",
    storageLocation: "K区",
    remainingStorage: 680.9,
    ph: 8.8,
    heatValue: 2100,
    waterContent: 5.8,
    flashPoint: null,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 12,
    sequenceNo: 12,
    wasteCode: "900-039-49",
    sourceUnit: "钢铁厂",
    wasteName: "废酸液",
    appearance: "无色透明液体",
    harmfulComponents: "硫酸",
    storageLocation: "L区",
    remainingStorage: 2500.0,
    ph: 0.8,
    heatValue: 500,
    waterContent: 85.2,
    flashPoint: null,
    properties: {
      oxidizing: true,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: true,
      infectious: false,
      corrosive: true
    }
  },
  {
    id: 13,
    sequenceNo: 13,
    wasteCode: "261-041-45",
    sourceUnit: "制药企业",
    wasteName: "抗生素废料",
    appearance: "淡黄色粉末",
    harmfulComponents: "抗生素残留",
    storageLocation: "M区",
    remainingStorage: 320.5,
    ph: 6.2,
    heatValue: 4100,
    waterContent: 25.8,
    flashPoint: 110,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: true,
      corrosive: false
    }
  },
  {
    id: 14,
    sequenceNo: 14,
    wasteCode: "900-005-01",
    sourceUnit: "电力公司",
    wasteName: "废变压器油",
    appearance: "淡黄色液体",
    harmfulComponents: "PCBs",
    storageLocation: "N区",
    remainingStorage: 1500.0,
    ph: null,
    heatValue: 9200,
    waterContent: 0.05,
    flashPoint: 145,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 15,
    sequenceNo: 15,
    wasteCode: "772-001-18",
    sourceUnit: "化工园区",
    wasteName: "化工废盐",
    appearance: "白色结晶状固体",
    harmfulComponents: "氯化钠",
    storageLocation: "O区",
    remainingStorage: 800.2,
    ph: 8.5,
    heatValue: 800,
    waterContent: 15.8,
    flashPoint: null,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: false,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 16,
    sequenceNo: 16,
    wasteCode: "900-046-49",
    sourceUnit: "VOCs治理",
    wasteName: "废吸附剂",
    appearance: "黑色颗粒状固体",
    harmfulComponents: "挥发性有机物",
    storageLocation: "P区",
    remainingStorage: 950.8,
    ph: 7.5,
    heatValue: 5800,
    waterContent: 8.5,
    flashPoint: 220,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: true,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 17,
    sequenceNo: 17,
    wasteCode: "336-100-17",
    sourceUnit: "电子厂",
    wasteName: "线路板废料",
    appearance: "绿色板状固体",
    harmfulComponents: "铜、锡、铅",
    storageLocation: "Q区",
    remainingStorage: 1100.3,
    ph: 6.8,
    heatValue: 4500,
    waterContent: 2.1,
    flashPoint: 280,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: false,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 18,
    sequenceNo: 18,
    wasteCode: "261-062-45",
    sourceUnit: "实验室",
    wasteName: "化学试剂废料",
    appearance: "混合色液体",
    harmfulComponents: "多种化学物质",
    storageLocation: "R区",
    remainingStorage: 180.5,
    ph: 4.5,
    heatValue: 3200,
    waterContent: 15.2,
    flashPoint: 75,
    properties: {
      oxidizing: true,
      reducing: true,
      volatile: true,
      flammable: true,
      toxic: true,
      reactive: true,
      infectious: false,
      corrosive: true
    }
  },
  {
    id: 19,
    sequenceNo: 19,
    wasteCode: "900-035-49",
    sourceUnit: "机械厂",
    wasteName: "废切削液",
    appearance: "乳白色液体",
    harmfulComponents: "矿物油",
    storageLocation: "S区",
    remainingStorage: 1350.0,
    ph: 8.8,
    heatValue: 6800,
    waterContent: 88.5,
    flashPoint: 95,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: false,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 20,
    sequenceNo: 20,
    wasteCode: "772-005-18",
    sourceUnit: "焦化厂",
    wasteName: "焦化废料",
    appearance: "黑色固体",
    harmfulComponents: "苯并芘",
    storageLocation: "T区",
    remainingStorage: 2200.1,
    ph: 8.2,
    heatValue: 5500,
    waterContent: 35.2,
    flashPoint: 180,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 21,
    sequenceNo: 21,
    wasteCode: "900-299-12",
    sourceUnit: "涂装厂",
    wasteName: "废漆渣",
    appearance: "多色粘稠固体",
    harmfulComponents: "有机溶剂",
    storageLocation: "U区",
    remainingStorage: 850.5,
    ph: 7.8,
    heatValue: 7200,
    waterContent: 15.8,
    flashPoint: 45,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: true,
      flammable: true,
      toxic: true,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 22,
    sequenceNo: 22,
    wasteCode: "261-071-45",
    sourceUnit: "农化企业",
    wasteName: "农药中间体",
    appearance: "黄色液体",
    harmfulComponents: "有机氯化物",
    storageLocation: "V区",
    remainingStorage: 425.8,
    ph: 4.2,
    heatValue: 4800,
    waterContent: 12.5,
    flashPoint: 105,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: true,
      flammable: false,
      toxic: true,
      reactive: true,
      infectious: false,
      corrosive: false
    }
  },
  {
    id: 23,
    sequenceNo: 23,
    wasteCode: "900-007-01",
    sourceUnit: "工业企业",
    wasteName: "废润滑油",
    appearance: "深色粘稠液体",
    harmfulComponents: "添加剂",
    storageLocation: "W区",
    remainingStorage: 1680.0,
    ph: null,
    heatValue: 9800,
    waterContent: 0.8,
    flashPoint: 195,
    properties: {
      oxidizing: false,
      reducing: false,
      volatile: false,
      flammable: true,
      toxic: false,
      reactive: false,
      infectious: false,
      corrosive: false
    }
  }
];

// Compatibility matrix - defines which wastes are compatible
const compatibilityMatrix = {
  // Key format: "waste1_id-waste2_id": boolean
  "1-2": true,   // 高沸物 + 油漆渣
  "1-3": false,  // 高沸物 + 废活性炭 (挥发性不相容)
  "1-6": true,   // 高沸物 + 废机油
  "2-6": true,   // 油漆渣 + 废机油
  "4-12": false, // 制药废料 + 废酸液 (强酸性)
  "5-8": true,   // 印染污泥 + 电镀污泥
  "7-13": false, // 农药废料 + 抗生素废料 (生物冲突)
  "9-10": true,  // 废溶剂 + 树脂废料
  "14-23": true, // 废变压器油 + 废润滑油
  "3-16": true,  // 废活性炭 + 废吸附剂
  "11-17": false, // 废催化剂 + 线路板废料 (金属冲突)
  "18-7": false,  // 化学试剂废料 + 农药废料 (复杂反应)
  "19-6": true,   // 废切削液 + 废机油
  "20-21": true,  // 焦化废料 + 废漆渣
  "15-5": false,  // 化工废盐 + 印染污泥 (盐分问题)
};

// Thermal treatment constraints
const thermalConstraints = {
  maxHeatValue: 8000,    // cal/g
  minHeatValue: 2000,    // cal/g
  maxWaterContent: 50,   // %
  phRange: {min: 6, max: 9},
  maxHeavyMetals: 0.5,   // %
  maxChlorine: 2.0,      // %
  maxSulfur: 3.0        // %
};

module.exports = {
  wasteData,
  compatibilityMatrix,
  thermalConstraints
}; 