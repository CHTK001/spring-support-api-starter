/**
 * Guacamole客户端工具类
 * 基于guacamole-common-js库的封装
 */

// Guacamole WebSocket连接类
export class GuacamoleWebSocket {
  private socket: WebSocket | null = null
  private url: string
  private onopen?: () => void
  private onclose?: (event: CloseEvent) => void
  private onerror?: (event: Event) => void
  private oninstruction?: (opcode: string, args: string[]) => void

  constructor(url: string) {
    this.url = url
  }

  connect() {
    this.socket = new WebSocket(this.url)
    
    this.socket.onopen = () => {
      this.onopen?.()
    }

    this.socket.onclose = (event) => {
      this.onclose?.(event)
    }

    this.socket.onerror = (event) => {
      this.onerror?.(event)
    }

    this.socket.onmessage = (event) => {
      const instruction = this.parseInstruction(event.data)
      if (instruction) {
        this.oninstruction?.(instruction.opcode, instruction.args)
      }
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
  }

  sendInstruction(opcode: string, ...args: string[]) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      const instruction = this.formatInstruction(opcode, args)
      this.socket.send(instruction)
    }
  }

  private parseInstruction(data: string): { opcode: string; args: string[] } | null {
    const parts = data.split(',')
    if (parts.length < 1) return null
    
    const opcode = parts[0]
    const args = parts.slice(1).map(arg => arg.replace(/;$/, ''))
    
    return { opcode, args }
  }

  private formatInstruction(opcode: string, args: string[]): string {
    return `${opcode},${args.join(',')};`
  }

  setOnOpen(callback: () => void) {
    this.onopen = callback
  }

  setOnClose(callback: (event: CloseEvent) => void) {
    this.onclose = callback
  }

  setOnError(callback: (event: Event) => void) {
    this.onerror = callback
  }

  setOnInstruction(callback: (opcode: string, args: string[]) => void) {
    this.oninstruction = callback
  }
}

// Guacamole客户端类
export class GuacamoleClient {
  private socket: GuacamoleWebSocket
  private display: HTMLCanvasElement | null = null
  private context: CanvasRenderingContext2D | null = null
  private mouse: GuacamoleMouse | null = null
  private keyboard: GuacamoleKeyboard | null = null

  constructor(socket: GuacamoleWebSocket) {
    this.socket = socket
    this.setupSocketHandlers()
  }

  private setupSocketHandlers() {
    this.socket.setOnInstruction((opcode, args) => {
      this.handleInstruction(opcode, args)
    })
  }

  private handleInstruction(opcode: string, args: string[]) {
    switch (opcode) {
      case 'size':
        this.handleSize(args)
        break
      case 'png':
        this.handlePng(args)
        break
      case 'copy':
        this.handleCopy(args)
        break
      case 'cursor':
        this.handleCursor(args)
        break
      default:
        console.log('Unhandled instruction:', opcode, args)
    }
  }

  private handleSize(args: string[]) {
    const width = parseInt(args[1])
    const height = parseInt(args[2])
    
    if (this.display) {
      this.display.width = width
      this.display.height = height
    }
  }

  private handlePng(args: string[]) {
    // 处理PNG图像数据
    const layer = parseInt(args[0])
    const x = parseInt(args[1])
    const y = parseInt(args[2])
    const data = args[3]
    
    if (this.context && data) {
      const img = new Image()
      img.onload = () => {
        this.context!.drawImage(img, x, y)
      }
      img.src = 'data:image/png;base64,' + data
    }
  }

  private handleCopy(args: string[]) {
    // 处理复制操作
    const srcLayer = parseInt(args[0])
    const srcX = parseInt(args[1])
    const srcY = parseInt(args[2])
    const srcWidth = parseInt(args[3])
    const srcHeight = parseInt(args[4])
    const dstLayer = parseInt(args[5])
    const dstX = parseInt(args[6])
    const dstY = parseInt(args[7])
    
    if (this.context) {
      const imageData = this.context.getImageData(srcX, srcY, srcWidth, srcHeight)
      this.context.putImageData(imageData, dstX, dstY)
    }
  }

