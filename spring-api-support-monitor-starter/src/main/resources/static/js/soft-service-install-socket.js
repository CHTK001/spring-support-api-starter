/**
 * 软件安装WebSocket客户端示例
 */
class SoftServiceInstallSocket {
    /**
     * 构造函数
     * @param {string} socketUrl WebSocket服务器URL，例如：ws://localhost:8080/socket.io
     * @param {function} onProgressCallback 进度回调函数
     * @param {function} onStatusCallback 状态回调函数
     * @param {function} onErrorCallback 错误回调函数
     */
    constructor(socketUrl, onProgressCallback, onStatusCallback, onErrorCallback) {
        this.socketUrl = socketUrl;
        this.socket = null;
        this.connected = false;
        this.onProgressCallback = onProgressCallback || function() {};
        this.onStatusCallback = onStatusCallback || function() {};
        this.onErrorCallback = onErrorCallback || function() {};
        this.monitoringInstalls = new Set();
    }

    /**
     * 连接到WebSocket服务器
     * @returns {Promise} 连接Promise
     */
    connect() {
        return new Promise((resolve, reject) => {
            try {
                // 使用socket.io客户端连接
                this.socket = io(this.socketUrl, {
                    transports: ['websocket'],
                    upgrade: false
                });

                // 连接事件
                this.socket.on('connect', () => {
                    console.log('WebSocket连接成功');
                    this.connected = true;
                    resolve();
                });

                // 连接错误事件
                this.socket.on('connect_error', (error) => {
                    console.error('WebSocket连接失败:', error);
                    this.connected = false;
                    reject(error);
                });

                // 断开连接事件
                this.socket.on('disconnect', () => {
                    console.log('WebSocket连接断开');
                    this.connected = false;
                });

                // 监听安装进度事件
                this.socket.on('/topic/install', (data) => {
                    console.log('收到安装进度更新:', data);
                    if (typeof this.onProgressCallback === 'function') {
                        this.onProgressCallback(data);
                    }
                });

                // 监听特定安装ID的状态
                this.socket.on('install_status', (data) => {
                    console.log('收到安装状态:', data);
                    if (typeof data === 'string') {
                        data = JSON.parse(data);
                    }
                    if (typeof this.onStatusCallback === 'function') {
                        this.onStatusCallback(data);
                    }
                });

                // 监听安装停止事件
                this.socket.on('install_stopped', (data) => {
                    console.log('安装已停止:', data);
                    if (typeof data === 'string') {
                        data = JSON.parse(data);
                    }
                    if (typeof this.onStatusCallback === 'function') {
                        this.onStatusCallback({
                            ...data,
                            installStatus: 3, // 安装失败状态
                            message: '安装被手动停止'
                        });
                    }
                });

                // 监听错误事件
                this.socket.on('error', (data) => {
                    console.error('收到错误:', data);
                    if (typeof data === 'string') {
                        data = JSON.parse(data);
                    }
                    if (typeof this.onErrorCallback === 'function') {
                        this.onErrorCallback(data);
                    }
                });

            } catch (error) {
                console.error('初始化WebSocket失败:', error);
                reject(error);
            }
        });
    }

    /**
     * 断开WebSocket连接
     */
    disconnect() {
        if (this.socket && this.connected) {
            this.socket.disconnect();
            this.connected = false;
        }
    }

    /**
     * 开始监控特定安装ID的安装进度
     * @param {number} installId 安装ID
     */
    monitorInstall(installId) {
        if (!this.connected || !this.socket) {
            throw new Error('WebSocket未连接');
        }

        this.socket.emit('monitor_install', { installId });
        this.monitoringInstalls.add(installId);
        console.log(`开始监控安装ID: ${installId}`);
    }

    /**
     * 停止监控特定安装ID的安装进度
     * @param {number} installId 安装ID
     */
    unmonitorInstall(installId) {
        if (!this.connected || !this.socket) {
            return;
        }

        this.socket.emit('unmonitor_install', { installId });
        this.monitoringInstalls.delete(installId);
        console.log(`停止监控安装ID: ${installId}`);
    }

    /**
     * 请求停止特定安装ID的安装过程
     * @param {number} installId 安装ID
     */
    stopInstall(installId) {
        if (!this.connected || !this.socket) {
            throw new Error('WebSocket未连接');
        }

        this.socket.emit('stop_install', { installId });
        console.log(`请求停止安装ID: ${installId}`);
    }
}

// 使用示例
/*
// 创建WebSocket客户端
const installSocket = new SoftServiceInstallSocket(
    'ws://localhost:8080/socket.io',
    // 进度回调
    (progress) => {
        console.log(`安装进度: ${progress.step}/${progress.total}, 消息: ${progress.msg}`);
        // 更新UI进度条
        const progressPercent = Math.round((progress.step / progress.total) * 100);
        document.getElementById('progressBar').style.width = progressPercent + '%';
        document.getElementById('progressText').innerText = progress.msg;
    },
    // 状态回调
    (status) => {
        console.log(`安装状态: ${status.installStatus}`);
        // 更新UI状态显示
        let statusText = '未知';
        switch(status.installStatus) {
            case 0: statusText = '未安装'; break;
            case 1: statusText = '安装中'; break;
            case 2: statusText = '安装成功'; break;
            case 3: statusText = '安装失败'; break;
        }
        document.getElementById('statusText').innerText = statusText;
    },
    // 错误回调
    (error) => {
        console.error('安装错误:', error);
        // 显示错误消息
        document.getElementById('errorText').innerText = error.message || '未知错误';
    }
);

// 连接WebSocket
installSocket.connect()
    .then(() => {
        // 开始监控安装ID为1的安装进度
        installSocket.monitorInstall(1);
    })
    .catch(error => {
        console.error('连接失败:', error);
    });

// 停止安装（例如通过按钮点击事件）
document.getElementById('stopButton').addEventListener('click', () => {
    installSocket.stopInstall(1);
});

// 页面卸载前断开连接
window.addEventListener('beforeunload', () => {
    installSocket.disconnect();
});
*/ 