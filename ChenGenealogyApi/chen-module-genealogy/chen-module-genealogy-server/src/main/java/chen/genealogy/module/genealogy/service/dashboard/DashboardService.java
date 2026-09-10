package chen.genealogy.module.genealogy.service.dashboard;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.book.vo.BookMetaRespVO;
import chen.genealogy.module.genealogy.controller.admin.dashboard.vo.DashboardSummaryRespVO;
import chen.genealogy.module.genealogy.controller.admin.dashboard.vo.DashboardSummaryRespVO.DashboardActivityVO;
import chen.genealogy.module.genealogy.controller.admin.dashboard.vo.DashboardSummaryRespVO.DashboardLinkVO;
import chen.genealogy.module.genealogy.controller.admin.dashboard.vo.DashboardSummaryRespVO.DashboardTodoVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedCommentDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.ArchiveApplyDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipConfigDO;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityMapper;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityRegistrationMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.AncestorDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.CultureGuideMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedCommentMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.ArchiveApplyMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipApplicationMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipConfigMapper;
import chen.genealogy.module.genealogy.enums.ScholarshipStatusEnum;
import chen.genealogy.module.genealogy.service.book.BookComposeService;
import chen.genealogy.module.genealogy.service.content.ContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static chen.genealogy.module.genealogy.enums.ScholarshipStatusEnum.*;

