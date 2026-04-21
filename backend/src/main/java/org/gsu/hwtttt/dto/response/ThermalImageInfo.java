package org.gsu.hwtttt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 热力学图像信息响应DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ThermalImageInfo", description = "热力学图像信息响应")
public class ThermalImageInfo {

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("光谱类型")
    private String spectrumType;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("测试名称")
    private String testName;

    @ApiModelProperty("测试日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;

    @ApiModelProperty("测试实验室")
    private String testLab;

    @ApiModelProperty("数据质量等级")
    private String qualityGrade;

    @ApiModelProperty("图像访问URL")
    private String imageUrl;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("备注")
    private String remark;
} 