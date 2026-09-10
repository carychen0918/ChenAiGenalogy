package chen.genealogy.module.genealogy.controller.admin.book.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BookSearchRespVO {

    /** 世系检索需登录 */
    private Boolean loginRequired;
    private List<BookSearchHitVO> list = new ArrayList<>();
}