  private handleCursor(args: string[]) {
    // 处理光标变化
    const x = parseInt(args[0])
    const y = parseInt(args[1])
    const data = args[2]
    
    if (this.display && data) {
      this.display.style.cursor = `url(data:image/png;base64,${data}) ${x} ${y}, auto`
    }
  }

  attachTo(canvas: HTMLCanvasElement) {
    this.display = canvas
    this.context = canvas.getContext('2d')
    
    // 初始化鼠标和键盘处理
    this.mouse = new GuacamoleMouse(canvas, this.socket)
    this.keyboard = new GuacamoleKeyboard(this.socket)
  }

  connect() {
    this.socket.connect()
  }

  disconnect() {
    this.socket.disconnect()
    this.mouse?.destroy()
    this.keyboard?.destroy()
  }

  sendSize(width: number, height: number) {
    this.socket.sendInstruction('size', width.toString(), height.toString())
  }
}

// Guacamole鼠标处理类
export class GuacamoleMouse {
  private canvas: HTMLCanvasElement
  private socket: GuacamoleWebSocket
  private mouseState = {
    left: false,
    middle: false,
    right: false
  }

  constructor(canvas: HTMLCanvasElement, socket: GuacamoleWebSocket) {
    this.canvas = canvas
    this.socket = socket
    this.setupEventListeners()
  }

  private setupEventListeners() {
    this.canvas.addEventListener('mousemove', this.handleMouseMove.bind(this))
    this.canvas.addEventListener('mousedown', this.handleMouseDown.bind(this))
    this.canvas.addEventListener('mouseup', this.handleMouseUp.bind(this))
    this.canvas.addEventListener('contextmenu', (e) => e.preventDefault())
  }

  private handleMouseMove(event: MouseEvent) {
    const rect = this.canvas.getBoundingClientRect()
    const x = Math.floor(event.clientX - rect.left)
    const y = Math.floor(event.clientY - rect.top)
    
    this.socket.sendInstruction('mouse', x.toString(), y.toString())
  }

  private handleMouseDown(event: MouseEvent) {
    const button = this.getMouseButton(event.button)
    if (button) {
      this.mouseState[button] = true
      this.sendMouseState()
    }
  }

  private handleMouseUp(event: MouseEvent) {
    const button = this.getMouseButton(event.button)
    if (button) {
      this.mouseState[button] = false
      this.sendMouseState()
    }
  }

  private getMouseButton(button: number): keyof typeof this.mouseState | null {
    switch (button) {
      case 0: return 'left'
      case 1: return 'middle'
      case 2: return 'right'
      default: return null
    }
  }

  private sendMouseState() {
    const state = (this.mouseState.left ? 1 : 0) |
                  (this.mouseState.middle ? 2 : 0) |
                  (this.mouseState.right ? 4 : 0)
    
    this.socket.sendInstruction('mouse', '0', '0', state.toString())
  }

  destroy() {
    this.canvas.removeEventListener('mousemove', this.handleMouseMove)
    this.canvas.removeEventListener('mousedown', this.handleMouseDown)
    this.canvas.removeEventListener('mouseup', this.handleMouseUp)
  }
}

// Guacamole键盘处理类
export class GuacamoleKeyboard {
  private socket: GuacamoleWebSocket
  private pressedKeys = new Set<number>()

  constructor(socket: GuacamoleWebSocket) {
    this.socket = socket
    this.setupEventListeners()
  }

  private setupEventListeners() {
    document.addEventListener('keydown', this.handleKeyDown.bind(this))
    document.addEventListener('keyup', this.handleKeyUp.bind(this))
  }

  private handleKeyDown(event: KeyboardEvent) {
    const keyCode = event.keyCode || event.which
    
    if (!this.pressedKeys.has(keyCode)) {
      this.pressedKeys.add(keyCode)
      this.socket.sendInstruction('key', keyCode.toString(), '1')
    }
    
    event.preventDefault()
  }

  private handleKeyUp(event: KeyboardEvent) {
    const keyCode = event.keyCode || event.which
    
    if (this.pressedKeys.has(keyCode)) {
      this.pressedKeys.delete(keyCode)
      this.socket.sendInstruction('key', keyCode.toString(), '0')
    }
    
    event.preventDefault()
  }

  destroy() {
    document.removeEventListener('keydown', this.handleKeyDown)
    document.removeEventListener('keyup', this.handleKeyUp)
  }
}
