package org.gsu.hwtttt.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.enums.WasteCategoryEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 相容性计算工具类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Component
public class CompatibilityUtil {

    /**
     * 相容性矩阵（41×41）
     * 0 表示相容，1 表示不相容
     */
    private static final int[][] COMPATIBILITY_MATRIX = new int[42][42]; // 索引从1开始，所以用42

    /**
     * 不相容原因映射
     */
    private static final Map<String, String> INCOMPATIBILITY_REASONS = new HashMap<>();

    /**
     * 初始化相容性矩阵
     */
    @PostConstruct
    public void initCompatibilityMatrix() {
        log.info("开始初始化相容性矩阵...");
        
        // 初始化所有元素为0（相容）
        for (int i = 1; i <= 41; i++) {
            for (int j = 1; j <= 41; j++) {
                COMPATIBILITY_MATRIX[i][j] = 0;
            }
        }

        // 设置不相容关系
        int[][] incompatiblePairs = getIncompatiblePairs();
        for (int[] pair : incompatiblePairs) {
            int category1 = pair[0];
            int category2 = pair[1];
            COMPATIBILITY_MATRIX[category1][category2] = 1;
            COMPATIBILITY_MATRIX[category2][category1] = 1; // 矩阵对称
        }
        
        // 初始化不相容原因
        initIncompatibilityReasons();
        
        log.info("相容性矩阵初始化完成，共有{}对不相容关系", incompatiblePairs.length);
    }

