package org.gsu.hwtttt.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unit Conversion Utility for Hazardous Waste Heat Values
 * 
 * Handles conversions between cal/g (database storage) and kJ/kg (constraint validation)
 * Conversion factor: 1 cal/g = 4.184 kJ/kg
 *
 * @author System
 * @date 2025/01/05
 */
@Component
@Slf4j
public class UnitConverter {
    
    private static final BigDecimal CAL_TO_KJ_FACTOR = new BigDecimal("4.184");
    
    /**
     * Convert heat value from cal/g to kJ/kg
     * 
     * @param calPerG Heat value in cal/g
     * @return Heat value in kJ/kg
     */
    public static BigDecimal calPerGToKjPerKg(BigDecimal calPerG) {
        if (calPerG == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = calPerG.multiply(CAL_TO_KJ_FACTOR);
        log.debug("Heat value conversion: {} cal/g → {} kJ/kg", calPerG, result);
        return result;
    }
    
    /**
     * Convert heat value from kJ/kg to cal/g  
     * 
     * @param kjPerKg Heat value in kJ/kg
     * @return Heat value in cal/g
     */
    public static BigDecimal kjPerKgToCalPerG(BigDecimal kjPerKg) {
        if (kjPerKg == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = kjPerKg.divide(CAL_TO_KJ_FACTOR, 2, RoundingMode.HALF_UP);
        log.debug("Heat value conversion: {} kJ/kg → {} cal/g", kjPerKg, result);
        return result;
    }
    
    /**
     * Get the conversion factor from cal/g to kJ/kg
     * 
     * @return Conversion factor (4.184)
     */
    public static BigDecimal getCalToKjConversionFactor() {
        return CAL_TO_KJ_FACTOR;
    }
    
    /**
     * Validate if a heat value is within acceptable range in cal/g
     * 
     * @param calPerG Heat value in cal/g
     * @param minKjPerKg Minimum allowed value in kJ/kg
     * @param maxKjPerKg Maximum allowed value in kJ/kg
     * @return true if within range, false otherwise
     */
    public static boolean isHeatValueInRange(BigDecimal calPerG, BigDecimal minKjPerKg, BigDecimal maxKjPerKg) {
        if (calPerG == null || minKjPerKg == null || maxKjPerKg == null) {
            return false;
        }
        
        BigDecimal kjPerKg = calPerGToKjPerKg(calPerG);
        return kjPerKg.compareTo(minKjPerKg) >= 0 && kjPerKg.compareTo(maxKjPerKg) <= 0;
    }
} 