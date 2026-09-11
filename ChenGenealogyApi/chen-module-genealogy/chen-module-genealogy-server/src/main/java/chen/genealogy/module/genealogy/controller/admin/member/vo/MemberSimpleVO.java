package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSimpleVO {

    private Long id;
    private String name;
    private Integer gender;
    private Integer generationNo;
    private String generationWord;
    private String generationHouse;
    private String generationNationalSource;
    private String avatar;
    private Boolean alive;
}
