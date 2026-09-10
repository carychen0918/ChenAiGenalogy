package chen.genealogy.module.genealogy.controller.admin.member.vo;

import lombok.Data;

import java.util.List;

@Data
public class MemberPhotoSaveReqVO {
    private List<String> photoUrls;
}
