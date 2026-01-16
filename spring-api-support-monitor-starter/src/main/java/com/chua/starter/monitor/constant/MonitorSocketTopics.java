package com.chua.starter.monitor.constant;

/**
 * 监控模块 Socket.IO 主题常量
 * <p>
 * 命名格式: 业务:模块:功能
 * 例如: remote:ssh:connect, monitor:docker:image_pull_progress
 *
 * @author CH
 * @version 1.0.0
 * @since 2024-12-27
 */
public final class MonitorSocketTopics {

    private MonitorSocketTopics() {
    }

    /**
     * 系统模块主题 (system)
     * 用于系统级别的通用功能
     */
    public static final class System {

        private System() {
        }

        /**
         * 消息相关
         */
        public static final class Message {
            public static final String PUSH = "system:message:push";
            public static final String NOTIFICATION = "system:message:notification";
            public static final String READ = "system:message:read";
            public static final String DELETE = "system:message:delete";

            private Message() {
            }
        }

        /**
         * 用户相关
         */
        public static final class User {
            public static final String ONLINE = "system:user:online";
            public static final String OFFLINE = "system:user:offline";
            public static final String STATUS = "system:user:status";
            public static final String KICK = "system:user:kick";

            private User() {
            }
        }

        /**
         * 配置相关
         */
        public static final class Config {
            public static final String UPDATE = "system:config:update";
            public static final String VERSION = "system:config:version";

            private Config() {
            }
        }

        /**
         * 权限相关
         */
        public static final class Auth {
            public static final String TOKEN_EXPIRED = "system:auth:token_expired";
            public static final String PERMISSION_CHANGE = "system:auth:permission_change";

            private Auth() {
            }
        }
    }

    /**
     * 服务模块主题 (service)
     * 用于服务级别的通用功能
     */
    public static final class Service {

        private Service() {
        }

        /**
         * 消息相关
         */
        public static final class Message {
            public static final String PUSH = "service:message:push";
            public static final String NOTIFICATION = "service:message:notification";
            public static final String READ = "service:message:read";
            public static final String DELETE = "service:message:delete";

            private Message() {
            }
        }

        /**
         * 操作相关
         */
        public static final class Operation {
            public static final String START = "service:operation:start";
            public static final String STOP = "service:operation:stop";
            public static final String RESTART = "service:operation:restart";
            public static final String STATUS = "service:operation:status";
            public static final String LOG = "service:operation:log";
            public static final String PROGRESS = "service:operation:progress";
            public static final String COMPLETE = "service:operation:complete";
            public static final String ERROR = "service:operation:error";

            private Operation() {
            }
        }
    }

    /**
     * 远程连接模块主题 (remote)
     * 用于远程服务器连接 (SSH/RDP/VNC)
     */
    public static final class Remote {

        private Remote() {
        }

        /**
         * SSH 相关
         */
        public static final class Ssh {
            public static final String CONNECT = "remote:ssh:connect";
            public static final String DATA = "remote:ssh:data";
            public static final String INPUT = "remote:ssh:input";
            public static final String DISCONNECT = "remote:ssh:disconnect";
            public static final String RESIZE = "remote:ssh:resize";
            public static final String ERROR = "remote:ssh:error";

            private Ssh() {
            }
        }

        /**
         * RDP 相关
         */
        public static final class Rdp {
            public static final String CONNECT = "remote:rdp:connect";
            public static final String DATA = "remote:rdp:data";
            public static final String INPUT = "remote:rdp:input";
            public static final String DISCONNECT = "remote:rdp:disconnect";
            public static final String RESIZE = "remote:rdp:resize";
            public static final String ERROR = "remote:rdp:error";

            private Rdp() {
            }
        }

        /**
         * VNC 相关
         */
        public static final class Vnc {
            public static final String CONNECT = "remote:vnc:connect";
            public static final String DATA = "remote:vnc:data";
            public static final String INPUT = "remote:vnc:input";
            public static final String DISCONNECT = "remote:vnc:disconnect";
            public static final String RESIZE = "remote:vnc:resize";
            public static final String ERROR = "remote:vnc:error";

            private Vnc() {
            }
        }

        /**
         * 自定义连接相关
         */
        public static final class Custom {
            public static final String CONNECT = "remote:custom:connect";
            public static final String CONNECTED = "remote:custom:connected";
            public static final String DATA = "remote:custom:data";
            public static final String INPUT = "remote:custom:input";
            public static final String DISCONNECT = "remote:custom:disconnect";
            public static final String DISCONNECTED = "remote:custom:disconnected";
            public static final String COMMAND = "remote:custom:command";
            public static final String COMMAND_RESULT = "remote:custom:command_result";
            public static final String RESIZE = "remote:custom:resize";
            public static final String ERROR = "remote:custom:error";
            public static final String BINARY_DATA = "remote:custom:binary_data";
            public static final String BINARY_REQUEST = "remote:custom:binary_request";

            private Custom() {
            }
        }

