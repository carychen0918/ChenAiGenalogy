package chen.genealogy.module.genealogy.controller.admin.member.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberImportExcelVO {

    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("世代")
    private Integer generationNo;
    @ExcelProperty("字辈")
    private String generationWord;
    @ExcelProperty("出生日期")
    private String birthDate;
    @ExcelProperty("逝世日期")
    private String deathDate;
    @ExcelProperty("父亲姓名")
    private String fatherName;
    @ExcelProperty("母亲姓名")
    private String motherName;
    @ExcelProperty("配偶姓名")
    private String spouseName;
    @ExcelProperty("简介")
    private String intro;
}
