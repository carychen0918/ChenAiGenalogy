package chen.genealogy.module.genealogy.controller.admin.dashboard.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardSummaryRespVO {

    private String familyName;
    private String surname;
    private String region;
    private String ancestorName;
    private String bookTitle;
    private String bookRevision;
    private Boolean bookPrefaceReady;

    private Long memberCount;
    private Long aliveCount;
    private Long deceasedCount;
    private Long incompleteCount;
    private Long recycleCount;
    private Integer maxGeneration;

    private Long generationCount;
    private Long generationInUse;
    private Integer bookPages;
    private Long migrationCount;
    private Long deedCount;
    private Long cultureCount;

    private Long pendingArchive;
    private Long pendingFeed;
    private Long pendingFeedComment;
    private Long pendingFirst;
    private Long pendingFinal;
    private Long pendingAudit;
    private Long pendingDisburse;
    private Long pendingMaterial;
    private Long pendingTotal;

    private Long activityCount;
    private Boolean scholarshipWindowOpen;
    private Integer scholarshipYear;

    private DashboardActivityVO upcomingActivity;
    private List<DashboardTodoVO> todos = new ArrayList<>();
    private List<DashboardLinkVO> links = new ArrayList<>();

    @Data
    public static class DashboardActivityVO {
        private Long id;
        private String title;
        private LocalDateTime startTime;
        private String place;
        private Long joinedCount;
        private Integer maxCount;
        private Integer status;
    }

    @Data
    public static class DashboardTodoVO {
        private String type;
        private String title;
        private LocalDateTime time;
        private String route;
        private Long id;
    }

    @Data
    public static class DashboardLinkVO {
        private String label;
        private String route;
        private String hint;
    }
}