    /**
     * 获取不相容配对关系
     */
    private int[][] getIncompatiblePairs() {
        return new int[][]{
            // 酸类、矿物、亚氧化物(1) 的不相容关系
            {1, 2}, {1, 7}, {1, 8}, {1, 9}, {1, 11}, {1, 18}, {1, 21}, {1, 22}, 
            {1, 24}, {1, 25}, {1, 26}, {1, 27}, {1, 28}, {1, 30}, {1, 31}, {1, 32}, 
            {1, 33}, {1, 34}, {1, 35}, {1, 36}, {1, 37}, {1, 41},
            
            // 碱类、矿物、氧化物(2) 的不相容关系
            {2, 3}, {2, 6}, {2, 8}, {2, 11}, {2, 12}, {2, 13}, {2, 15}, {2, 17}, 
            {2, 18}, {2, 22}, {2, 23}, {2, 24}, {2, 25}, {2, 28}, {2, 34}, {2, 39},
            
            // 酸类、有机的(3) 的不相容关系
            {3, 4}, {3, 6}, {3, 7}, {3, 8}, {3, 9}, {3, 11}, {3, 14}, {3, 18}, 
            {3, 21}, {3, 22}, {3, 24}, {3, 25}, {3, 26}, {3, 27}, {3, 28}, {3, 30}, 
            {3, 31}, {3, 32}, {3, 33}, {3, 34}, {3, 35}, {3, 36}, {3, 37}, {3, 41},
            
            // 醇类及二醇(4) 的不相容关系
            {4, 8}, {4, 11}, {4, 15}, {4, 18}, {4, 22}, {4, 24}, {4, 25}, {4, 27}, 
            {4, 28}, {4, 34}, {4, 35}, {4, 36}, {4, 37}, {4, 39},
            
            // 醛(5) 的不相容关系
            {5, 6}, {5, 7}, {5, 8}, {5, 9}, {5, 11}, {5, 15}, {5, 22}, {5, 23}, 
            {5, 24}, {5, 25}, {5, 27}, {5, 28}, {5, 30}, {5, 31}, {5, 34}, {5, 35}, 
            {5, 36}, {5, 37}, {5, 39},
            
            // 酰胺或酰化物(6) 的不相容关系
            {6, 11}, {6, 15}, {6, 18}, {6, 22}, {6, 24}, {6, 25}, {6, 27}, {6, 28}, 
            {6, 34}, {6, 35}, {6, 36}, {6, 37}, {6, 39},
            
            // 胺类(7) 的不相容关系
            {7, 8}, {7, 11}, {7, 15}, {7, 17}, {7, 18}, {7, 22}, {7, 23}, {7, 24}, 
            {7, 25}, {7, 27}, {7, 28}, {7, 34}, {7, 35}, {7, 36}, {7, 37}, {7, 39},
            
            // 偶氮化合物(8) 的不相容关系
            {8, 9}, {8, 11}, {8, 13}, {8, 14}, {8, 18}, {8, 19}, {8, 20}, {8, 21}, 
            {8, 22}, {8, 23}, {8, 24}, {8, 25}, {8, 26}, {8, 27}, {8, 28}, {8, 30}, 
            {8, 31}, {8, 32}, {8, 33}, {8, 34}, {8, 35}, {8, 36}, {8, 37}, {8, 39}, 
            {8, 41},
            
            // 氨基甲酸酯(9) 的不相容关系
            {9, 11}, {9, 15}, {9, 18}, {9, 22}, {9, 24}, {9, 25}, {9, 27}, {9, 28}, 
            {9, 34}, {9, 35}, {9, 36}, {9, 37}, {9, 39},
            
            // 烯烃(10) 的不相容关系
            {10, 11}, {10, 15}, {10, 22}, {10, 24}, {10, 25}, {10, 27}, {10, 28}, 
            {10, 34}, {10, 35}, {10, 36}, {10, 37}, {10, 39},
            
            // 氰化物(11) 的不相容关系
            {11, 15}, {11, 22}, {11, 24}, {11, 25}, {11, 27}, {11, 28}, {11, 34}, 
            {11, 35}, {11, 36}, {11, 37},
            
            // 二硫代氨基甲酸酯(12) 的不相容关系
            {12, 15}, {12, 18}, {12, 22}, {12, 24}, {12, 25}, {12, 27}, {12, 28}, 
            {12, 34}, {12, 35}, {12, 36}, {12, 37}, {12, 39},
            
            // 酯(13) 的不相容关系
            {13, 15}, {13, 18}, {13, 21}, {13, 22}, {13, 24}, {13, 25}, {13, 26}, 
            {13, 27}, {13, 28}, {13, 30}, {13, 34}, {13, 35}, {13, 36}, {13, 37}, 
            {13, 41},
            
            // 醚(14) 的不相容关系
            {14, 15}, {14, 22}, {14, 24}, {14, 25}, {14, 27}, {14, 28}, {14, 34}, 
            {14, 35}, {14, 36}, {14, 37}, {14, 39},
            
            // 氟化物、无机的(15) 的不相容关系
            {15, 21}, {15, 22}, {15, 24}, {15, 25}, {15, 26}, {15, 27}, {15, 28}, 
            {15, 30}, {15, 34}, {15, 35}, {15, 36}, {15, 37}, {15, 41},
            
            // 卤氢化合物、芳香族的(16) 的不相容关系
            {16, 21}, {16, 22}, {16, 24}, {16, 25}, {16, 26}, {16, 27}, {16, 28}, 
            {16, 30}, {16, 34}, {16, 35}, {16, 36}, {16, 37}, {16, 41},
            
            // 卤化有机物(17) 的不相容关系
            {17, 18}, {17, 21}, {17, 22}, {17, 24}, {17, 25}, {17, 26}, {17, 27}, 
            {17, 28}, {17, 30}, {17, 34}, {17, 35}, {17, 36}, {17, 37}, {17, 41},
            
            // 异氰酸盐(18) 的不相容关系
            {18, 20}, {18, 21}, {18, 22}, {18, 23}, {18, 24}, {18, 25}, {18, 26}, 
            {18, 27}, {18, 28}, {18, 30}, {18, 31}, {18, 32}, {18, 33}, {18, 34}, 
            {18, 35}, {18, 36}, {18, 37}, {18, 39}, {18, 41},
            
            // 酮(19) 的不相容关系
            {19, 21}, {19, 22}, {19, 24}, {19, 25}, {19, 27}, {19, 28}, {19, 30}, 
            {19, 34}, {19, 35}, {19, 36}, {19, 37}, {19, 39}, {19, 41},
            
            // 硫醇及其他有机硫化物(20) 的不相容关系
            {20, 21}, {20, 22}, {20, 24}, {20, 25}, {20, 27}, {20, 28}, {20, 34}, 
            {20, 35}, {20, 36}, {20, 37}, {20, 39},
            
            // 金属、碱及碱土、元素的(21) 的不相容关系
            {21, 22}, {21, 23}, {21, 24}, {21, 25}, {21, 26}, {21, 27}, {21, 28}, 
            {21, 31}, {21, 32}, {21, 33}, {21, 34}, {21, 35}, {21, 36}, {21, 37}, 
            {21, 39}, {21, 40}, {21, 41},
            
            // 氧化剂、强氧化剂(22) 的不相容关系
            {22, 23}, {22, 26}, {22, 30}, {22, 31}, {22, 32}, {22, 33}, {22, 38}, 
            {22, 40}, {22, 41},
            
            // 酚类及甲酚(23) 的不相容关系
            {23, 24}, {23, 25}, {23, 27}, {23, 28}, {23, 34}, {23, 35}, {23, 36}, 
            {23, 37}, {23, 39},
            
            // 有机过氧化物(24) 的不相容关系
            {24, 26}, {24, 30}, {24, 31}, {24, 32}, {24, 33}, {24, 38}, {24, 40}, 
            {24, 41},
            
            // 过氧化氢(25) 的不相容关系
            {25, 26}, {25, 30}, {25, 31}, {25, 32}, {25, 33}, {25, 38}, {25, 40}, 
            {25, 41},
            
            // 碱化物(26) 的不相容关系
            {26, 27}, {26, 28}, {26, 34}, {26, 35}, {26, 36}, {26, 37}, {26, 39}, 
            {26, 41},
            
            // 氯酸盐(27) 的不相容关系
            {27, 30}, {27, 31}, {27, 32}, {27, 33}, {27, 38}, {27, 40}, {27, 41},
            
            // 过氯酸盐及过氯酸(28) 的不相容关系
            {28, 30}, {28, 31}, {28, 32}, {28, 33}, {28, 38}, {28, 40}, {28, 41},
            
            // 锰化合物及氧化剂(29) 的不相容关系
            {29, 30}, {29, 31}, {29, 32}, {29, 33}, {29, 38}, {29, 40}, {29, 41},
            
            // 还原剂、强还原剂(30) 的不相容关系
            {30, 34}, {30, 35}, {30, 36}, {30, 37}, {30, 39},
            
            // 硫化物及无机物(31) 的不相容关系
            {31, 34}, {31, 35}, {31, 36}, {31, 37}, {31, 39},
            
            // 亚硫酸盐(32) 的不相容关系
            {32, 34}, {32, 35}, {32, 36}, {32, 37}, {32, 39},
            
            // 硫代硫酸盐(33) 的不相容关系
            {33, 34}, {33, 35}, {33, 36}, {33, 37}, {33, 39},
            
            // 高氯酸(34) 的不相容关系
            {34, 38}, {34, 40}, {34, 41},
            
            // 过锰酸盐(35) 的不相容关系
            {35, 38}, {35, 40}, {35, 41},
            
            // 硝酸盐(36) 的不相容关系
            {36, 38}, {36, 40}, {36, 41},
            
            // 亚硝酸盐(37) 的不相容关系
            {37, 38}, {37, 40}, {37, 41},
            
            // 氯化物、强剧毒(38) 的不相容关系
            {38, 39}, {38, 41},
            
            // 硝基物、硝酸酯(39) 的不相容关系
            {39, 40}, {39, 41},
            
            // 无氯化合物(40) 的不相容关系
            {40, 41}
        };
    }

