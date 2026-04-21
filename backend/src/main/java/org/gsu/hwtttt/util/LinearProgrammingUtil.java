package org.gsu.hwtttt.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.gsu.hwtttt.dto.BlendingConstraints;
import org.gsu.hwtttt.dto.BlendingResult;
import org.gsu.hwtttt.dto.WasteBlendingData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 线性规划算法工具类
 * 实现简化的线性规划求解器用于配伍计算
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Component
public class LinearProgrammingUtil {

    /**
     * 配伍控制参数
     */
    @Data
    public static class ControlParameters {
        private BigDecimal heatValueMin = new BigDecimal("12500");    // 热值最小值 kJ/kg
        private BigDecimal heatValueMax = new BigDecimal("16800");    // 热值最大值 kJ/kg
        private BigDecimal waterContentMax = new BigDecimal("45");     // 水分最大值 %
        private BigDecimal nContentMax = new BigDecimal("2");          // N含量最大值 %
        private BigDecimal sContentMax = new BigDecimal("3");          // S含量最大值 %
        private BigDecimal clContentMax = new BigDecimal("1.5");       // Cl含量最大值 %
        private BigDecimal fContentMax = new BigDecimal("1");          // F含量最大值 %
        private BigDecimal hgContentMax = new BigDecimal("4");         // Hg含量最大值 mg/kg
        private BigDecimal cdContentMax = new BigDecimal("1");         // Cd含量最大值 mg/kg
        private BigDecimal asNiContentMax = new BigDecimal("95");      // As+Ni含量最大值 mg/kg
        private BigDecimal pbContentMax = new BigDecimal("70");        // Pb含量最大值 mg/kg
        private BigDecimal heavyMetalContentMax = new BigDecimal("800"); // Cr+Sn+Sb+Cu+Mn含量最大值 mg/kg
    }

    /**
     * 危废数据
     */
    @Data
    public static class WasteData {
        private Long wasteId;
        private BigDecimal maxQuantity;    // 最大可用量(kg)
        private BigDecimal heatValue;      // 热值(kJ/kg)
        private BigDecimal waterContent;   // 水分(%)
        private BigDecimal nContent;       // N含量(%)
        private BigDecimal sContent;       // S含量(%)
        private BigDecimal clContent;      // Cl含量(%)
        private BigDecimal fContent;       // F含量(%)
        private BigDecimal hgContent;      // Hg含量(mg/kg)
        private BigDecimal cdContent;      // Cd含量(mg/kg)
        private BigDecimal asContent;      // As含量(mg/kg)
        private BigDecimal niContent;      // Ni含量(mg/kg)
        private BigDecimal pbContent;      // Pb含量(mg/kg)
        private BigDecimal crContent;      // Cr含量(mg/kg)
        private BigDecimal snContent;      // Sn含量(mg/kg)
        private BigDecimal sbContent;      // Sb含量(mg/kg)
        private BigDecimal cuContent;      // Cu含量(mg/kg)
        private BigDecimal mnContent;      // Mn含量(mg/kg)
        private String constraintType;     // 约束类型
        private BigDecimal constraintValue; // 约束值
    }

    /**
     * 求解结果
     */
    @Data
    public static class SolutionResult {
        private boolean feasible;                        // 是否有可行解
        private Map<Long, BigDecimal> wasteQuantities;   // 各危废用量(kg)
        private BigDecimal totalQuantity;                // 总用量(kg)
        private MixtureProperties mixtureProperties;     // 混合后指标
        private List<ConstraintViolation> violations;    // 约束违反情况
        private String errorMessage;                     // 错误信息
        private long solutionTime;                       // 求解时间(ms)
    }

    /**
     * 混合后指标
     */
    @Data
    public static class MixtureProperties {
        private BigDecimal heatValue;
        private BigDecimal waterContent;
        private BigDecimal nContent;
        private BigDecimal sContent;
        private BigDecimal clContent;
        private BigDecimal fContent;
        private BigDecimal hgContent;
        private BigDecimal cdContent;
        private BigDecimal asNiContent;
        private BigDecimal pbContent;
        private BigDecimal heavyMetalContent;
    }

