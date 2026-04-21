# API配置说明

## 概述
此配置文件统一管理所有API的基础地址，方便在开发和生产环境之间切换。

## 配置文件位置
- `config/api-config.js` - 主配置文件

## 使用方法

### 1. 切换到服务器环境
只需要修改 `config/api-config.js` 文件中的 `BASE_URL` 变量：

```javascript
// 开发环境
const BASE_URL = 'http://localhost:8080';

// 生产环境（将localhost:8080替换为您的服务器地址）
const BASE_URL = 'https://your-server-domain.com';
// 或者
const BASE_URL = 'http://your-server-ip:port';
```

### 2. 支持的API端点
- 配伍匹配API: `/api/matching`
- 危废目录API: `/api/v1/waste-directory`
- 热力性质API: `/api/thermal-properties`
- 物理性质API: `/api/physical-properties`
- 静态图片资源: `/static/thermal-images`

### 3. 已统一的文件
以下文件已经统一使用配置文件：
- `utils/matching-api.js`
- `utils/thermal-api.js`
- `pages/waste/list/list.js`
- `pages/waste/detail/detail.js`
- `pages/physical-property/detail/index.js`
- `pages/thermal-properties/spectrum/spectrum.js`
- `pages/thermal-properties/thermal/thermal.js`

## 注意事项
- 只需要修改 `BASE_URL` 一个变量即可完成环境切换
- 确保服务器地址包含协议（http://或https://）
- 如果服务器使用非标准端口，请包含端口号 