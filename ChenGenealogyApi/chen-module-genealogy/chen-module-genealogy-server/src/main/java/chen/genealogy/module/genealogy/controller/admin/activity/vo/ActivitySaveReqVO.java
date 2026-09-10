package chen.genealogy.module.genealogy.controller.admin.activity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivitySaveReqVO {
    private Long id;
    @NotBlank
    private String title;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    @NotBlank
    private String place;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String processDesc;
    @NotBlank
    private String gatherPlace;
    private String notice;
    @NotNull
    private LocalDateTime deadline;
    private Integer maxCount;
    @NotBlank
    private String contactName;
    private String contactMobile;
    private Integer status;
}
