/**
 * 基于GuacamoleWebSocketTunnelEndpoint的客户端
 * 使用标准的Guacamole WebSocket隧道模式
 */

import type { GuacamoleConnectionConfig } from './types';

declare global {
  interface Window {
    Guacamole: any;
  }
}

export interface TunnelClientConfig extends GuacamoleConnectionConfig {
  /** WebSocket端点URL */
  tunnelUrl?: string;
}

export interface TunnelClientCallbacks {
  onStateChange?: (state: number) => void;
  onError?: (error: any) => void;
  onName?: (name: string) => void;
  onClipboard?: (stream: any, mimetype: string) => void;
  onFile?: (stream: any, mimetype: string, filename: string) => void;
}

/**
 * Guacamole隧道客户端
 * 直接使用GuacamoleWebSocketTunnelEndpoint
 */
export class GuacamoleTunnelClient {
  private config: TunnelClientConfig;
  private callbacks: TunnelClientCallbacks;
  private tunnel: any = null;
  private client: any = null;
  private display: any = null;
  private mouse: any = null;
  private keyboard: any = null;
  private isDestroyed = false;
  private isConnecting = false;

  constructor(config: TunnelClientConfig, callbacks: TunnelClientCallbacks = {}) {
    this.config = config;
    this.callbacks = callbacks;
  }

  /**
   * 连接到服务器
   */
  async connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        // 如果已经销毁，不允许连接
        if (this.isDestroyed) {
          reject(new Error('客户端已销毁'));
          return;
        }

        // 防止重复连接
        if (this.isConnecting) {
          reject(new Error('正在连接中，请勿重复连接'));
          return;
        }

        // 如果已经连接，直接返回成功
        if (this.client && this.client.currentState === window.Guacamole.Client.CONNECTED) {
          resolve();
          return;
        }

        this.isConnecting = true;

        // 构建 WebSocket URL
        const wsUrl = this.buildWebSocketUrl();
        console.log('开始连接 Guacamole 隧道:', wsUrl);

        // 创建 Guacamole WebSocket 隧道
        this.tunnel = new window.Guacamole.WebSocketTunnel(wsUrl);

        // 添加隧道事件监听
        this.tunnel.onerror = (error: any) => {
          console.error('WebSocket 隧道错误:', error);
          this.isConnecting = false;
          if (this.callbacks.onError) {
            this.callbacks.onError(error);
          }
          reject(error);
        };

        // 创建 Guacamole 客户端
        this.client = new window.Guacamole.Client(this.tunnel);

        // 获取显示对象
        this.display = this.client.getDisplay();

        // 设置客户端事件监听器
        this.setupClientEventHandlers(resolve, reject);