    /**
     * 约束违反
     */
    @Data
    public static class ConstraintViolation {
        private String constraintName;
        private BigDecimal currentValue;
        private BigDecimal limitValue;
        private BigDecimal violation;
    }

    /**
     * 求解配伍问题
     *
     * @param wasteDataList 危废数据列表
     * @param totalCapacity 总处理量(kg)
     * @param parameters    控制参数
     * @return 求解结果
     */
    public SolutionResult solve(List<WasteData> wasteDataList, BigDecimal totalCapacity, 
                               ControlParameters parameters) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始求解配伍问题，危废数量：{}，总处理量：{}kg", wasteDataList.size(), totalCapacity);
            
            // 1. 预处理：检查数据有效性
            if (wasteDataList.isEmpty()) {
                return createErrorResult("危废列表为空", startTime);
            }
            
            // 2. 构建线性规划模型
            LinearModel model = buildLinearModel(wasteDataList, totalCapacity, parameters);
            
            // 3. 使用简化的启发式算法求解
            SolutionResult result = solveByHeuristic(model, wasteDataList, totalCapacity, parameters);
            
            result.setSolutionTime(System.currentTimeMillis() - startTime);
            log.info("配伍问题求解完成，用时：{}ms，是否有可行解：{}", result.getSolutionTime(), result.isFeasible());
            