    /**
     * 初始化不相容原因
     */
    private void initIncompatibilityReasons() {
        INCOMPATIBILITY_REASONS.put("ACID_BASE", "酸碱中和反应，会产生大量热量");
        INCOMPATIBILITY_REASONS.put("OXIDIZER_REDUCER", "氧化还原反应，可能引起燃烧或爆炸");
        INCOMPATIBILITY_REASONS.put("WATER_REACTIVE", "与水反应，产生有毒气体或热量");
        INCOMPATIBILITY_REASONS.put("TOXIC_GAS", "反应产生有毒气体");
        INCOMPATIBILITY_REASONS.put("FLAMMABLE", "反应产生易燃物质");
        INCOMPATIBILITY_REASONS.put("EXPLOSIVE", "可能发生爆炸反应");
        INCOMPATIBILITY_REASONS.put("HEAT_GENERATION", "放热反应，温度升高");
        INCOMPATIBILITY_REASONS.put("POLYMERIZATION", "可能发生聚合反应");
    }

    /**
     * 检查两个类别是否相容
     *
     * @param category1 类别1（1-41）
     * @param category2 类别2（1-41）
     * @return true-相容，false-不相容
     */
    public static boolean isCompatible(Integer category1, Integer category2) {
        if (!WasteCategoryEnum.isValidCode(category1) || !WasteCategoryEnum.isValidCode(category2)) {
            return false;
        }
        return COMPATIBILITY_MATRIX[category1][category2] == 0;
    }

