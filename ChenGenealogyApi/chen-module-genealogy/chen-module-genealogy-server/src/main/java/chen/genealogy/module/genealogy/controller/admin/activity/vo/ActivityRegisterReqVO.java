package chen.genealogy.module.genealogy.controller.admin.activity.vo;

import lombok.Data;

@Data
public class ActivityRegisterReqVO {
    private Long activityId;
    private Integer peopleCount;
    private Boolean needBus;
    private String mobile;
}
