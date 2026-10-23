package com.chua.starter.monitor.agent;

/**
 * 系统指标数据源 SPI。
 *
 * <p>实现类通过 {@code @Spi("名称")} 注册，名称约定：</p>
 * <ul>
 *   <li>{@code none} —— 无可用后端时的降级实现，恒可用，返回空数据；</li>
 *   <li>{@code hotsport} —— 直连 hotsport 观测后端，整体透传同一套
 *       {@code /api/apm/*} 契约；</li>
 *   <li>{@code skywalker} —— 直连 SkyWalking OAP GraphQL，翻译成前端契约。</li>
 * </ul>
 *
 * <p>实现无状态、单例：{@link SystemMetricRegistry} 通过
 * {@code ServiceProvider.of(SystemMetricProvider.class)} 统一发现与路由，
 * 与 ACME 模块 {@code AcmeProvider} 的用法保持一致。</p>
 *
 * @author CH
 * @since 4.0.0.42
 * @see SystemMetricRegistry
 */
public interface SystemMetricProvider {

    /**
     * SPI 名称：none / hotsport / skywalker。
     *
     * @return 名称
     */
    String name();

    /**
     * 人类可读描述，用于前端 provider 状态展示。
     *
     * @return 描述
     */
    String description();

    /**
     * 探测后端是否可达。
     *
     * <p>探测逻辑各实现自行定义（hotsport 探 /health，skywalker 探 /graphql），
     * 超时与目标地址取自配置。探测失败应当返回 {@code false} 而不是抛异常。</p>
     *
     * @param properties 系统指标配置
     * @return 可达为 true
     */
    boolean probe(SystemMetricProperties properties);

    /**
     * 处理一次接口调用。
     *
     * @param request 请求（方法 / 路径 / 查询 / 体 / 头）
     * @param properties 系统指标配置
     * @return 响应对象
     * @throws Exception 上游不可达、超时或翻译失败
     */
    SystemMetricResponse forward(SystemMetricRequest request, SystemMetricProperties properties) throws Exception;
}