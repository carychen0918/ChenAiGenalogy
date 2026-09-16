package chen.genealogy.module.genealogy.controller.admin.showcase.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShowcaseHomeRespVO {

    private PersonCard featuredPerson;
    private List<GalleryItem> gallery = new ArrayList<>();
    private List<CalendarItem> calendar = new ArrayList<>();
    private List<DeedCard> featuredDeeds = new ArrayList<>();
    private List<WallItem> scholarshipWall = new ArrayList<>();
    private List<FeedCard> feeds = new ArrayList<>();
    private long galleryTotal;
    private long memberTotal;
    private long deedTotal;

    @Data
    public static class PersonCard {
        private Long id;
        private String name;
        private String avatar;
        private String photo;
        private String intro;
        private Integer generationNo;
        private String generationWord;
        private String lifeSpan;
        private String source;
    }

    @Data
    public static class GalleryItem {
        private String url;
        private Long memberId;
        private String memberName;
    }

    @Data
    public static class CalendarItem {
        private String type;
        private String title;
        private LocalDate date;
        private String remark;
        private Long refId;
        private String link;
    }

    @Data
    public static class DeedCard {
        private Long id;
        private String kind;
        private Long memberId;
        private String name;
        private String title;
        private String content;
        private String occurYear;
        private String coverUrl;
        private boolean pinned;
    }

    @Data
    public static class WallItem {
        private Long applicationId;
        private Long memberId;
        private String name;
        private String school;
        private String major;
        private Integer year;
        private String grade;
    }

    @Data
    public static class FeedCard {
        private Long id;
        private Integer type;
        private String title;
        private String content;
        private List<String> images;
        private String authorName;
        private Integer likeCount;
        private Integer commentCount;
        private Boolean pinned;
    }
}
