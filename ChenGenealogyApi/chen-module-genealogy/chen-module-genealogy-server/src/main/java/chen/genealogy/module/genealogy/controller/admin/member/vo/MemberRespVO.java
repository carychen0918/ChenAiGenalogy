package chen.genealogy.module.genealogy.controller.admin.member.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 成员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MemberRespVO {

    @ExcelProperty("编号")
    private Long id;
    private Long familyId;
    private Long userId;
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("性别")
    private Integer gender;
    private Long generationId;
    private Integer generationNo;
    private String generationWord;
    private LocalDateTime birthDate;
    private LocalDateTime deathDate;
    private Long fatherId;
    private String fatherName;
    private Long motherId;
    private String motherName;
    private List<Long> spouseIds;
    private List<String> spouseNames;
    private String intro;
    private String avatar;
    private String mobile;
    private String idCard;
    private String tags;
    private List<String> photoUrls;
    private List<String> documentUrls;
    private Boolean alive;
    private LocalDateTime createTime;
    private List<MemberDeedRespVO> deeds;
    private List<MemberSimpleVO> children;
    private Integer descendantCount;
    @Schema(description = "登录账号")
    private String loginUsername;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String address;
    private String regionName;
}
