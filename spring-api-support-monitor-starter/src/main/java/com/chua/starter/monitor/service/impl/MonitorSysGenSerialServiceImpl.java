package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenSerial;
import com.chua.starter.monitor.mapper.MonitorSysGenSerialMapper;
import com.chua.starter.monitor.service.MonitorSysGenSerialService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 串口配置服务实现类
 *
 * @author CH
 * @since 2024/7/23
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenSerialServiceImpl extends ServiceImpl<MonitorSysGenSerialMapper, MonitorSysGenSerial> implements MonitorSysGenSerialService {

    /**
     * Socket会话模板，用于推送消息
     */
    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 活跃的串口连接缓存 key: serialId, value: SerialPortConnection
     */
    private final Map<String, SerialPortConnection> activeSerialPorts = new ConcurrentHashMap<>();

    /**
     * 串口数据监听任务缓存 key: serialId, value: ScheduledFuture
     */
    private final Map<String, ScheduledFuture<?>> serialDataTasks = new ConcurrentHashMap<>();

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);

    /**
     * 串口连接包装类
     */
    private static class SerialPortConnection {
        private final String portName;
        private final Process process;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private volatile boolean connected = false;

        public SerialPortConnection(String portName, Process process) {
            this.portName = portName;
            this.process = process;
            this.inputStream = process.getInputStream();
            this.outputStream = process.getOutputStream();
            this.connected = true;
        }

        public boolean isConnected() {
            return connected && process.isAlive();
        }

        public void close() {
            connected = false;
            try {
                if (inputStream != null) { inputStream.close(); }
                if (outputStream != null) { outputStream.close(); }
                if (process != null) { process.destroyForcibly(); }
            } catch (IOException e) {
                log.warn("关闭串口连接时发生异常: {}", e.getMessage());
            }
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public String getPortName() {
            return portName;
        }
    }

    /**
     * 开启串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> start(String id) {
        // 检查ID是否为空
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        // 根据ID获取串口配置实例
        MonitorSysGenSerial monitorSysGenSerial = this.getById(id);

        // 检查串口配置实例是否存在
        if (null == monitorSysGenSerial) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        // 检查串口状态，若已开启，则返回错误信息
        if (null != monitorSysGenSerial.getMonitorSysGenSerialStatus() && 1 == monitorSysGenSerial.getMonitorSysGenSerialStatus()) {
            return ReturnResult.error("串口已开启");
        }

        try {
            // 检查串口是否已经开启
            String serialId = String.valueOf(monitorSysGenSerial.getMonitorSysGenSerialId());
            if (activeSerialPorts.containsKey(serialId)) {
                return ReturnResult.error("串口已开启");
            }

            // 开启串口连接
            SerialPortConnection connection = openSerialPort(monitorSysGenSerial);
            if (connection == null || !connection.isConnected()) {
                return ReturnResult.error("无法打开串口: " + monitorSysGenSerial.getMonitorSysGenSerialPort());
            }

            // 缓存连接
            activeSerialPorts.put(serialId, connection);

            // 启动数据监听任务
            startSerialDataListener(serialId, monitorSysGenSerial, connection);

            log.info("开启串口成功: {}", monitorSysGenSerial.getMonitorSysGenSerialName());

            // 更新状态为开启
            monitorSysGenSerial.setMonitorSysGenSerialStatus(1);
            boolean result = this.updateById(monitorSysGenSerial);

            // 发送串口状态变更消息
            sendSerialStatusMessage(monitorSysGenSerial, "start");

            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("开启串口失败: {}", e.getMessage(), e);
            // 发送错误消息
            sendSerialErrorMessage(monitorSysGenSerial, "start", e.getMessage());
            return ReturnResult.error("开启串口失败: " + e.getMessage());
        }
    }

    /**
     * 关闭串口
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> stop(String id) {
        // 检查ID是否为空
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        // 根据ID获取串口配置实例
        MonitorSysGenSerial monitorSysGenSerial = this.getById(id);

        // 检查串口配置实例是否存在
        if (null == monitorSysGenSerial) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        // 检查串口状态，若已停止，则返回错误信息
        if (null != monitorSysGenSerial.getMonitorSysGenSerialStatus() && 0 == monitorSysGenSerial.getMonitorSysGenSerialStatus()) {
            return ReturnResult.error("串口已关闭");
        }

        try {
            String serialId = String.valueOf(monitorSysGenSerial.getMonitorSysGenSerialId());

            // 停止数据监听任务
            stopSerialDataListener(serialId);

            // 关闭串口连接
            SerialPortConnection connection = activeSerialPorts.remove(serialId);
            if (connection != null) {
                connection.close();
            }

            log.info("关闭串口: {}", monitorSysGenSerial.getMonitorSysGenSerialName());

            // 更新状态为关闭
            monitorSysGenSerial.setMonitorSysGenSerialStatus(0);
            boolean result = this.updateById(monitorSysGenSerial);

            // 发送串口状态变更消息
            sendSerialStatusMessage(monitorSysGenSerial, "stop");

            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("关闭串口失败: {}", e.getMessage(), e);
            // 发送错误消息
            sendSerialErrorMessage(monitorSysGenSerial, "stop", e.getMessage());
            return ReturnResult.error("关闭串口失败: " + e.getMessage());
        }
    }

    /**
     * 测试串口连接
     *
     * @param id 串口配置ID
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> test(String id) {
        // 检查ID是否为空
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        // 根据ID获取串口配置实例
        MonitorSysGenSerial monitorSysGenSerial = this.getById(id);

        // 检查串口配置实例是否存在
        if (null == monitorSysGenSerial) {
            return ReturnResult.error("数据不存在, 请刷新后重试");
        }

        try {
            // 测试串口连接
            SerialPortConnection testConnection = openSerialPort(monitorSysGenSerial);
            if (testConnection == null || !testConnection.isConnected()) {
                return ReturnResult.error("串口连接测试失败: 无法打开串口 " + monitorSysGenSerial.getMonitorSysGenSerialPort());
            }

            // 关闭测试连接
            testConnection.close();

            log.info("测试串口连接成功: {}", monitorSysGenSerial.getMonitorSysGenSerialName());

            // 发送串口测试消息
            sendSerialStatusMessage(monitorSysGenSerial, "test");

            return ReturnResult.ok(true, "串口测试连接成功");
        } catch (Exception e) {
            log.error("测试串口连接失败: {}", e.getMessage(), e);
            // 发送错误消息
            sendSerialErrorMessage(monitorSysGenSerial, "test", e.getMessage());
            return ReturnResult.error("测试串口连接失败: " + e.getMessage());
        }
    }

    /**
     * 向串口发送数据
     *
     * @param id 串口配置ID
     * @param data 要发送的数据
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> sendData(String id, String data) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("串口ID不能为空");
        }

        if (StringUtils.isEmpty(data)) {
            return ReturnResult.error("发送数据不能为空");
        }

        try {
            SerialPortConnection connection = activeSerialPorts.get(id);
            if (connection == null || !connection.isConnected()) {
                return ReturnResult.error("串口未开启或连接已断开");
            }

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            log.info("向串口发送数据成功: serialId={}, dataLength={}", id, data.length());

            // 发送数据发送成功消息
            MonitorSysGenSerial serial = this.getById(id);
            if (serial != null) {
                Map<String, Object> message = new HashMap<>(8);
                message.put("id", serial.getMonitorSysGenSerialId());
                message.put("name", serial.getMonitorSysGenSerialName());
                message.put("port", serial.getMonitorSysGenSerialPort());
                message.put("sentData", data);
                message.put("timestamp", System.currentTimeMillis());
                message.put("type", "sent");

                String topic = "gen/serial/" + id;
                socketSessionTemplate.send(topic, Json.toJson(message));
            }

            return ReturnResult.ok(true, "数据发送成功");
        } catch (Exception e) {
            log.error("向串口发送数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("发送数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取串口状态
     *
     * @param id 串口配置ID
     * @return 串口状态信息
     */
    @Override
    public ReturnResult<Map<String, Object>> getStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("串口ID不能为空");
        }

        try {
            MonitorSysGenSerial serial = this.getById(id);
            if (serial == null) {
                return ReturnResult.error("串口配置不存在");
            }

            Map<String, Object> status = new HashMap<>(16);
            status.put("id", serial.getMonitorSysGenSerialId());
            status.put("name", serial.getMonitorSysGenSerialName());
            status.put("port", serial.getMonitorSysGenSerialPort());
            status.put("baudRate", serial.getMonitorSysGenSerialBaudRate());
            status.put("dataBits", serial.getMonitorSysGenSerialDataBits());
            status.put("stopBits", serial.getMonitorSysGenSerialStopBits());
            status.put("parity", serial.getMonitorSysGenSerialParity());
            status.put("flowControl", serial.getMonitorSysGenSerialFlowControl());
            status.put("timeout", serial.getMonitorSysGenSerialTimeout());
            status.put("configStatus", serial.getMonitorSysGenSerialStatus());

            // 检查实际连接状态
            SerialPortConnection connection = activeSerialPorts.get(id);
            boolean connected = connection != null && connection.isConnected();
            status.put("connected", connected);
            status.put("connectionTime", connected ? System.currentTimeMillis() : null);

            // 检查监听任务状态
            ScheduledFuture<?> task = serialDataTasks.get(id);
            boolean listening = task != null && !task.isCancelled() && !task.isDone();
            status.put("listening", listening);

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取串口状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前服务器可用的串口列表
     *
     * @return 串口列表
     */
    @Override
    public ReturnResult<List<Map<String, Object>>> getAvailableSerialPorts() {
        try {
            List<Map<String, Object>> serialPorts = new ArrayList<>();

            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                serialPorts = getWindowsSerialPorts();
            } else if (osName.contains("linux") || osName.contains("unix")) {
                serialPorts = getLinuxSerialPorts();
            } else if (osName.contains("mac")) {
                serialPorts = getMacSerialPorts();
            } else {
                log.warn("不支持的操作系统: {}", osName);
                return ReturnResult.ok(serialPorts, "当前操作系统不支持自动检测串口");
            }

            log.info("检测到 {} 个串口设备", serialPorts.size());
            return ReturnResult.ok(serialPorts);
        } catch (Exception e) {
            log.error("获取串口列表失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取串口列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取Windows系统串口列表
     */
    private List<Map<String, Object>> getWindowsSerialPorts() {
        List<Map<String, Object>> ports = new ArrayList<>();

        try {
            // 使用wmic命令查询串口设备
            ProcessBuilder builder = new ProcessBuilder("wmic", "path", "Win32_SerialPort", "get",
                    "DeviceID,Name,Description,PNPDeviceID", "/format:csv");
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] parts = line.split(",");
                    if (parts.length >= 4 && !parts[1].trim().isEmpty()) {
                        Map<String, Object> portInfo = new HashMap<>();
                        portInfo.put("port", parts[1].trim()); // DeviceID (如: COM1)
                        portInfo.put("name", parts[3].trim()); // Name
                        portInfo.put("description", parts[2].trim()); // Description
                        portInfo.put("pnpDeviceId", parts[4].trim()); // PNPDeviceID
                        portInfo.put("type", "serial");
                        portInfo.put("available", true);
                        portInfo.put("platform", "windows");

                        ports.add(portInfo);
                    }
                }
            }

            process.waitFor();

            // 如果wmic命令失败，尝试使用mode命令检测常见串口
            if (ports.isEmpty()) {
                ports = getWindowsSerialPortsByMode();
            }

        } catch (Exception e) {
            log.error("获取Windows串口列表失败: {}", e.getMessage(), e);
            // 降级方案：检测常见的COM端口
            ports = getWindowsSerialPortsByMode();
        }

        return ports;
    }

    /**
     * 使用mode命令检测Windows串口
     */
    private List<Map<String, Object>> getWindowsSerialPortsByMode() {
        List<Map<String, Object>> ports = new ArrayList<>();

        // 检测COM1到COM20
        for (int i = 1; i <= 20; i++) {
            String comPort = "COM" + i;
            try {
                ProcessBuilder builder = new ProcessBuilder("mode", comPort);
                Process process = builder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    Map<String, Object> portInfo = new HashMap<>();
                    portInfo.put("port", comPort);
                    portInfo.put("name", comPort + " 串口");
                    portInfo.put("description", "Windows串口设备");
                    portInfo.put("type", "serial");
                    portInfo.put("available", true);
                    portInfo.put("platform", "windows");

                    ports.add(portInfo);
                }
            } catch (Exception e) {
                // 忽略单个端口检测失败
            }
        }

        return ports;
    }

    /**
     * 获取Linux系统串口列表
     */
    private List<Map<String, Object>> getLinuxSerialPorts() {
        List<Map<String, Object>> ports = new ArrayList<>();

        try {
            // 检查 /dev/tty* 设备
            ProcessBuilder builder = new ProcessBuilder("ls", "/dev/");
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern serialPattern = Pattern.compile("^(ttyUSB\\d+|ttyACM\\d+|ttyS\\d+|ttyAMA\\d+)$");

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    Matcher matcher = serialPattern.matcher(line);

                    if (matcher.matches()) {
                        String devicePath = "/dev/" + line;

                        // 检查设备是否可访问
                        if (isDeviceAccessible(devicePath)) {
                            Map<String, Object> portInfo = new HashMap<>();
                            portInfo.put("port", devicePath);
                            portInfo.put("name", getLinuxDeviceName(line));
                            portInfo.put("description", getLinuxDeviceDescription(line));
                            portInfo.put("type", "serial");
                            portInfo.put("available", true);
                            portInfo.put("platform", "linux");

                            ports.add(portInfo);
                        }
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            log.error("获取Linux串口列表失败: {}", e.getMessage(), e);
        }

        return ports;
    }

    /**
     * 获取Mac系统串口列表
     */
    private List<Map<String, Object>> getMacSerialPorts() {
        List<Map<String, Object>> ports = new ArrayList<>();

        try {
            // 检查 /dev/cu.* 和 /dev/tty.* 设备
            ProcessBuilder builder = new ProcessBuilder("ls", "/dev/");
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                Pattern serialPattern = Pattern.compile("^(cu\\.|tty\\.)(usbserial|usbmodem|serial).*");

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    Matcher matcher = serialPattern.matcher(line);

                    if (matcher.matches()) {
                        String devicePath = "/dev/" + line;

                        Map<String, Object> portInfo = new HashMap<>();
                        portInfo.put("port", devicePath);
                        portInfo.put("name", getMacDeviceName(line));
                        portInfo.put("description", "Mac串口设备");
                        portInfo.put("type", "serial");
                        portInfo.put("available", true);
                        portInfo.put("platform", "mac");

                        ports.add(portInfo);
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            log.error("获取Mac串口列表失败: {}", e.getMessage(), e);
        }

        return ports;
    }

    /**
     * 检查Linux设备是否可访问
     */
    private boolean isDeviceAccessible(String devicePath) {
        try {
            ProcessBuilder builder = new ProcessBuilder("test", "-c", devicePath);
            Process process = builder.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Linux设备名称
     */
    private String getLinuxDeviceName(String device) {
        if (device.startsWith("ttyUSB")) {
            return "USB串口 " + device;
        } else if (device.startsWith("ttyACM")) {
            return "USB CDC设备 " + device;
        } else if (device.startsWith("ttyS")) {
            return "串行端口 " + device;
        } else if (device.startsWith("ttyAMA")) {
            return "ARM串口 " + device;
        }
        return device;
    }

    /**
     * 获取Linux设备描述
     */
    private String getLinuxDeviceDescription(String device) {
        if (device.startsWith("ttyUSB")) {
            return "USB转串口设备";
        } else if (device.startsWith("ttyACM")) {
            return "USB CDC通信设备";
        } else if (device.startsWith("ttyS")) {
            return "标准串行端口";
        } else if (device.startsWith("ttyAMA")) {
            return "ARM UART设备";
        }
        return "串口设备";
    }

    /**
     * 获取Mac设备名称
     */
    private String getMacDeviceName(String device) {
        if (device.contains("usbserial")) {
            return "USB串口 " + device;
        } else if (device.contains("usbmodem")) {
            return "USB调制解调器 " + device;
        }
        return device;
    }

    @Override
    public IPage<MonitorSysGenSerial> pageFor(Page<MonitorSysGenSerial> page, MonitorSysGenSerial entity) {
        return this.page(
                page, Wrappers.<MonitorSysGenSerial>lambdaQuery()
                        .eq(MonitorSysGenSerial::getGenId, entity.getGenId())
        );
    }

    /**
     * 打开串口连接
     *
     * @param serial 串口配置
     * @return 串口连接
     */
    private SerialPortConnection openSerialPort(MonitorSysGenSerial serial) {
        try {
            String portName = serial.getMonitorSysGenSerialPort();
            Integer baudRate = serial.getMonitorSysGenSerialBaudRate();
            Integer dataBits = serial.getMonitorSysGenSerialDataBits();
            Integer stopBits = serial.getMonitorSysGenSerialStopBits();
            String parity = serial.getMonitorSysGenSerialParity();

            // 设置默认值
            if (baudRate == null) { baudRate = 9600; }
            if (dataBits == null) { dataBits = 8; }
            if (stopBits == null) { stopBits = 1; }
            if (StringUtils.isEmpty(parity)) parity = "NONE";

            // 在Windows系统上使用mode命令配置串口
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return openWindowsSerialPort(portName, baudRate, dataBits, stopBits, parity);
            } else {
                // Linux/Unix系统使用stty命令
                return openUnixSerialPort(portName, baudRate, dataBits, stopBits, parity);
            }
        } catch (Exception e) {
            log.error("打开串口失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 打开Windows串口
     */
    private SerialPortConnection openWindowsSerialPort(String portName, int baudRate, int dataBits, int stopBits, String parity) throws IOException {
        // 使用mode命令配置串口
        String modeCommand = String.format("mode %s baud=%d data=%d stop=%d parity=%s",
                portName, baudRate, dataBits, stopBits, parity.toLowerCase().charAt(0));

        ProcessBuilder modeBuilder = new ProcessBuilder("cmd", "/c", modeCommand);
        Process modeProcess = modeBuilder.start();

        try {
            int exitCode = modeProcess.waitFor();
            if (exitCode != 0) {
                log.warn("串口配置命令执行失败: {}", modeCommand);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("串口配置被中断", e);
        }

        // 使用copy命令读取串口数据
        ProcessBuilder builder = new ProcessBuilder("cmd", "/c", "copy con " + portName);
        Process process = builder.start();

        return new SerialPortConnection(portName, process);
    }

    /**
     * 打开Unix/Linux串口
     */
    private SerialPortConnection openUnixSerialPort(String portName, int baudRate, int dataBits, int stopBits, String parity) throws IOException {
        // 使用stty命令配置串口
        String sttyCommand = String.format("stty -F %s %d cs%d", portName, baudRate, dataBits);
        if (stopBits == 2) {
            sttyCommand += " cstopb";
        }
        if ("EVEN".equalsIgnoreCase(parity)) {
            sttyCommand += " parenb -parodd";
        } else if ("ODD".equalsIgnoreCase(parity)) {
            sttyCommand += " parenb parodd";
        }

        ProcessBuilder sttyBuilder = new ProcessBuilder("sh", "-c", sttyCommand);
        Process sttyProcess = sttyBuilder.start();

        try {
            int exitCode = sttyProcess.waitFor();
            if (exitCode != 0) {
                log.warn("串口配置命令执行失败: {}", sttyCommand);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("串口配置被中断", e);
        }

        // 使用cat命令读取串口数据
        ProcessBuilder builder = new ProcessBuilder("cat", portName);
        Process process = builder.start();

        return new SerialPortConnection(portName, process);
    }

    /**
     * 启动串口数据监听任务
     *
     * @param serialId 串口ID
     * @param serial 串口配置
     * @param connection 串口连接
     */
    private void startSerialDataListener(String serialId, MonitorSysGenSerial serial, SerialPortConnection connection) {
        ScheduledFuture<?> task = scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (!connection.isConnected()) {
                    log.warn("串口连接已断开: {}", serial.getMonitorSysGenSerialName());
                    stopSerialDataListener(serialId);
                    return;
                }

                InputStream inputStream = connection.getInputStream();
                if (inputStream.available() > 0) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        // 发送串口数据到客户端
                        sendSerialDataMessage(serial, data);
                    }
                }
            } catch (IOException e) {
                log.error("读取串口数据失败: {}", e.getMessage(), e);
                stopSerialDataListener(serialId);
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

        serialDataTasks.put(serialId, task);
        log.info("启动串口数据监听任务: serialId={}, port={}", serialId, serial.getMonitorSysGenSerialPort());
    }

    /**
     * 停止串口数据监听任务
     *
     * @param serialId 串口ID
     */
    private void stopSerialDataListener(String serialId) {
        ScheduledFuture<?> task = serialDataTasks.remove(serialId);
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            log.info("停止串口数据监听任务: serialId={}", serialId);
        }
    }

    /**
     * 发送串口数据消息到客户端
     *
     * @param serial 串口配置
     * @param data 串口数据
     */
    private void sendSerialDataMessage(MonitorSysGenSerial serial, String data) {
        if (socketSessionTemplate == null) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>(8);
            message.put("id", serial.getMonitorSysGenSerialId());
            message.put("name", serial.getMonitorSysGenSerialName());
            message.put("port", serial.getMonitorSysGenSerialPort());
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            message.put("type", "data");

            String topic = "gen/serial/" + serial.getMonitorSysGenSerialId();
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.debug("发送串口数据: serialId={}, dataLength={}",
                    serial.getMonitorSysGenSerialId(), data.length());
        } catch (Exception e) {
            log.error("发送串口数据消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("清理串口服务资源...");

        // 停止所有数据监听任务
        serialDataTasks.keySet().forEach(this::stopSerialDataListener);

        // 关闭所有串口连接
        activeSerialPorts.values().forEach(SerialPortConnection::close);
        activeSerialPorts.clear();

        // 关闭线程池
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("串口服务资源清理完成");
    }

    /**
     * 发送串口状态变更消息
     *
     * @param serial 串口配置
     * @param action 操作类型：start, stop, test
     */
    private void sendSerialStatusMessage(MonitorSysGenSerial serial, String action) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送串口状态变更通知");
            return;
        }

        try {
            // 创建消息内容
            Map<String, Object> message = new HashMap<>(8);
            message.put("id", serial.getMonitorSysGenSerialId());
            message.put("name", serial.getMonitorSysGenSerialName());
            message.put("port", serial.getMonitorSysGenSerialPort());
            message.put("status", serial.getMonitorSysGenSerialStatus());
            message.put("action", action);
            message.put("timestamp", System.currentTimeMillis());
            message.put("success", true);

            // 发送消息
            String topic = "gen/serial/" + action;
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.info("发送串口状态变更消息: topic={}, serialId={}, action={}",
                    topic, serial.getMonitorSysGenSerialId(), action);
        } catch (Exception e) {
            log.error("发送串口状态变更消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送串口操作错误消息
     *
     * @param serial 串口配置
     * @param action 操作类型：start, stop, test
     * @param errorMsg 错误信息
     */
    private void sendSerialErrorMessage(MonitorSysGenSerial serial, String action, String errorMsg) {
        if (socketSessionTemplate == null) {
            log.warn("socketSessionTemplate为空，无法发送串口操作错误通知");
            return;
        }

        try {
            // 创建消息内容
            Map<String, Object> message = new HashMap<>(8);
            message.put("id", serial.getMonitorSysGenSerialId());
            message.put("name", serial.getMonitorSysGenSerialName());
            message.put("port", serial.getMonitorSysGenSerialPort());
            message.put("status", serial.getMonitorSysGenSerialStatus());
            message.put("action", action);
            message.put("timestamp", System.currentTimeMillis());
            message.put("success", false);
            message.put("error", errorMsg);

            // 发送消息
            String topic = "gen/serial/" +  action;
            socketSessionTemplate.send(topic, Json.toJson(message));

            log.info("发送串口操作错误消息: topic={}, serialId={}, action={}, error={}",
                    topic, serial.getMonitorSysGenSerialId(), action, errorMsg);
        } catch (Exception e) {
            log.error("发送串口操作错误消息失败: {}", e.getMessage(), e);
        }
    }
} 