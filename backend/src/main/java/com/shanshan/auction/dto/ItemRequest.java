package com.shanshan.auction.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequest {
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题最多100个字符")
    private String title;

    @Size(max = 500, message = "商品描述最多500个字符")
    private String description;

    @NotNull(message = "起拍价不能为空")
    @DecimalMin(value = "0.01", message = "起拍价必须大于0")
    private BigDecimal startPrice;

    @NotNull(message = "加价幅度不能为空")
    @DecimalMin(value = "0.01", message = "加价幅度必须大于0")
    private BigDecimal incrementAmount;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是将来时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是将来时间")
    private LocalDateTime endTime;

    @NotNull(message = "延时时长不能为空")
    @Min(value = 30, message = "延时时长必须大于30秒")
    @Max(value = 3600, message = "延时时长不能超过3600秒")
    private Integer delayDuration;

    @NotEmpty(message = "商品图片不能为空")
    @Size(min = 1, max = 9, message = "图片数量必须在1-9张之间")
    private List<@Pattern(
        regexp = "^(http|https)://.*$",
        message = "图片URL必须以http://或https://开头"
    ) String> imageUrls;

    @AssertTrue(message = "结束时间必须晚于开始时间")
    private boolean isEndTimeValid() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
