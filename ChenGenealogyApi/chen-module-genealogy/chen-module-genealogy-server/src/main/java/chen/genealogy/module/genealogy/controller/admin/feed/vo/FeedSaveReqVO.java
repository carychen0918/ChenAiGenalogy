package chen.genealogy.module.genealogy.controller.admin.feed.vo;

import lombok.Data;

import java.util.List;

@Data
public class FeedSaveReqVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private List<String> images;
    private Boolean pinned;
}
