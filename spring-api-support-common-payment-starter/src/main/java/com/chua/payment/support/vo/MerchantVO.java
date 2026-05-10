package com.chua.payment.support.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户视图对象
 *
 * @author CH
 * @since 2026-03-18
 */
@Data
public class MerchantVO implements Serializable {
    private Long id;

    private String merchantNo;

    private String merchantName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String businessLicense;

    private String legalPerson;

    private Boolean paymentWalletEnabled;

    private Boolean autoCloseEnabled;

    private Integer autoCloseMinutes;

    private Boolean paymentSplitTableEnabled;

    private Boolean paymentProfitSharingEnabled;

    private Boolean paymentCouponEnabled;

    private String remark;

    private Integer status;

    private String statusDesc;

    private Integer channelCount;

    private List<String> channelTypes;

    private List<String> channelTypeDetails;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
