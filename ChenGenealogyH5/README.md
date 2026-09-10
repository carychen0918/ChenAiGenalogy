# 陈氏族谱移动端（ChenGenealogyH5）

基于 **uni-app + Vue3 + TypeScript + Vite**，与 `ChenGenealogyApi`、`ChenGenealogyUI` 同级。一套代码同时支持：

- H5 移动端
- 微信公众号（H5 在微信内打开，`terminal=11`）
- 微信小程序（`terminal=10`）

接口遵循芋道 App 规范：请求 `controller.app` 下的接口，统一前缀 **`/app-api`**，携带 `Authorization: Bearer`、`terminal`，解包 `CommonResult`（`code === 0`）。

## 本地运行

先启动后端 `ChenGenealogyApi`（默认 `http://localhost:48080`），再在本目录：

```bash
npm install
npm run dev:h5
```

H5 开发地址：http://localhost:5173 ，已代理 `/app-api` 到 `.env.development` 中的 `VITE_APP_BASE_URL`。

微信小程序：

```bash
npm run dev:mp-weixin
```

用微信开发者工具打开 `dist/dev/mp-weixin`。请在 `src/manifest.json` 的 `mp-weixin.appid` 填入小程序 AppID。开发阶段已关闭 `urlCheck`。

生产构建：

```bash
npm run build:h5
npm run build:mp-weixin
```

公众号请将 `build:h5` 产物部署到已备案 HTTPS 域名，并在公众号后台配置 JS 接口安全域名。

## 环境变量

| 变量 | 说明 |
| --- | --- |
| `VITE_APP_BASE_URL` | 后端根地址，不含 `/app-api` |
| `VITE_APP_API_PREFIX` | 固定 `/app-api` |
| `VITE_APP_TENANT_ENABLE` | 与后端一致，当前为 `false` |
| `VITE_APP_CAPTCHA_ENABLE` | 本地验证码关闭时为 `false` |

## 登录与权限

- 登录：`POST /app-api/system/auth/login`（`controller.app.auth.AppAuthController`）
- 业务：`/app-api/genealogy/**`（`controller.app` 下各 App 控制器）
- 文件：`POST /app-api/infra/file/upload`
- 游客可访问：首页、寻根、AI 寻宗、迁徙、字辈、祖先事迹
- 需登录：族谱、成员档案、祭祖、资助、活动报名、发布动态

AI 寻宗在小程序/H5 使用非流式接口 `POST /app-api/genealogy/ai-match/chat`。管理端门户仍走 `/admin-api` 的 SSE `chat-stream`。游客每天限 10 次。
