package com.chua.starter.payment.test;

import com.chua.payment.support.dto.PaymentSchedulerTaskUpdateDTO;
import com.chua.payment.support.service.PaymentSchedulerTaskOpsService;
import com.chua.payment.support.vo.PaymentSchedulerTaskVO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * 独立 Payment 测试后端。
 */
@EnableCaching
@EnableScheduling
@ConditionalOnProperty(prefix = "plugin.payment.server", name = "enable", havingValue = "true", matchIfMissing = true)
@SpringBootApplication(scanBasePackages = {"com.chua.starter.payment.test", "com.chua.payment.support"})
@MapperScan("com.chua.starter.payment.test.mapper")
public class PaymentTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentTestApplication.class, args);
    }

    /**
     * scheduler.enabled=false 时提供空实现，确保 PaymentOpsController 启动。
     */
    @Bean
    public PaymentSchedulerTaskOpsService paymentSchedulerTaskOpsService() {
        return new PaymentSchedulerTaskOpsService() {
            @Override
            public List<PaymentSchedulerTaskVO> listTasks() {
                return List.of();
            }

            @Override
            public PaymentSchedulerTaskVO updateTask(String taskKey, PaymentSchedulerTaskUpdateDTO dto) {
                PaymentSchedulerTaskVO vo = new PaymentSchedulerTaskVO();
                vo.setTaskKey(taskKey);
                vo.setEnabled(dto.getEnabled());
                return vo;
            }

            @Override
            public PaymentSchedulerTaskVO triggerTask(String taskKey) {
                PaymentSchedulerTaskVO vo = new PaymentSchedulerTaskVO();
                vo.setTaskKey(taskKey);
                return vo;
            }
        };
    }
}