        /**
         * 远程桌面相关 (基于图片传输)
         */
        public static final class Desktop {
            public static final String CONNECT = "remote:desktop:connect";
            public static final String CONNECTED = "remote:desktop:connected";
            public static final String SCREEN = "remote:desktop:screen";
            public static final String MOUSE_MOVE = "remote:desktop:mouse_move";
            public static final String MOUSE_CLICK = "remote:desktop:mouse_click";
            public static final String MOUSE_WHEEL = "remote:desktop:mouse_wheel";
            public static final String KEY_PRESS = "remote:desktop:key_press";
            public static final String KEY_RELEASE = "remote:desktop:key_release";
            public static final String DISCONNECT = "remote:desktop:disconnect";
            public static final String DISCONNECTED = "remote:desktop:disconnected";
            public static final String SET_QUALITY = "remote:desktop:set_quality";
            public static final String SET_COMPRESSOR = "remote:desktop:set_compressor";
            public static final String CAPTURE_RESET = "remote:desktop:capture_reset";
            public static final String ERROR = "remote:desktop:error";
            public static final String MODE_DETECTED = "remote:desktop:mode_detected";

            private Desktop() {
            }
        }

        /**
         * WebRTC 相关 (基于 WebRTC 实时视频流)
         */
        public static final class WebRTC {
            public static final String CONNECT = "remote:webrtc:connect";
            public static final String CONNECTED = "remote:webrtc:connected";
            public static final String DISCONNECT = "remote:webrtc:disconnect";
            public static final String DISCONNECTED = "remote:webrtc:disconnected";
            public static final String OFFER = "remote:webrtc:offer";
            public static final String ANSWER = "remote:webrtc:answer";
            public static final String ICE_CANDIDATE = "remote:webrtc:ice_candidate";
            public static final String STREAM_STARTED = "remote:webrtc:stream_started";
            public static final String STREAM_STATS = "remote:webrtc:stream_stats";
            public static final String ERROR = "remote:webrtc:error";

            private WebRTC() {
            }
        }
    }

    /**
     * 监控模块主题 (monitor)
     * 用于监控相关功能
     */
    public static final class Monitor {

        private Monitor() {
        }

        /**
         * Docker/容器相关
         */
        public static final class Docker {
            public static final String IMAGE_PULL_PROGRESS = "monitor:docker:image_pull_progress";
            public static final String IMAGE_EXPORT_PROGRESS = "monitor:docker:image_export_progress";
            public static final String IMAGE_IMPORT_PROGRESS = "monitor:docker:image_import_progress";
            public static final String CONTAINER_STATUS = "monitor:docker:container_status";
            public static final String CONTAINER_LOG = "monitor:docker:container_log";
            public static final String CONTAINER_STATISTICS = "monitor:docker:container_statistics";
            public static final String CONTAINER_EVENTS = "monitor:docker:container_events";
            public static final String START = "monitor:docker:start";
            public static final String PROGRESS = "monitor:docker:progress";
            public static final String COMPLETE = "monitor:docker:complete";
            public static final String ERROR = "monitor:docker:error";

            private Docker() {
            }
        }

        /**
         * 服务器相关
         */
        public static final class Server {
            public static final String STATUS = "monitor:server:status";
            public static final String METRICS = "monitor:server:metrics";
            public static final String ALERT = "monitor:server:alert";
            public static final String TERMINAL = "monitor:server:terminal";
            public static final String CONNECTION_STATUS = "monitor:server:connection_status";
            public static final String CONNECTION_TEST = "monitor:server:connection_test";
            public static final String HEALTH = "monitor:server:health";

            private Server() {
            }
        }

        /**
         * 软件相关
         */
        public static final class Software {
            public static final String INSTALL_PROGRESS = "monitor:software:install_progress";
            public static final String SYNC_PROGRESS = "monitor:software:sync_progress";

            private Software() {
            }
        }

        /**
         * 操作进度
         */
        public static final class Operation {
            public static final String PROGRESS = "monitor:operation:progress";
            public static final String COMPLETE = "monitor:operation:complete";
            public static final String ERROR = "monitor:operation:error";
            public static final String UPDATE = "monitor:operation:update";

            private Operation() {
            }
        }

        /**
         * 数据同步相关
         */
        public static final class Sync {
            public static final String STATUS = "monitor:sync:status";
            public static final String LOG = "monitor:sync:log";
            public static final String PROGRESS = "monitor:sync:progress";
            public static final String METRICS = "monitor:sync:metrics";
            public static final String ERROR = "monitor:sync:error";

            private Sync() {
            }
        }

        /**
         * 爬虫相关
         */
        public static final class Spider {
            public static final String STATUS = "monitor:spider:status";
            public static final String LOG = "monitor:spider:log";
            public static final String PROGRESS = "monitor:spider:progress";
            public static final String METRICS = "monitor:spider:metrics";
            public static final String DATA = "monitor:spider:data";
            public static final String ERROR = "monitor:spider:error";

            private Spider() {
            }
        }

    }

    /**
     * 视频模块主题 (video)
     * 用于视频监控相关功能
     */
    public static final class Video {

        private Video() {
        }

        /**
         * 设备相关
         */
        public static final class Device {
            public static final String STATUS = "video:device:status";
            public static final String ONLINE = "video:device:online";
            public static final String OFFLINE = "video:device:offline";
            public static final String ALARM = "video:device:alarm";

            private Device() {
            }
        }

        /**
         * 录像相关
         */
        public static final class Record {
            public static final String START = "video:record:start";
            public static final String STOP = "video:record:stop";
            public static final String PROGRESS = "video:record:progress";

            private Record() {
            }
        }

        /**
         * 流媒体相关
         */
        public static final class Stream {
            public static final String START = "video:stream:start";
            public static final String STOP = "video:stream:stop";
            public static final String ERROR = "video:stream:error";

            private Stream() {
            }
        }
    }
}