        // 连接客户端
        this.client.connect();

      } catch (error) {
        this.isConnecting = false;
        console.error('连接失败:', error);
        reject(error);
      }
    });
  }

  /**
   * 设置客户端事件处理器
   */
  private setupClientEventHandlers(resolve: () => void, reject: (error: any) => void) {
    // 状态变化监听
    this.client.onstatechange = (state: number) => {
      console.log('Guacamole 客户端状态变化:', state);
      
      if (this.callbacks.onStateChange) {
        this.callbacks.onStateChange(state);
      }

      switch (state) {
        case window.Guacamole.Client.CONNECTED:
          this.isConnecting = false;
          console.log('Guacamole 连接成功');
          resolve();
          break;
          
        case window.Guacamole.Client.DISCONNECTED:
          this.isConnecting = false;
          console.log('Guacamole 连接断开');
          break;
          
        case window.Guacamole.Client.CONNECTING:
          console.log('Guacamole 正在连接...');
          break;
          
        case window.Guacamole.Client.WAITING:
          console.log('Guacamole 等待中...');
          break;
          
        case window.Guacamole.Client.DISCONNECTING:
          console.log('Guacamole 正在断开连接...');
          break;
      }
    };

    // 错误监听
    this.client.onerror = (error: any) => {
      this.isConnecting = false;
      console.error('Guacamole 客户端错误:', error);
      
      if (this.callbacks.onError) {
        this.callbacks.onError(error);
      }
      
      reject(error);
    };

    // 名称监听
    this.client.onname = (name: string) => {
      console.log('Guacamole 会话名称:', name);
      if (this.callbacks.onName) {
        this.callbacks.onName(name);
      }
    };

    // 剪贴板监听
    this.client.onclipboard = (stream: any, mimetype: string) => {
      console.log('收到剪贴板数据:', mimetype);
      if (this.callbacks.onClipboard) {
        this.callbacks.onClipboard(stream, mimetype);
      }
    };

    // 文件监听
    this.client.onfile = (stream: any, mimetype: string, filename: string) => {
      console.log('收到文件:', filename, mimetype);
      if (this.callbacks.onFile) {
        this.callbacks.onFile(stream, mimetype, filename);
      }
    };
  }

  /**
   * 构建WebSocket URL
   */
  private buildWebSocketUrl(): string {
    // 使用配置的隧道URL或默认URL
    const baseUrl = this.config.tunnelUrl || '/websocket/guacamole';
    
    // 构建查询参数
    const params = new URLSearchParams({
      serverId: this.config.serverId.toString(),
      protocol: this.config.protocol
    });

    // 添加协议特定参数
    if (this.config.protocol === 'rdp') {
      if (this.config.width) params.set('width', this.config.width.toString());
      if (this.config.height) params.set('height', this.config.height.toString());
      if (this.config.colorDepth) params.set('color-depth', this.config.colorDepth.toString());
      if (this.config.security) params.set('security', this.config.security);
      if (this.config.ignoreCert !== undefined) params.set('ignore-cert', this.config.ignoreCert.toString());
      if (this.config.serverLayout) params.set('server-layout', this.config.serverLayout);
    } else if (this.config.protocol === 'vnc') {
      if (this.config.colorDepth) params.set('color-depth', this.config.colorDepth.toString());
      if (this.config.swapRedBlue !== undefined) params.set('swap-red-blue', this.config.swapRedBlue.toString());
      if (this.config.cursor) params.set('cursor', this.config.cursor);
      if (this.config.readOnly !== undefined) params.set('read-only', this.config.readOnly.toString());
    }

    // 构建完整URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    return `${protocol}//${host}${baseUrl}?${params.toString()}`;
  }

  /**
   * 绑定到DOM元素
   */
  attachTo(container: HTMLElement): void {
    if (!this.display) {
      throw new Error('客户端未连接，无法绑定显示');
    }

    // 清空容器并添加显示元素
    container.innerHTML = '';
    container.appendChild(this.display.getElement());

    // 创建鼠标和键盘处理器
    this.mouse = new window.Guacamole.Mouse(this.display.getElement());
    this.keyboard = new window.Guacamole.Keyboard(document);

    // 设置鼠标事件
    this.mouse.onmousedown = 
    this.mouse.onmouseup = 
    this.mouse.onmousemove = (mouseState: any) => {
      if (this.client) {
        this.client.sendMouseState(mouseState);
      }
    };

    // 设置键盘事件
    this.keyboard.onkeydown = (keysym: number) => {
      if (this.client) {
        this.client.sendKeyEvent(1, keysym);
      }
    };

    this.keyboard.onkeyup = (keysym: number) => {
      if (this.client) {
        this.client.sendKeyEvent(0, keysym);
      }
    };

    console.log('Guacamole 客户端已绑定到容器');
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    try {
      if (this.client) {
        this.client.disconnect();
      }
      
      if (this.tunnel) {
        this.tunnel.disconnect();
      }
      
      console.log('Guacamole 连接已断开');
    } catch (error) {
      console.error('断开连接时出错:', error);
    }
  }

  /**
   * 销毁客户端
   */
  destroy(): void {
    this.isDestroyed = true;
    this.disconnect();
    
    // 清理资源
    this.client = null;
    this.tunnel = null;
    this.display = null;
    this.mouse = null;
    this.keyboard = null;
    
    console.log('Guacamole 客户端已销毁');
  }

  /**
   * 获取当前状态
   */
  getCurrentState(): number | null {
    return this.client ? this.client.currentState : null;
  }

  /**
   * 检查是否已连接
   */
  isConnected(): boolean {
    return this.client && this.client.currentState === window.Guacamole.Client.CONNECTED;
  }

  /**
   * 获取显示对象
   */
  getDisplay(): any {
    return this.display;
  }

  /**
   * 发送剪贴板数据
   */
  sendClipboard(data: string, mimetype: string = 'text/plain'): void {
    if (this.client && this.isConnected()) {
      const stream = this.client.createClipboardStream(mimetype);
      const writer = new window.Guacamole.StringWriter(stream);
      writer.sendText(data);
      writer.sendEnd();
    }
  }

  /**
   * 截图
   */
  takeScreenshot(): string | null {
    if (this.display) {
      const canvas = this.display.getElement();
      return canvas.toDataURL('image/png');
    }
    return null;
  }
}
