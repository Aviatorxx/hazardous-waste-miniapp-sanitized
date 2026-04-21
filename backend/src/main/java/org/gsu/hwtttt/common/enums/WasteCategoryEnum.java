package org.gsu.hwtttt.common.enums;

import lombok.Getter;

/**
 * 危废类别枚举（41类）
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
public enum WasteCategoryEnum {

    ACID_MINERAL(1, "ACID_MINERAL", "酸类、矿物、亚氧化物"),
    BASE_MINERAL(2, "BASE_MINERAL", "碱类、矿物、氧化物"),
    ACID_ORGANIC(3, "ACID_ORGANIC", "酸类、有机的"),
    ALCOHOL(4, "ALCOHOL", "醇类及二醇"),
    ALDEHYDE(5, "ALDEHYDE", "醛"),
    AMIDE(6, "AMIDE", "酰胺或酰化物"),
    AMINE(7, "AMINE", "胺类、脂肪族的及芳香族的"),
    AZO_COMPOUND(8, "AZO_COMPOUND", "偶氮化合物、重氮化合物及肼"),
    CARBAMATE(9, "CARBAMATE", "氨基甲酸酯"),
    OLEFIN(10, "OLEFIN", "烯烃"),
    CYANIDE(11, "CYANIDE", "氰化物"),
    DITHIOCARBAMATE(12, "DITHIOCARBAMATE", "二硫代氨基甲酸酯"),
    ESTER(13, "ESTER", "酯"),
    ETHER(14, "ETHER", "醚"),
    FLUORIDE(15, "FLUORIDE", "氟化物、无机的"),
    HALOGENATED_AROMATIC(16, "HALOGENATED_AROMATIC", "卤氢化合物、芳香族的"),
    HALOGENATED_ORGANIC(17, "HALOGENATED_ORGANIC", "卤化有机物"),
    ISOCYANATE(18, "ISOCYANATE", "异氰酸盐"),
    KETONE(19, "KETONE", "酮"),
    MERCAPTAN(20, "MERCAPTAN", "硫醇及其他有机硫化物"),
    METAL_ALKALI(21, "METAL_ALKALI", "金属、碱及碱土、元素的"),
    OXIDIZER_STRONG(22, "OXIDIZER_STRONG", "氧化剂、强氧化剂"),
    PHENOL(23, "PHENOL", "酚类及甲酚"),
    ORGANIC_PEROXIDE(24, "ORGANIC_PEROXIDE", "有机过氧化物"),
    HYDROGEN_PEROXIDE(25, "HYDROGEN_PEROXIDE", "过氧化氢"),
    ALKALIDE(26, "ALKALIDE", "碱化物"),
    CHLORATE(27, "CHLORATE", "氯酸盐"),
    PERCHLORATE(28, "PERCHLORATE", "过氯酸盐及过氯酸"),
    MANGANESE_COMPOUND(29, "MANGANESE_COMPOUND", "锰化合物及氧化剂"),
    REDUCER_STRONG(30, "REDUCER_STRONG", "还原剂、强还原剂"),
    SULFIDE(31, "SULFIDE", "硫化物及无机物"),
    SULFITE(32, "SULFITE", "亚硫酸盐"),
    THIOSULFATE(33, "THIOSULFATE", "硫代硫酸盐"),
    PERCHLORIC_ACID(34, "PERCHLORIC_ACID", "高氯酸"),
    PERMANGANATE(35, "PERMANGANATE", "过锰酸盐"),
    NITRATE(36, "NITRATE", "硝酸盐"),
    NITRITE(37, "NITRITE", "亚硝酸盐"),
    CHLORIDE_TOXIC(38, "CHLORIDE_TOXIC", "氯化物、强剧毒"),
    NITRO_COMPOUND(39, "NITRO_COMPOUND", "硝基物、硝酸酯"),
    NON_CHLORINE(40, "NON_CHLORINE", "无氯化合物"),
    WATER_REACTIVE(41, "WATER_REACTIVE", "含水及反应物质");

    private final Integer code;
    private final String categoryCode;
    private final String description;

    WasteCategoryEnum(Integer code, String categoryCode, String description) {
        this.code = code;
        this.categoryCode = categoryCode;
        this.description = description;
    }

    /**
     * 根据编号获取类别
     */
    public static WasteCategoryEnum getByCode(Integer code) {
        for (WasteCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 根据类别代码获取类别
     */
    public static WasteCategoryEnum getByCategoryCode(String categoryCode) {
        for (WasteCategoryEnum category : values()) {
            if (category.getCategoryCode().equals(categoryCode)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 验证类别编号是否有效
     */
    public static boolean isValidCode(Integer code) {
        return code != null && code >= 1 && code <= 41;
    }
} 