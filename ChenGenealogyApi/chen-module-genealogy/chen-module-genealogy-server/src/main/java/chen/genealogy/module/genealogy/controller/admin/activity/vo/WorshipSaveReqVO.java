package chen.genealogy.module.genealogy.controller.admin.activity.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorshipSaveReqVO {
    private Long activityId;
    private String content;
    private List<String> images;
    private Boolean online;
    private String ancestorName;
}
