/**
 * Guacamole Common JS 类型声明
 */

declare module 'guacamole-common-js' {
  export interface GuacamoleClient {
    new (tunnel: any): GuacamoleClient;
    connect(): void;
    disconnect(): void;
    getDisplay(): any;
    sendMouseState(mouseState: any): void;
    sendKeyEvent(pressed: number, keysym: number): void;
    createClipboardStream(mimetype: string): any;
    createFileStream(mimetype: string, filename: string): any;
    onstatechange: ((state: number) => void) | null;
    onerror: ((error: any) => void) | null;
    onname: ((name: string) => void) | null;
    onclipboard: ((stream: any, mimetype: string) => void) | null;
    onfile: ((stream: any, mimetype: string, filename: string) => void) | null;
    onpipe: ((stream: any, mimetype: string, name: string) => void) | null;
    onsync: ((timestamp: number) => void) | null;
  }

  export interface GuacamoleWebSocketTunnel {
    new (url: string): GuacamoleWebSocketTunnel;
  }

  export interface GuacamoleMouse {
    new (element: HTMLElement): GuacamoleMouse;
    onmousedown: ((mouseState: any) => void) | null;
    onmouseup: ((mouseState: any) => void) | null;
    onmousemove: ((mouseState: any) => void) | null;
  }

  export interface GuacamoleKeyboard {
    new (element: HTMLElement): GuacamoleKeyboard;
    onkeydown: ((keysym: number) => void) | null;
    onkeyup: ((keysym: number) => void) | null;
  }

  export interface GuacamoleStringWriter {
    new (stream: any): GuacamoleStringWriter;
    sendText(text: string): void;
    sendEnd(): void;
  }

  export interface GuacamoleArrayBufferWriter {
    new (stream: any): GuacamoleArrayBufferWriter;
    sendData(data: ArrayBuffer): void;
    sendEnd(): void;
  }

  export const Client: {
    new (tunnel: any): GuacamoleClient;
    IDLE: number;
    CONNECTING: number;
    WAITING: number;
    CONNECTED: number;
    DISCONNECTING: number;
    DISCONNECTED: number;
  };

  export const WebSocketTunnel: {
    new (url: string): GuacamoleWebSocketTunnel;
  };

  export const Mouse: {
    new (element: HTMLElement): GuacamoleMouse;
  };

  export const Keyboard: {
    new (element: HTMLElement): GuacamoleKeyboard;
  };

  export const StringWriter: {
    new (stream: any): GuacamoleStringWriter;
  };

  export const ArrayBufferWriter: {
    new (stream: any): GuacamoleArrayBufferWriter;
  };

  const Guacamole: {
    Client: typeof Client;
    WebSocketTunnel: typeof WebSocketTunnel;
    Mouse: typeof Mouse;
    Keyboard: typeof Keyboard;
    StringWriter: typeof StringWriter;
    ArrayBufferWriter: typeof ArrayBufferWriter;
  };

  export default Guacamole;
}
