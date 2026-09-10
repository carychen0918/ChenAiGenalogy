package chen.genealogy.module.genealogy.controller.admin.member.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static chen.genealogy.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 成员分页")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberPageReqVO extends PageParam {

    private String name;
    private Integer gender;
    private Integer generationNo;
    private Long fatherId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] birthDate;
}
