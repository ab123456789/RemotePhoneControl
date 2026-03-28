# Architecture

## 角色
### 1. Agent App（被控端）
运行在有 root 的 Android 手机上：
- 采集屏幕截图 / 缩放 / 质量调节
- 注入输入事件（tap / swipe / keyevent / text）
- 暴露 HTTP / WebSocket 控制接口
- 提供设备信息、屏幕尺寸、网络地址、访问码

### 2. Controller App（控制端）
运行在无 root 的 Android 手机上：
- 连接被控端
- 显示远程画面
- 手势映射为远程操作
- 画质 / 刷新间隔控制
- 会话状态、连接状态、历史设备列表

## 网络
- 手动输入 IPv4 / IPv6 地址
- 支持局域网直连
- 认证方式先用一次性访问码
- 后续可升级成配对密钥

## 首版功能
- 设备连接
- 截图拉流（轮询）
- 点击 / 滑动 / Home / Back / Recent / Power / 音量
- 文本输入
- 画质档位：低 / 中 / 高
- 刷新频率：静态刷新 / 自动刷新

## 后续增强
- WebSocket 增量帧
- H.264/MJPEG
- 剪贴板同步
- 文件传输
- 音频转发
