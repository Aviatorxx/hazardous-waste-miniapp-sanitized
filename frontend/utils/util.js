// Utility functions for the Hazardous Waste Knowledge Base

const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

// Format storage amount with proper units
const formatStorage = (amount) => {
  if (amount >= 1000) {
    // Fix: Use Math.floor to preserve precision and avoid incorrect rounding
    // Convert to tons with 3 decimal places, then truncate to avoid rounding up
    const tons = Math.floor((amount / 1000) * 1000) / 1000;
    return tons.toFixed(3) + 't';
  }
  return amount.toFixed(1) + 'kg';
}

// Format heat value with proper display
const formatHeatValue = (value) => {
  if (!value) return 'N/A'
  return value.toLocaleString() + ' cal/g'
}

// Format pH value with proper precision
const formatPH = (ph) => {
  if (!ph) return 'N/A'
  return ph.toFixed(1)
}

// Format flash point with proper units
const formatFlashPoint = (flashPoint) => {
  if (!flashPoint) return 'N/A'
  return flashPoint + '°C'
}

// Get property tag class based on property type
const getPropertyTagClass = (property) => {
  const tagMap = {
    'flammable': 'tag-flammable',
    'toxic': 'tag-toxic',
    'corrosive': 'tag-corrosive',
    'reactive': 'tag-reactive',
    'oxidizing': 'tag-oxidizing',
    'volatile': 'tag-volatile',
    'infectious': 'tag-infectious'
  }
  return tagMap[property] || 'tag-default'
}

// Get property display name in Chinese
const getPropertyDisplayName = (property) => {
  const nameMap = {
    'flammable': '易燃性',
    'toxic': '毒性',
    'corrosive': '腐蚀性',
    'reactive': '反应性',
    'oxidizing': '氧化性',
    'volatile': '挥发性',
    'infectious': '感染性'
  }
  return nameMap[property] || property
}

// Check if a waste has a specific property
const hasProperty = (waste, property) => {
  return waste.properties && waste.properties[property] === true
}

// Get all active properties for a waste
const getActiveProperties = (waste) => {
  if (!waste.properties) return []
  
  const activeProperties = []
  Object.keys(waste.properties).forEach(key => {
    if (waste.properties[key] === true) {
      activeProperties.push({
        key: key,
        name: getPropertyDisplayName(key),
        class: getPropertyTagClass(key)
      })
    }
  })
  return activeProperties
}

// Calculate compatibility score between two wastes
const calculateCompatibilityScore = (waste1, waste2) => {
  let score = 100
  
  // pH compatibility check
  const ph1 = waste1.ph || 7
  const ph2 = waste2.ph || 7
  const phDiff = Math.abs(ph1 - ph2)
  if (phDiff > 6) score -= 30
  else if (phDiff > 3) score -= 15
  
  // Property conflicts
  if (hasProperty(waste1, 'flammable') && hasProperty(waste2, 'oxidizing')) score -= 40
  if (hasProperty(waste2, 'flammable') && hasProperty(waste1, 'oxidizing')) score -= 40
  if (hasProperty(waste1, 'reactive') && hasProperty(waste2, 'reactive')) score -= 25
  if (hasProperty(waste1, 'corrosive') && hasProperty(waste2, 'volatile')) score -= 20
  if (hasProperty(waste2, 'corrosive') && hasProperty(waste1, 'volatile')) score -= 20
  
  return Math.max(0, score)
}

// Validate thermal treatment constraints
const validateThermalConstraints = (mixedProperties, constraints) => {
  const violations = []
  
  if (mixedProperties.heatValue < constraints.minHeatValue) {
    violations.push(`热值过低: ${mixedProperties.heatValue} < ${constraints.minHeatValue}`)
  }
  
  if (mixedProperties.heatValue > constraints.maxHeatValue) {
    violations.push(`热值过高: ${mixedProperties.heatValue} > ${constraints.maxHeatValue}`)
  }
  
  if (mixedProperties.waterContent > constraints.maxWaterContent) {
    violations.push(`含水率过高: ${mixedProperties.waterContent}% > ${constraints.maxWaterContent}%`)
  }
  
  if (mixedProperties.ph < constraints.phRange.min || mixedProperties.ph > constraints.phRange.max) {
    violations.push(`pH值超出范围: ${mixedProperties.ph} 不在 ${constraints.phRange.min}-${constraints.phRange.max} 范围内`)
  }
  
  return violations
}

// Debounce function for search input
const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

module.exports = {
  formatTime,
  formatStorage,
  formatHeatValue,
  formatPH,
  formatFlashPoint,
  getPropertyTagClass,
  getPropertyDisplayName,
  hasProperty,
  getActiveProperties,
  calculateCompatibilityScore,
  validateThermalConstraints,
  debounce
}
