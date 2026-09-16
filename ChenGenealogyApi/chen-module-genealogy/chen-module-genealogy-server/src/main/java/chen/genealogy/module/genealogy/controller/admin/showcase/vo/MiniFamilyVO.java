package chen.genealogy.module.genealogy.controller.admin.showcase.vo;

import chen.genealogy.module.genealogy.controller.admin.member.vo.MemberSimpleVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MiniFamilyVO {
    private MemberSimpleVO self;
    /** 父系上溯，远祖在前，最多三代 */
    private List<MemberSimpleVO> ancestors = new ArrayList<>();
    private MemberSimpleVO mother;
    private List<MemberSimpleVO> spouses = new ArrayList<>();
    private List<MemberSimpleVO> siblings = new ArrayList<>();
    private List<MemberSimpleVO> children = new ArrayList<>();
    private List<MemberSimpleVO> grandchildren = new ArrayList<>();
}
