# 危废热处理配伍算法说明文档

## 文档说明

本文档为危废热处理智库系统中使用的配伍算法

---

## 目录结构

```
算法文档/
├── README.md                          # 本文档
└── src/                              # 算法源代码
    ├── LinearProgrammingUtil.java      # 核心算法工具类
    ├── MatchingService.java            # 配伍算法服务接口
    ├── BlendingConstraints.java        # 约束参数DTO
    ├── BlendingResult.java             # 计算结果DTO
    └── WasteBlendingData.java          # 危废数据DTO
```

### 代码文件说明

| 文件 | 说明 |
|------|------|
| `LinearProgrammingUtil.java` | 线性规划算法核心实现，包含求解器、约束检查、混合指标计算 |
| `MatchingService.java` | 配伍算法服务接口定义 |
| `BlendingConstraints.java` | 配伍约束参数数据结构 |
| `BlendingResult.java` | 配伍计算结果数据结构 |
| `WasteBlendingData.java` | 危废输入数据结构 |

---

