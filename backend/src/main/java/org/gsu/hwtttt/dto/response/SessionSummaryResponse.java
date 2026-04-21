package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Session Summary Response DTO
 * Provides comprehensive session overview including waste details, 
 * quantities, compatibility status, and calculation readiness
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Data
@ApiModel(value = "SessionSummaryResponse", description = "Session Summary Response")
public class SessionSummaryResponse {

    @ApiModelProperty("Session ID")
    private Long sessionId;

    @ApiModelProperty("Session name")
    private String sessionName;

    @ApiModelProperty("Session status")
    private String status;

    @ApiModelProperty("Total planned amount (kg)")
    private BigDecimal totalAmount;

    @ApiModelProperty("Target heat value (kJ/kg)")
    private BigDecimal targetHeatValue;

    @ApiModelProperty("Number of wastes in session")
    private Integer wasteCount;

    @ApiModelProperty("Waste details with quantities")
    private List<WasteSummaryItem> wastes;

    @ApiModelProperty("Compatibility status")
    private CompatibilitySummary compatibility;

    @ApiModelProperty("Calculation readiness")
    private CalculationReadiness readiness;

    @ApiModelProperty("Last update time")
    private LocalDateTime lastUpdateTime;

    /**
     * Waste Summary Item
     */
    @Data
    @ApiModel(value = "WasteSummaryItem", description = "Waste Summary Item")
    public static class WasteSummaryItem {

        @ApiModelProperty("Waste ID")
        private Long wasteId;

        @ApiModelProperty("Waste code")
        private String wasteCode;

        @ApiModelProperty("Waste name")
        private String wasteName;

        @ApiModelProperty("Source unit")
        private String sourceUnit;

        @ApiModelProperty("Planned amount (kg)")
        private BigDecimal plannedAmount;

        @ApiModelProperty("Remaining storage (kg)")
        private BigDecimal remainingStorage;

        @ApiModelProperty("Stock sufficient indicator")
        private Boolean stockSufficient;

        @ApiModelProperty("Heat value (kJ/kg)")
        private BigDecimal heatValueKjPerKg;

        @ApiModelProperty("Water content (%)")
        private BigDecimal waterContentPercent;

        @ApiModelProperty("Compatibility category code")
        private Integer compatibilityCategoryCode;

        @ApiModelProperty("Nitrogen content (%)")
        private BigDecimal nitrogenContent;

        @ApiModelProperty("Sulfur content (%)")
        private BigDecimal sulfurContent;

        @ApiModelProperty("Chlorine content (%)")
        private BigDecimal chlorineContent;

        @ApiModelProperty("Fluorine content (%)")
        private BigDecimal fluorineContent;

        @ApiModelProperty("Mercury content (mg/kg)")
        private BigDecimal mercuryContent;

        @ApiModelProperty("Cadmium content (mg/kg)")
        private BigDecimal cadmiumContent;

        @ApiModelProperty("Lead content (mg/kg)")
        private BigDecimal leadContent;
    }

    /**
     * Compatibility Summary
     */
    @Data
    @ApiModel(value = "CompatibilitySummary", description = "Compatibility Summary")
    public static class CompatibilitySummary {

        @ApiModelProperty("Has compatibility check been performed")
        private Boolean checked;

        @ApiModelProperty("Are all wastes compatible")
        private Boolean compatible;

        @ApiModelProperty("Number of incompatible pairs")
        private Integer incompatiblePairs;

        @ApiModelProperty("Risk factors found (H,F,G,GT,E,P,S,U codes)")
        private List<String> riskFactors;

        @ApiModelProperty("Detailed incompatible waste pairs")
        private List<IncompatiblePairDetail> incompatiblePairDetails;
    }

    /**
     * Incompatible Pair Detail
     */
    @Data
    @ApiModel(value = "IncompatiblePairDetail", description = "Incompatible Pair Detail")
    public static class IncompatiblePairDetail {

        @ApiModelProperty("First waste ID")
        private Long wasteId1;

        @ApiModelProperty("First waste code")
        private String wasteCode1;

        @ApiModelProperty("Second waste ID")
        private Long wasteId2;

        @ApiModelProperty("Second waste code")
        private String wasteCode2;

        @ApiModelProperty("Risk codes")
        private List<String> riskCodes;

        @ApiModelProperty("Risk description")
        private String riskDescription;
    }

    /**
     * Calculation Readiness
     */
    @Data
    @ApiModel(value = "CalculationReadiness", description = "Calculation Readiness")
    public static class CalculationReadiness {

        @ApiModelProperty("Can calculation be started")
        private Boolean ready;

        @ApiModelProperty("What prevents calculation")
        private List<String> blockers;

        @ApiModelProperty("Preliminary heat value estimate (kJ/kg)")
        private BigDecimal estimatedHeatValue;

        @ApiModelProperty("Estimated total amount meets minimum")
        private Boolean totalAmountSufficient;

        @ApiModelProperty("All wastes have sufficient stock")
        private Boolean stockSufficient;

        @ApiModelProperty("Compatibility check passed")
        private Boolean compatibilityPassed;

        @ApiModelProperty("Session status allows calculation")
        private Boolean statusReady;
    }
} 