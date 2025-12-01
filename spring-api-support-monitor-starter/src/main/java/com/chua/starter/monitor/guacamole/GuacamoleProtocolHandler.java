package com.chua.starter.monitor.guacamole;

import com.chua.common.support.utils.MapUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Guacamole协议处理器
 * 处理Guacamole协议指令的解析和生成
 *
 * @author CH
 * @since 2024/12/18
 */
@Slf4j
public class GuacamoleProtocolHandler {

    /**
     * 指令分隔符
     */
    private static final String INSTRUCTION_SEPARATOR = ",";
    private static final String INSTRUCTION_TERMINATOR = ";";

    /**
     * 处理鼠标事件
     */
    public static String handleMouseEvent(Map<String, Object> data) {
        try {
            String type = MapUtils.getString(data, "type");
            Integer x = MapUtils.getInteger(data, "x");
            Integer y = MapUtils.getInteger(data, "y");
            Integer button = MapUtils.getInteger(data, "button", 0);

            if (x == null || y == null) {
                log.warn("鼠标事件坐标不能为空");
                return null;
            }

            return switch (type) {
                case "move" -> createInstruction("mouse", x.toString(), y.toString());
                case "down" -> createInstruction("mouse", x.toString(), y.toString(), "1");
                case "up" -> createInstruction("mouse", x.toString(), y.toString(), "0");
                case "click" ->
                    // 点击事件：先按下再释放
                    createInstruction("mouse", x.toString(), y.toString(), "1") +
                        createInstruction("mouse", x.toString(), y.toString(), "0");
                case "scroll" -> {
                    Integer delta = MapUtils.getInteger(data, "delta", 0);
                    yield createInstruction("mouse", x.toString(), y.toString(), "0", delta.toString());
                }
                default -> {
                    log.warn("未知的鼠标事件类型: {}", type);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("处理鼠标事件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 处理键盘事件
     */
    public static String handleKeyboardEvent(Map<String, Object> data) {
        try {
            String type = MapUtils.getString(data, "type");
            Integer keyCode = MapUtils.getInteger(data, "keyCode");
            String key = MapUtils.getString(data, "key");

            if (keyCode == null && key == null) {
                log.warn("键盘事件键码或键值不能为空");
                return null;
            }

            // 如果没有keyCode，尝试从key转换
            if (keyCode == null && key != null) {
                keyCode = convertKeyToKeyCode(key);
            }

            if (keyCode == null) {
                log.warn("无法获取有效的键码");
                return null;
            }

            return switch (type) {
                case "down", "keydown" -> createInstruction("key", keyCode.toString(), "1");
                case "up", "keyup" -> createInstruction("key", keyCode.toString(), "0");
                case "press" ->
                    // 按键事件：先按下再释放
                    createInstruction("key", keyCode.toString(), "1") +
                        createInstruction("key", keyCode.toString(), "0");
                default -> {
                    log.warn("未知的键盘事件类型: {}", type);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("处理键盘事件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建Guacamole指令
     */
    public static String createInstruction(String opcode, String... args) {
        StringBuilder instruction = new StringBuilder();
        instruction.append(opcode.length()).append(".").append(opcode);

        for (String arg : args) {
            if (arg != null) {
                instruction.append(INSTRUCTION_SEPARATOR)
                          .append(arg.length())
                          .append(".")
                          .append(arg);
            }
        }

        instruction.append(INSTRUCTION_TERMINATOR);
        return instruction.toString();
    }

    /**
     * 解析Guacamole指令
     */
    public static GuacamoleInstruction parseInstruction(String instructionData) {
        try {
            if (instructionData == null || instructionData.trim().isEmpty()) {
                return null;
            }

            // 移除终止符
            String data = instructionData.trim();
            if (data.endsWith(INSTRUCTION_TERMINATOR)) {
                data = data.substring(0, data.length() - 1);
            }

            // 解析指令
            List<String> elements = new ArrayList<>();
            int index = 0;

            while (index < data.length()) {
                // 查找长度
                int dotIndex = data.indexOf('.', index);
                if (dotIndex == -1) { break; }

                int length = Integer.parseInt(data.substring(index, dotIndex));
                int startIndex = dotIndex + 1;
                int endIndex = startIndex + length;

                if (endIndex > data.length()) { break; }

                String element = data.substring(startIndex, endIndex);
                elements.add(element);

                index = endIndex;
                if (index < data.length() && data.charAt(index) == ',') {
                    index++; // 跳过分隔符
                }
            }

            if (elements.isEmpty()) {
                return null;
            }

            String opcode = elements.get(0);
            String[] args = elements.subList(1, elements.size()).toArray(new String[0]);

            return new GuacamoleInstruction(opcode, args);

        } catch (Exception e) {
            log.error("解析Guacamole指令失败: instructionData={}, error={}", instructionData, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将键值转换为键码
     */
    private static Integer convertKeyToKeyCode(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        // 常见键值映射
        switch (key.toLowerCase()) {
            case "enter":
            case "return":
                return 13;
            case "escape":
            case "esc":
                return 27;
            case "space":
                return 32;
            case "tab":
                return 9;
            case "backspace":
                return 8;
            case "delete":
                return 46;
            case "arrowup":
            case "up":
                return 38;
            case "arrowdown":
            case "down":
                return 40;
            case "arrowleft":
            case "left":
                return 37;
            case "arrowright":
            case "right":
                return 39;
            case "shift":
                return 16;
            case "control":
            case "ctrl":
                return 17;
            case "alt":
                return 18;
            default:
                // 对于单个字符，使用ASCII码
                if (key.length() == 1) {
                    return (int) key.toUpperCase().charAt(0);
                }
                return null;
        }
    }

    /**
     * Guacamole指令类
     */
    @Data
    public static class GuacamoleInstruction {
        private final String opcode;
        private final String[] args;

        public GuacamoleInstruction(String opcode, String[] args) {
            this.opcode = opcode;
            this.args = args != null ? args : new String[0];
        }

        public String getArg(int index) {
            return index >= 0 && index < args.length ? args[index] : null;
        }

        public int getArgCount() {
            return args.length;
        }

        @Override
        public String toString() {
            return String.format("GuacamoleInstruction[opcode=%s, args=%d]", opcode, args.length);
        }
    }
}