            return result;
            
        } catch (Exception e) {
            log.error("配伍问题求解失败", e);
            return createErrorResult("求解过程中发生错误：" + e.getMessage(), startTime);
        }
    }

    /**
     * 构建线性规划模型
     */
    private LinearModel buildLinearModel(List<WasteData> wasteDataList, BigDecimal totalCapacity, 
                                       ControlParameters parameters) {
        LinearModel model = new LinearModel();
        model.setVariableCount(wasteDataList.size());
        model.setMaxQuantities(new BigDecimal[wasteDataList.size()]);
        
        // 设置变量上界（库存约束）
        for (int i = 0; i < wasteDataList.size(); i++) {
            model.getMaxQuantities()[i] = wasteDataList.get(i).getMaxQuantity();
        }
        
        return model;
    }

    /**
     * 使用启发式算法求解
     */
    private SolutionResult solveByHeuristic(LinearModel model, List<WasteData> wasteDataList, 
                                           BigDecimal totalCapacity, ControlParameters parameters) {
        SolutionResult result = new SolutionResult();
        result.setWasteQuantities(new HashMap<>());
        result.setViolations(new ArrayList<>());
        
        try {
            // 1. 初始化：按库存比例分配
            Map<Long, BigDecimal> quantities = initializeByStock(wasteDataList, totalCapacity);
            
            // 2. 迭代优化
            for (int iteration = 0; iteration < 100; iteration++) {
                // 计算当前混合指标
                MixtureProperties properties = calculateMixtureProperties(quantities, wasteDataList);
                
                // 检查约束
                List<ConstraintViolation> violations = checkConstraints(properties, parameters);
                
                if (violations.isEmpty()) {
                    // 找到可行解
                    result.setFeasible(true);
                    result.setWasteQuantities(quantities);
                    result.setMixtureProperties(properties);
                    result.setTotalQuantity(totalCapacity);
                    return result;
                }
                
                // 调整配比以修复约束违反
                quantities = adjustQuantities(quantities, violations, wasteDataList, totalCapacity);
                
                if (quantities == null) {
                    break; // 无法找到可行解
                }
            }
            
            // 无法找到严格可行解，返回最佳近似解
            MixtureProperties properties = calculateMixtureProperties(quantities, wasteDataList);
            List<ConstraintViolation> violations = checkConstraints(properties, parameters);
            
            result.setFeasible(false);
            result.setWasteQuantities(quantities);
            result.setMixtureProperties(properties);
            result.setViolations(violations);
            result.setTotalQuantity(totalCapacity);
            result.setErrorMessage("未找到完全满足约束的可行解，返回最佳近似解");
            
        } catch (Exception e) {
            result.setFeasible(false);
            result.setErrorMessage("求解过程中发生错误：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 按库存比例初始化分配
     */
    private Map<Long, BigDecimal> initializeByStock(List<WasteData> wasteDataList, BigDecimal totalCapacity) {
        Map<Long, BigDecimal> quantities = new HashMap<>();
        
        // 计算总库存
        BigDecimal totalStock = wasteDataList.stream()
                .map(WasteData::getMaxQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按库存比例分配
        for (WasteData waste : wasteDataList) {
            BigDecimal ratio = waste.getMaxQuantity().divide(totalStock, 6, RoundingMode.HALF_UP);
            BigDecimal quantity = totalCapacity.multiply(ratio);
            
            // 确保不超过库存
            if (quantity.compareTo(waste.getMaxQuantity()) > 0) {
                quantity = waste.getMaxQuantity();
            }
            
            quantities.put(waste.getWasteId(), quantity);
        }
        
        // 调整总量
        BigDecimal actualTotal = quantities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (actualTotal.compareTo(totalCapacity) != 0) {
            // 按比例调整
            BigDecimal adjustRatio = totalCapacity.divide(actualTotal, 6, RoundingMode.HALF_UP);
            for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
                entry.setValue(entry.getValue().multiply(adjustRatio));
            }
        }
        
        return quantities;
    }

    /**
     * 计算混合后指标
     */
    private MixtureProperties calculateMixtureProperties(Map<Long, BigDecimal> quantities, 
                                                       List<WasteData> wasteDataList) {
        MixtureProperties properties = new MixtureProperties();
        
        BigDecimal totalQuantity = quantities.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return properties;
        }
        
        BigDecimal totalHeatValue = BigDecimal.ZERO;
        BigDecimal totalWaterContent = BigDecimal.ZERO;
        BigDecimal totalNContent = BigDecimal.ZERO;
        BigDecimal totalSContent = BigDecimal.ZERO;
        BigDecimal totalClContent = BigDecimal.ZERO;
        BigDecimal totalFContent = BigDecimal.ZERO;
        BigDecimal totalHgContent = BigDecimal.ZERO;
        BigDecimal totalCdContent = BigDecimal.ZERO;
        BigDecimal totalAsNiContent = BigDecimal.ZERO;
        BigDecimal totalPbContent = BigDecimal.ZERO;
        BigDecimal totalHeavyMetalContent = BigDecimal.ZERO;
        
        for (WasteData waste : wasteDataList) {
            BigDecimal quantity = quantities.getOrDefault(waste.getWasteId(), BigDecimal.ZERO);
            
            if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                totalHeatValue = totalHeatValue.add(quantity.multiply(nvl(waste.getHeatValue())));
                totalWaterContent = totalWaterContent.add(quantity.multiply(nvl(waste.getWaterContent())));
                totalNContent = totalNContent.add(quantity.multiply(nvl(waste.getNContent())));
                totalSContent = totalSContent.add(quantity.multiply(nvl(waste.getSContent())));
                totalClContent = totalClContent.add(quantity.multiply(nvl(waste.getClContent())));
                totalFContent = totalFContent.add(quantity.multiply(nvl(waste.getFContent())));
                totalHgContent = totalHgContent.add(quantity.multiply(nvl(waste.getHgContent())));
                totalCdContent = totalCdContent.add(quantity.multiply(nvl(waste.getCdContent())));
                
                BigDecimal asNi = nvl(waste.getAsContent()).add(nvl(waste.getNiContent()));
                totalAsNiContent = totalAsNiContent.add(quantity.multiply(asNi));
                
                totalPbContent = totalPbContent.add(quantity.multiply(nvl(waste.getPbContent())));
                
                BigDecimal heavyMetal = nvl(waste.getCrContent())
                        .add(nvl(waste.getSnContent()))
                        .add(nvl(waste.getSbContent()))
                        .add(nvl(waste.getCuContent()))
                        .add(nvl(waste.getMnContent()));
                totalHeavyMetalContent = totalHeavyMetalContent.add(quantity.multiply(heavyMetal));
            }
        }
        
        // 计算加权平均值
        properties.setHeatValue(totalHeatValue.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setWaterContent(totalWaterContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setNContent(totalNContent.divide(totalQuantity, 4, RoundingMode.HALF_UP));
        properties.setSContent(totalSContent.divide(totalQuantity, 4, RoundingMode.HALF_UP));
        properties.setClContent(totalClContent.divide(totalQuantity, 4, RoundingMode.HALF_UP));
        properties.setFContent(totalFContent.divide(totalQuantity, 4, RoundingMode.HALF_UP));
        properties.setHgContent(totalHgContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setCdContent(totalCdContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setAsNiContent(totalAsNiContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setPbContent(totalPbContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        properties.setHeavyMetalContent(totalHeavyMetalContent.divide(totalQuantity, 2, RoundingMode.HALF_UP));
        
        return properties;
    }

    /**
     * 检查约束
     */
    private List<ConstraintViolation> checkConstraints(MixtureProperties properties, ControlParameters parameters) {
        List<ConstraintViolation> violations = new ArrayList<>();
        
        // 热值约束
        if (properties.getHeatValue().compareTo(parameters.getHeatValueMin()) < 0) {
            violations.add(createViolation("热值下限", properties.getHeatValue(), parameters.getHeatValueMin()));
        }
        if (properties.getHeatValue().compareTo(parameters.getHeatValueMax()) > 0) {
            violations.add(createViolation("热值上限", properties.getHeatValue(), parameters.getHeatValueMax()));
        }
        
        // 其他约束...
        if (properties.getWaterContent().compareTo(parameters.getWaterContentMax()) > 0) {
            violations.add(createViolation("水分", properties.getWaterContent(), parameters.getWaterContentMax()));
        }
        
        return violations;
    }

    /**
     * 调整配比
     */
    private Map<Long, BigDecimal> adjustQuantities(Map<Long, BigDecimal> quantities, 
                                                 List<ConstraintViolation> violations,
                                                 List<WasteData> wasteDataList, 
                                                 BigDecimal totalCapacity) {
        // 简化的调整策略：随机微调
        Map<Long, BigDecimal> newQuantities = new HashMap<>(quantities);
        
        // 选择一个危废进行调整
        if (!wasteDataList.isEmpty()) {
            WasteData waste = wasteDataList.get(new Random().nextInt(wasteDataList.size()));
            BigDecimal currentQuantity = newQuantities.get(waste.getWasteId());
            BigDecimal adjustment = currentQuantity.multiply(new BigDecimal("0.1")); // 调整10%
            
            if (new Random().nextBoolean()) {
                newQuantities.put(waste.getWasteId(), currentQuantity.add(adjustment));
            } else {
                newQuantities.put(waste.getWasteId(), currentQuantity.subtract(adjustment));
            }
            
            // 确保不超过库存和非负
            BigDecimal newQuantity = newQuantities.get(waste.getWasteId());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                newQuantities.put(waste.getWasteId(), BigDecimal.ZERO);
            }
            if (newQuantity.compareTo(waste.getMaxQuantity()) > 0) {
                newQuantities.put(waste.getWasteId(), waste.getMaxQuantity());
            }
        }
        
        return newQuantities;
    }

    /**
     * 创建约束违反对象
     */
    private ConstraintViolation createViolation(String name, BigDecimal current, BigDecimal limit) {
        ConstraintViolation violation = new ConstraintViolation();
        violation.setConstraintName(name);
        violation.setCurrentValue(current);
        violation.setLimitValue(limit);
        violation.setViolation(current.subtract(limit).abs());
        return violation;
    }

    /**
     * 创建错误结果
     */
    private SolutionResult createErrorResult(String errorMessage, long startTime) {
        SolutionResult result = new SolutionResult();
        result.setFeasible(false);
        result.setErrorMessage(errorMessage);
        result.setSolutionTime(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 空值处理
     */
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * 线性规划模型
     */
    @Data
    private static class LinearModel {
        private int variableCount;
        private BigDecimal[] maxQuantities;
    }

    /**
     * 求解配伍优化问题
     */
    public static BlendingResult solve(List<WasteBlendingData> wasteDataList, BlendingConstraints constraints) {
        BlendingResult result = new BlendingResult();
        
        try {
            // 检查输入数据
            if (wasteDataList == null || wasteDataList.isEmpty()) {
                result.setFeasible(false);
                result.setFailureReason("没有危废数据");
                return result;
            }

            // 简化的可行性检查（实际项目中需要使用专业的线性规划求解器）
            boolean feasible = checkConstraints(wasteDataList, constraints);
            
            if (feasible) {
                // 计算配伍结果
                result.setFeasible(true);
                result.setTotalQuantity(calculateTotalQuantity(wasteDataList));
                result.setAverageHeatValue(calculateAverageHeatValue(wasteDataList));
                result.setAverageMoisture(calculateAverageMoisture(wasteDataList));
                result.setElementComposition(calculateElementComposition(wasteDataList));
                result.setHeavyMetalContent(calculateHeavyMetalContent(wasteDataList));
                result.setWasteAllocations(getWasteAllocations(wasteDataList));
                
            } else {
                result.setFeasible(false);
                result.setFailureReason("配伍约束条件不满足");
                result.setConstraintViolations(getConstraintViolations(wasteDataList, constraints));
            }
            
        } catch (Exception e) {
            log.error("线性规划求解失败", e);
            result.setFeasible(false);
            result.setFailureReason("计算过程发生异常: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 检查约束条件
     */
    private static boolean checkConstraints(List<WasteBlendingData> wasteDataList, BlendingConstraints constraints) {
        double totalQuantity = calculateTotalQuantity(wasteDataList);
        if (totalQuantity <= 0) {
            return false;
        }

        // 检查热值约束
        double avgHeatValue = calculateAverageHeatValue(wasteDataList);
        if (avgHeatValue < constraints.getMinHeatValue() || avgHeatValue > constraints.getMaxHeatValue()) {
            return false;
        }

        // 检查水分约束
        double avgMoisture = calculateAverageMoisture(wasteDataList);
        if (avgMoisture > constraints.getMaxMoisture()) {
            return false;
        }

        // 检查元素含量约束
        Map<String, Double> elementComposition = calculateElementComposition(wasteDataList);
        if (elementComposition.get("N") > constraints.getMaxNContent() ||
            elementComposition.get("S") > constraints.getMaxSContent() ||
            elementComposition.get("Cl") > constraints.getMaxClContent() ||
            elementComposition.get("F") > constraints.getMaxFContent()) {
            return false;
        }

        // 检查重金属约束
        Map<String, Double> heavyMetals = calculateHeavyMetalContent(wasteDataList);
        if (heavyMetals.get("Cd") > constraints.getMaxCdContent() ||
            heavyMetals.get("AsNiTotal") > constraints.getMaxAsNiTotal() ||
            heavyMetals.get("Pb") > constraints.getMaxPbContent() ||
            heavyMetals.get("CrSnSbCuMnTotal") > constraints.getMaxCrSnSbCuMnTotal()) {
            return false;
        }

        return true;
    }

    /**
     * 计算总用量
     */
    private static double calculateTotalQuantity(List<WasteBlendingData> wasteDataList) {
        return wasteDataList.stream()
                .mapToDouble(WasteBlendingData::getQuantity)
                .sum();
    }

    /**
     * 计算平均热值
     */
    private static double calculateAverageHeatValue(List<WasteBlendingData> wasteDataList) {
        double totalQuantity = calculateTotalQuantity(wasteDataList);
        if (totalQuantity <= 0) return 0;

        double weightedHeatValue = wasteDataList.stream()
                .filter(data -> data.getHeatValue() != null)
                .mapToDouble(data -> data.getQuantity() * data.getHeatValue())
                .sum();

        return weightedHeatValue / totalQuantity;
    }

    /**
     * 计算平均水分含量
     */
    private static double calculateAverageMoisture(List<WasteBlendingData> wasteDataList) {
        double totalQuantity = calculateTotalQuantity(wasteDataList);
        if (totalQuantity <= 0) return 0;

        double weightedMoisture = wasteDataList.stream()
                .filter(data -> data.getMoisture() != null)
                .mapToDouble(data -> data.getQuantity() * data.getMoisture())
                .sum();

        return weightedMoisture / totalQuantity;
    }

    /**
     * 计算元素组成
     */
    private static Map<String, Double> calculateElementComposition(List<WasteBlendingData> wasteDataList) {
        Map<String, Double> composition = new HashMap<>();
        double totalQuantity = calculateTotalQuantity(wasteDataList);
        
        if (totalQuantity <= 0) {
            composition.put("N", 0.0);
            composition.put("S", 0.0);
            composition.put("Cl", 0.0);
            composition.put("F", 0.0);
            return composition;
        }

        // 计算加权平均元素含量
        double nContent = wasteDataList.stream()
                .filter(data -> data.getNContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getNContent())
                .sum() / totalQuantity;

        double sContent = wasteDataList.stream()
                .filter(data -> data.getSContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getSContent())
                .sum() / totalQuantity;

        double clContent = wasteDataList.stream()
                .filter(data -> data.getClContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getClContent())
                .sum() / totalQuantity;

        double fContent = wasteDataList.stream()
                .filter(data -> data.getFContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getFContent())
                .sum() / totalQuantity;

        composition.put("N", nContent);
        composition.put("S", sContent);
        composition.put("Cl", clContent);
        composition.put("F", fContent);

        return composition;
    }

    /**
     * 计算重金属含量
     */
    private static Map<String, Double> calculateHeavyMetalContent(List<WasteBlendingData> wasteDataList) {
        Map<String, Double> heavyMetals = new HashMap<>();
        double totalQuantity = calculateTotalQuantity(wasteDataList);
        
        if (totalQuantity <= 0) {
            heavyMetals.put("Cd", 0.0);
            heavyMetals.put("AsNiTotal", 0.0);
            heavyMetals.put("Pb", 0.0);
            heavyMetals.put("CrSnSbCuMnTotal", 0.0);
            return heavyMetals;
        }

        // 计算各重金属的加权平均含量
        double cdContent = wasteDataList.stream()
                .filter(data -> data.getCdContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getCdContent())
                .sum() / totalQuantity;

        double asNiTotal = wasteDataList.stream()
                .mapToDouble(data -> {
                    double as = data.getAsContent() != null ? data.getAsContent() : 0;
                    double ni = data.getNiContent() != null ? data.getNiContent() : 0;
                    return data.getQuantity() * (as + ni);
                })
                .sum() / totalQuantity;

        double pbContent = wasteDataList.stream()
                .filter(data -> data.getPbContent() != null)
                .mapToDouble(data -> data.getQuantity() * data.getPbContent())
                .sum() / totalQuantity;

        double crSnSbCuMnTotal = wasteDataList.stream()
                .mapToDouble(data -> {
                    double cr = data.getCrContent() != null ? data.getCrContent() : 0;
                    double sn = data.getSnContent() != null ? data.getSnContent() : 0;
                    double sb = data.getSbContent() != null ? data.getSbContent() : 0;
                    double cu = data.getCuContent() != null ? data.getCuContent() : 0;
                    double mn = data.getMnContent() != null ? data.getMnContent() : 0;
                    return data.getQuantity() * (cr + sn + sb + cu + mn);
                })
                .sum() / totalQuantity;

        heavyMetals.put("Cd", cdContent);
        heavyMetals.put("AsNiTotal", asNiTotal);
        heavyMetals.put("Pb", pbContent);
        heavyMetals.put("CrSnSbCuMnTotal", crSnSbCuMnTotal);

        return heavyMetals;
    }

    /**
     * 获取危废用量分配
     */
    private static Map<Long, Double> getWasteAllocations(List<WasteBlendingData> wasteDataList) {
        Map<Long, Double> allocations = new HashMap<>();
        for (WasteBlendingData data : wasteDataList) {
            allocations.put(data.getWasteId(), data.getQuantity());
        }
        return allocations;
    }

    /**
     * 获取约束违反情况
     */
    private static List<String> getConstraintViolations(List<WasteBlendingData> wasteDataList, BlendingConstraints constraints) {
        // TODO: 实现具体的约束违反检查，返回违反的约束说明
        return java.util.Arrays.asList("约束条件检查待实现");
    }
} 