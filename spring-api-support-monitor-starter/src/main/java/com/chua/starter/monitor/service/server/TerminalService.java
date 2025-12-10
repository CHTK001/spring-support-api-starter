package com.chua.starter.monitor.service.server;//package com.chua.starter.monitor.service.server;
//
//import com.chua.ssh.support.ssh.SshClient;
//import com.chua.sshj.support.ssh.SshjClient;
//
///**
// * 终端
// * @author CH
// * @since 2024/6/19
// */
//public interface TerminalService {
//
//    /**
//     * 根据请求ID获取会话对象。
//     *
//     * 此方法旨在通过请求ID检索与特定请求相关的会话对象。会话对象可能包含关于用户会话的重要信息，
//     * 如用户身份验证状态、会话变量等。
//     *
//     * @param genId 请求ID，用于唯一标识一个请求。这是检索会话的关键依据。
//     * @return 与给定请求ID关联的会话对象。如果找不到匹配的会话，可能返回null。
//     */
//    SshClient getClient(String genId);
//
//    /**
//     * 根据SSH ID创建或获取SSH客户端。
//     * <p>
//     * 此方法通过SSH ID获取SSH模型信息，并创建或返回对应的SSH客户端对象。
//     * 如果该SSH ID的客户端已经存在，则直接返回缓存的客户端对象；
//     * 否则，创建新的SSH客户端对象并缓存。
//     *
//     * @param sshId SSH ID，用于唯一标识一个SSH连接配置
//     * @return 与给定SSH ID关联的SSH客户端对象。如果找不到匹配的SSH配置或创建失败，返回null。
//     */
//    SshjClient createClientBySshId(String sshId);
//}