@Service
public class DashboardService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private ContentService contentService;
    @Resource
    private BookComposeService bookComposeService;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    private ScholarshipApplicationMapper applicationMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityRegistrationMapper registrationMapper;
    @Resource
    private FeedMapper feedMapper;
    @Resource
    private FeedCommentMapper feedCommentMapper;
    @Resource
    private ArchiveApplyMapper archiveApplyMapper;
    @Resource
    private AncestorDeedMapper ancestorDeedMapper;
    @Resource
    private CultureGuideMapper cultureGuideMapper;
    @Resource
    private ScholarshipConfigMapper scholarshipConfigMapper;

    public DashboardSummaryRespVO summary() {
        FamilyDO family = contentService.getFamily();
        List<MemberDO> members = memberMapper.selectListByFamilyId(FAMILY_ID);
        List<GenerationDO> gens = contentService.getGenerationList();
        BookMetaRespVO book = bookComposeService.getMeta();

        long alive = members.stream().filter(m -> Boolean.TRUE.equals(m.getAlive())).count();
        long incomplete = members.stream().filter(m -> isIncomplete(m, family.getAncestorId())).count();
        int maxGen = members.stream().map(MemberDO::getGenerationNo).filter(Objects::nonNull)
                .max(Integer::compareTo).orElse(0);

        long pendingArchive = archiveApplyMapper.selectCount(ArchiveApplyDO::getStatus, 0);
        long pendingFeed = feedMapper.selectCount(FeedDO::getStatus, 0);
        long pendingComment = feedCommentMapper.selectCount(FeedCommentDO::getStatus, 0);
        long pendingFirst = applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, PENDING_FIRST.getStatus());
        long pendingFinal = applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, FIRST_PASSED.getStatus());
        long pendingAudit = applicationMapper.selectCount(new LambdaQueryWrapperX<ScholarshipApplicationDO>()
                .in(ScholarshipApplicationDO::getStatus, Arrays.asList(
                        PENDING_FIRST.getStatus(), PENDING_COUNTY.getStatus(), PENDING_CITY.getStatus(),
                        PENDING_PROVINCE.getStatus(), PENDING_FAMILY.getStatus())));
        long pendingDisburse = applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, PENDING_DISBURSE.getStatus());
        long pendingMaterial = applicationMapper.selectCount(ScholarshipApplicationDO::getStatus, NEED_MATERIAL.getStatus());

        DashboardSummaryRespVO vo = new DashboardSummaryRespVO();
        vo.setFamilyName(family.getName());
        vo.setSurname(family.getSurname());
        vo.setRegion(family.getRegion());
        vo.setAncestorName(family.getAncestorName());
        vo.setBookTitle(StrUtil.blankToDefault(family.getBookTitle(), family.getName()));
        vo.setBookRevision(family.getBookRevision());
        vo.setBookPrefaceReady(StrUtil.isNotBlank(family.getBookPreface()));

        vo.setMemberCount((long) members.size());
        vo.setAliveCount(alive);
        vo.setDeceasedCount(members.size() - alive);
        vo.setIncompleteCount(incomplete);
        vo.setRecycleCount((long) memberMapper.selectRecycleList().size());
        vo.setMaxGeneration(maxGen);

        vo.setGenerationCount((long) gens.size());
        vo.setGenerationInUse(gens.stream().filter(g -> Objects.equals(g.getStatus(), 1)).count());
        vo.setBookPages(book.getTotalPages());
        vo.setMigrationCount((long) contentService.getMigrationList().size());
        vo.setDeedCount(ancestorDeedMapper.selectCount(new LambdaQueryWrapperX<AncestorDeedDO>()
                .eq(AncestorDeedDO::getFamilyId, FAMILY_ID)));
        vo.setCultureCount((long) cultureGuideMapper.selectListByFamilyId(FAMILY_ID).size());

        vo.setPendingArchive(pendingArchive);
        vo.setPendingFeed(pendingFeed);
        vo.setPendingFeedComment(pendingComment);
        vo.setPendingFirst(pendingFirst);
        vo.setPendingFinal(pendingFinal);
        vo.setPendingAudit(pendingAudit);
        vo.setPendingDisburse(pendingDisburse);
        vo.setPendingMaterial(pendingMaterial);
        vo.setPendingTotal(pendingArchive + pendingFeed + pendingComment + pendingAudit + pendingDisburse + pendingMaterial);

        vo.setActivityCount(activityMapper.selectCount(ActivityDO::getFamilyId, FAMILY_ID));
        fillScholarshipWindow(vo);
        fillUpcoming(vo);
        vo.setTodos(buildTodos());
        vo.setLinks(buildLinks());
        return vo;
    }

    private void fillScholarshipWindow(DashboardSummaryRespVO vo) {
        ScholarshipConfigDO cfg = scholarshipConfigMapper.selectLatest(FAMILY_ID);
        if (cfg == null) {
            vo.setScholarshipWindowOpen(false);
            return;
        }
        vo.setScholarshipYear(cfg.getYear());
        LocalDateTime now = LocalDateTime.now();
        vo.setScholarshipWindowOpen(cfg.getWindowStart() != null && cfg.getWindowEnd() != null
                && !now.isBefore(cfg.getWindowStart()) && !now.isAfter(cfg.getWindowEnd()));
    }

    private void fillUpcoming(DashboardSummaryRespVO vo) {
        ActivityDO next = activityMapper.selectOne(new LambdaQueryWrapperX<ActivityDO>()
                .eq(ActivityDO::getFamilyId, FAMILY_ID)
                .eq(ActivityDO::getStatus, 1)
                .ge(ActivityDO::getStartTime, LocalDateTime.now())
                .orderByAsc(ActivityDO::getStartTime)
                .last("LIMIT 1"));
        if (next == null) {
            next = activityMapper.selectOne(new LambdaQueryWrapperX<ActivityDO>()
                    .eq(ActivityDO::getFamilyId, FAMILY_ID)
                    .ne(ActivityDO::getStatus, 3)
                    .orderByDesc(ActivityDO::getStartTime)
                    .last("LIMIT 1"));
        }
        if (next == null) {
            return;
        }
        DashboardActivityVO av = new DashboardActivityVO();
        av.setId(next.getId());
        av.setTitle(next.getTitle());
        av.setStartTime(next.getStartTime());
        av.setPlace(next.getPlace());
        av.setMaxCount(next.getMaxCount());
        av.setStatus(next.getStatus());
        av.setJoinedCount(registrationMapper.selectJoinedCount(next.getId()));
        vo.setUpcomingActivity(av);
    }

    private List<DashboardTodoVO> buildTodos() {
        List<DashboardTodoVO> list = new ArrayList<>();
        List<ArchiveApplyDO> archives = archiveApplyMapper.selectList(new LambdaQueryWrapperX<ArchiveApplyDO>()
                .eq(ArchiveApplyDO::getStatus, 0).orderByDesc(ArchiveApplyDO::getId).last("LIMIT 5"));
        for (ArchiveApplyDO a : archives) {
            list.add(todo("ARCHIVE", "档案补充待审 #" + a.getId(), a.getCreateTime(), "/genealogy/family/archive", a.getId()));
        }
        List<FeedDO> feeds = feedMapper.selectList(new LambdaQueryWrapperX<FeedDO>()
                .eq(FeedDO::getStatus, 0).orderByDesc(FeedDO::getId).last("LIMIT 5"));
        for (FeedDO f : feeds) {
            list.add(todo("FEED", "动态待审：" + StrUtil.blankToDefault(f.getTitle(), "无标题"), f.getCreateTime(), "/genealogy/ops/feed", f.getId()));
        }
        List<ScholarshipApplicationDO> apps = applicationMapper.selectList(new LambdaQueryWrapperX<ScholarshipApplicationDO>()
                .in(ScholarshipApplicationDO::getStatus, Arrays.asList(
                        PENDING_FIRST.getStatus(), PENDING_COUNTY.getStatus(), PENDING_CITY.getStatus(),
                        PENDING_PROVINCE.getStatus(), PENDING_FAMILY.getStatus(), PENDING_DISBURSE.getStatus(),
                        NEED_MATERIAL.getStatus()))
                .orderByDesc(ScholarshipApplicationDO::getId).last("LIMIT 8"));
        for (ScholarshipApplicationDO a : apps) {
            ScholarshipStatusEnum st = ScholarshipStatusEnum.of(a.getStatus());
            String label = st == null ? "资助事项" : st.getName();
            String route = Objects.equals(a.getStatus(), PENDING_DISBURSE.getStatus())
                    ? "/genealogy/scholarship/disburse" : "/genealogy/scholarship/audit";
            list.add(todo("SCHOLARSHIP", label + " " + StrUtil.blankToDefault(a.getApplyNo(), "#" + a.getId()),
                    a.getSubmitTime() != null ? a.getSubmitTime() : a.getCreateTime(), route, a.getId()));
        }
        list.sort(Comparator.comparing(DashboardTodoVO::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        if (list.size() > 8) {
            return list.subList(0, 8);
        }
        return list;
    }

    private List<DashboardLinkVO> buildLinks() {
        return Arrays.asList(
                link("成员", "/genealogy/family/member", "谱籍档案"),
                link("谱系", "/genealogy/family/pedigree", "世系编辑"),
                link("字辈", "/genealogy/family/generation", "派语维护"),
                link("谱书", "/genealogy/worship/content", "前言与成书"),
                link("资助", "/genealogy/scholarship/audit", "审核发放"),
                link("祭祖", "/genealogy/worship/activity", "活动报名")
        );
    }

    private DashboardTodoVO todo(String type, String title, LocalDateTime time, String route, Long id) {
        DashboardTodoVO vo = new DashboardTodoVO();
        vo.setType(type);
        vo.setTitle(title);
        vo.setTime(time);
        vo.setRoute(route);
        vo.setId(id);
        return vo;
    }

    private DashboardLinkVO link(String label, String route, String hint) {
        DashboardLinkVO vo = new DashboardLinkVO();
        vo.setLabel(label);
        vo.setRoute(route);
        vo.setHint(hint);
        return vo;
    }

    private boolean isIncomplete(MemberDO m, Long ancestorId) {
        if (m.getGenerationNo() == null) {
            return true;
        }
        if (m.getFatherId() == null && (ancestorId == null || !ancestorId.equals(m.getId()))) {
            return true;
        }
        return false;
    }
}