    /**
     * 检查多个类别之间的相容性
     *
     * @param categories 类别列表
     * @return 相容性检查结果
     */
    public static CompatibilityCheckResult checkCompatibility(List<Integer> categories) {
        CompatibilityCheckResult result = new CompatibilityCheckResult();
        result.setCompatible(true);
        result.setIncompatiblePairs(new ArrayList<>());
        
        for (int i = 0; i < categories.size(); i++) {
            for (int j = i + 1; j < categories.size(); j++) {
                Integer cat1 = categories.get(i);
                Integer cat2 = categories.get(j);
                
                if (!isCompatible(cat1, cat2)) {
                    result.setCompatible(false);
                    IncompatiblePair pair = new IncompatiblePair();
                    pair.setCategory1(cat1);
                    pair.setCategory2(cat2);
                    pair.setCategory1Name(WasteCategoryEnum.getByCode(cat1).getDescription());
                    pair.setCategory2Name(WasteCategoryEnum.getByCode(cat2).getDescription());
                    pair.setReason(getIncompatibilityReason(cat1, cat2));
                    result.getIncompatiblePairs().add(pair);
                }
            }
        }
        
        return result;
    }

    /**
     * 获取不相容原因
     */
    private static String getIncompatibilityReason(Integer cat1, Integer cat2) {
        // 根据类别组合判断不相容原因（简化版本）
        if ((cat1 == 1 || cat1 == 3) && cat2 == 2) {
            return INCOMPATIBILITY_REASONS.get("ACID_BASE");
        }
        if ((cat1 == 22 || cat1 == 24 || cat1 == 25) && cat2 == 30) {
            return INCOMPATIBILITY_REASONS.get("OXIDIZER_REDUCER");
        }
        if (cat1 == 41 || cat2 == 41) {
            return INCOMPATIBILITY_REASONS.get("WATER_REACTIVE");
        }
        return "化学反应不相容，存在安全风险";
    }

    /**
     * 相容性检查结果
     */
    @Data
    public static class CompatibilityCheckResult {
        private Boolean compatible;
        private List<IncompatiblePair> incompatiblePairs;
    }

    /**
     * 不相容配对
     */
    @Data
    public static class IncompatiblePair {
        private Integer category1;
        private Integer category2;
        private String category1Name;
        private String category2Name;
        private String reason;
    }
} 