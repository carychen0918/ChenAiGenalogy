package chen.genealogy.module.genealogy.controller.admin.scholarship.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScholarshipConfigSaveReqVO {
    private Long id;
    private Integer year;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String policy;
}
