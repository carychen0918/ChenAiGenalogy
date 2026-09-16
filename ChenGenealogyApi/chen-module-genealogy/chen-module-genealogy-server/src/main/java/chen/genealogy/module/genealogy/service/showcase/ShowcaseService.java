package chen.genealogy.module.genealogy.service.showcase;

import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.member.vo.MemberSimpleVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.MiniFamilyVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.RelationHopVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.ShowcaseConfigSaveReqVO;
import chen.genealogy.module.genealogy.controller.admin.showcase.vo.ShowcaseHomeRespVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.ActivityDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.feed.FeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipConfigDO;
import chen.genealogy.module.genealogy.dal.dataobject.showcase.ShowcaseConfigDO;
import chen.genealogy.module.genealogy.dal.mysql.activity.ActivityMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.AncestorDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.feed.FeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.generation.GenerationMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipApplicationMapper;
import chen.genealogy.module.genealogy.dal.mysql.scholarship.ScholarshipConfigMapper;
import chen.genealogy.module.genealogy.dal.mysql.showcase.ShowcaseConfigMapper;
import chen.genealogy.module.genealogy.enums.ScholarshipStatusEnum;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class ShowcaseService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private ShowcaseConfigMapper showcaseConfigMapper;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    private MemberDeedMapper memberDeedMapper;
    @Resource
    private AncestorDeedMapper ancestorDeedMapper;
    @Resource
    private FeedMapper feedMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ScholarshipConfigMapper scholarshipConfigMapper;
    @Resource
    private ScholarshipApplicationMapper applicationMapper;
    @Resource
    private GenerationMapper generationMapper;

    public ShowcaseConfigDO getConfig() {
        ShowcaseConfigDO cfg = showcaseConfigMapper.selectByFamilyId(FAMILY_ID);
        return cfg == null ? new ShowcaseConfigDO() : cfg;
    }

    public void saveConfig(ShowcaseConfigSaveReqVO reqVO) {
        ShowcaseConfigDO exists = showcaseConfigMapper.selectByFamilyId(FAMILY_ID);
        if (exists == null) {
            ShowcaseConfigDO row = new ShowcaseConfigDO();
            row.setFamilyId(FAMILY_ID);
            row.setFeaturedMemberId(reqVO.getFeaturedMemberId());
            row.setFeaturedDeedId(reqVO.getFeaturedDeedId());
            row.setFeaturedAncestorDeedId(reqVO.getFeaturedAncestorDeedId());
            row.setWallApplicationIds(reqVO.getWallApplicationIds());
            row.setCalendarMemberIds(reqVO.getCalendarMemberIds());
            showcaseConfigMapper.insert(row);
            return;
        }
        exists.setFeaturedMemberId(reqVO.getFeaturedMemberId());
        exists.setFeaturedDeedId(reqVO.getFeaturedDeedId());
        exists.setFeaturedAncestorDeedId(reqVO.getFeaturedAncestorDeedId());
        exists.setWallApplicationIds(reqVO.getWallApplicationIds());
        exists.setCalendarMemberIds(reqVO.getCalendarMemberIds());
        showcaseConfigMapper.updateById(exists);
    }

    public ShowcaseHomeRespVO home() {
        ShowcaseConfigDO cfg = getConfig();
        List<MemberDO> members = memberMapper.selectListByFamilyId(FAMILY_ID);
        ShowcaseHomeRespVO vo = new ShowcaseHomeRespVO();
        vo.setMemberTotal(members.size());
        vo.setGallery(buildGallery(members, 12));
        vo.setGalleryTotal(countPhotos(members));
        vo.setFeaturedPerson(pickFeaturedPerson(cfg, members, vo.getGallery()));
        vo.setFeaturedDeeds(buildDeeds(cfg, 8));
        vo.setDeedTotal(vo.getFeaturedDeeds().size());
        vo.setScholarshipWall(buildWall(cfg, 12));
        vo.setCalendar(buildCalendar(cfg, members, LocalDate.now(), 8));
        vo.setFeeds(buildFeeds(10));
        return vo;
    }

    public List<ShowcaseHomeRespVO.GalleryItem> gallery(int limit) {
        List<MemberDO> members = memberMapper.selectListByFamilyId(FAMILY_ID);
        return buildGallery(members, limit <= 0 ? 60 : Math.min(limit, 120));
    }

    public List<ShowcaseHomeRespVO.CalendarItem> calendar(Integer year, Integer month) {
        LocalDate base = LocalDate.now();
        if (year != null && month != null) {
            base = LocalDate.of(year, month, 1);
        }
        ShowcaseConfigDO cfg = getConfig();
        List<MemberDO> members = memberMapper.selectListByFamilyId(FAMILY_ID);
        return buildMonthCalendar(cfg, members, base.getYear(), base.getMonthValue());
    }

    public List<ShowcaseHomeRespVO.DeedCard> deeds() {
        return buildDeeds(getConfig(), 30);
    }

    public List<ShowcaseHomeRespVO.WallItem> scholarshipWall() {
        return buildWall(getConfig(), 40);
    }

    public List<MemberSimpleVO> search(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        String kw = keyword.trim();
        Integer genNo = parseGenerationNo(kw);
        Set<Long> genIds = new HashSet<>();
        if (genNo == null) {
            List<GenerationDO> gens = generationMapper.selectList(new LambdaQueryWrapperX<GenerationDO>()
                    .like(GenerationDO::getWord, kw));
            gens.forEach(g -> genIds.add(g.getId()));
        }
        List<MemberDO> list = memberMapper.selectList(new LambdaQueryWrapperX<MemberDO>()
                .eq(MemberDO::getFamilyId, FAMILY_ID)
                .and(w -> {
                    w.like(MemberDO::getName, kw);
                    if (genNo != null) {
                        w.or().eq(MemberDO::getGenerationNo, genNo);
                    }
                    if (!genIds.isEmpty()) {
                        w.or().in(MemberDO::getGenerationId, genIds);
                    }
                })
                .last("LIMIT 30"));
        return list.stream().map(this::toSimple).collect(Collectors.toList());
    }

    public MiniFamilyVO miniFamily(Long memberId) {
        MiniFamilyVO vo = new MiniFamilyVO();
        if (memberId == null) {
            return vo;
        }
        MemberDO self = memberMapper.selectById(memberId);
        if (self == null || !FAMILY_ID.equals(self.getFamilyId())) {
            return vo;
        }
        vo.setSelf(toSimple(self));
        List<MemberSimpleVO> ancestors = new ArrayList<>();
        Long cursor = self.getFatherId();
        for (int i = 0; i < 3 && cursor != null; i++) {
            MemberDO a = memberMapper.selectById(cursor);
            if (a == null) {
                break;
            }
            ancestors.add(0, toSimple(a));
            cursor = a.getFatherId();
        }
        vo.setAncestors(ancestors);
        if (self.getMotherId() != null) {
            MemberDO mother = memberMapper.selectById(self.getMotherId());
            if (mother != null) {
                vo.setMother(toSimple(mother));
            }
        }
        if (CollUtil.isNotEmpty(self.getSpouseIds())) {
            for (Long sid : self.getSpouseIds()) {
                MemberDO s = memberMapper.selectById(sid);
                if (s != null) {
                    vo.getSpouses().add(toSimple(s));
                }
            }
        }
        if (self.getFatherId() != null) {
            vo.setSiblings(memberMapper.selectByFatherId(self.getFatherId()).stream()
                    .filter(s -> !s.getId().equals(self.getId()))
                    .map(this::toSimple)
                    .collect(Collectors.toList()));
        }
        List<MemberDO> children = mergeChildren(self.getId());
        vo.setChildren(children.stream().map(this::toSimple).collect(Collectors.toList()));
        List<MemberSimpleVO> grand = new ArrayList<>();
        for (MemberDO c : children) {
            for (MemberDO g : mergeChildren(c.getId())) {
                grand.add(toSimple(g));
            }
        }
        vo.setGrandchildren(grand);
        return vo;
    }

    public List<RelationHopVO> relation(Long fromId, Long toId) {
        if (fromId == null || toId == null) {
            return Collections.emptyList();
        }
        List<MemberDO> members = memberMapper.selectListByFamilyId(FAMILY_ID);
        Map<Long, MemberDO> byId = members.stream().collect(Collectors.toMap(MemberDO::getId, m -> m, (a, b) -> a));
        if (!byId.containsKey(fromId) || !byId.containsKey(toId)) {
            return Collections.emptyList();
        }
        if (fromId.equals(toId)) {
            return List.of(hop(byId.get(fromId), "本人"));
        }
        Map<Long, List<long[]>> graph = new HashMap<>();
        for (MemberDO m : members) {
            if (m.getFatherId() != null) {
                addEdge(graph, m.getId(), m.getFatherId(), 1);
                addEdge(graph, m.getFatherId(), m.getId(), 2);
            }
            if (m.getMotherId() != null) {
                addEdge(graph, m.getId(), m.getMotherId(), 3);
                addEdge(graph, m.getMotherId(), m.getId(), 2);
            }
            if (CollUtil.isNotEmpty(m.getSpouseIds())) {
                for (Long sid : m.getSpouseIds()) {
                    addEdge(graph, m.getId(), sid, 4);
                    addEdge(graph, sid, m.getId(), 4);
                }
            }
        }
        Map<Long, Long> prev = new HashMap<>();
        Map<Long, Integer> edge = new HashMap<>();
        ArrayDeque<Long> q = new ArrayDeque<>();
        q.add(fromId);
        prev.put(fromId, -1L);
        while (!q.isEmpty()) {
            Long cur = q.poll();
            if (cur.equals(toId)) {
                break;
            }
            for (long[] e : graph.getOrDefault(cur, Collections.emptyList())) {
                Long nxt = e[0];
                if (prev.containsKey(nxt)) {
                    continue;
                }
                prev.put(nxt, cur);
                edge.put(nxt, (int) e[1]);
                q.add(nxt);
            }
        }
        if (!prev.containsKey(toId)) {
            return Collections.emptyList();
        }
        LinkedList<RelationHopVO> path = new LinkedList<>();
        Long cur = toId;
        while (cur != null && cur != -1L) {
            MemberDO m = byId.get(cur);
            String label = cur.equals(fromId) ? "起点" : edgeLabel(edge.get(cur), m);
            path.addFirst(hop(m, label));
            Long p = prev.get(cur);
            if (p == null || p == -1L) {
                break;
            }
            cur = p;
        }
        if (!path.isEmpty()) {
            path.getFirst().setEdge("起点");
        }
        return path;
    }

    private void addEdge(Map<Long, List<long[]>> graph, Long from, Long to, int type) {
        if (from == null || to == null) {
            return;
        }
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new long[]{to, type});
    }

    private String edgeLabel(Integer type, MemberDO target) {
        if (type == null) {
            return "";
        }
        if (type == 1) {
            return "父";
        }
        if (type == 3) {
            return "母";
        }
        if (type == 4) {
            return "配偶";
        }
        if (target != null && target.getGender() != null && target.getGender() == 2) {
            return "女";
        }
        return "子";
    }

    private RelationHopVO hop(MemberDO m, String edge) {
        RelationHopVO vo = new RelationHopVO();
        if (m != null) {
            vo.setMemberId(m.getId());
            vo.setName(m.getName());
            vo.setAvatar(m.getAvatar());
        }
        vo.setEdge(edge);
        return vo;
    }

    private ShowcaseHomeRespVO.PersonCard pickFeaturedPerson(ShowcaseConfigDO cfg, List<MemberDO> members,
                                                            List<ShowcaseHomeRespVO.GalleryItem> gallery) {
        MemberDO picked = null;
        String source = "自动选取";
        if (cfg.getFeaturedMemberId() != null) {
            picked = memberMapper.selectById(cfg.getFeaturedMemberId());
            source = "管理员置顶";
        }
        if (picked == null) {
            picked = members.stream()
                    .filter(m -> CollUtil.isNotEmpty(m.getPhotoUrls()) || StrUtil.isNotBlank(m.getIntro()) || StrUtil.isNotBlank(m.getAvatar()))
                    .max(Comparator.comparingInt(m -> (m.getPhotoUrls() == null ? 0 : m.getPhotoUrls().size())
                            + (StrUtil.isNotBlank(m.getIntro()) ? 2 : 0)))
                    .orElse(members.isEmpty() ? null : members.get(0));
        }
        if (picked == null) {
            return null;
        }
        ShowcaseHomeRespVO.PersonCard card = new ShowcaseHomeRespVO.PersonCard();
        card.setId(picked.getId());
        card.setName(picked.getName());
        card.setAvatar(picked.getAvatar());
        card.setIntro(StrUtil.maxLength(picked.getIntro(), 80));
        card.setGenerationNo(picked.getGenerationNo());
        card.setLifeSpan(lifeSpan(picked));
        card.setSource(source);
        if (picked.getGenerationId() != null) {
            GenerationDO gen = generationMapper.selectById(picked.getGenerationId());
            if (gen != null) {
                card.setGenerationWord(gen.getWord());
            }
        }
        if (CollUtil.isNotEmpty(picked.getPhotoUrls())) {
            card.setPhoto(picked.getPhotoUrls().get(0));
        } else if (!gallery.isEmpty()) {
            card.setPhoto(gallery.get(0).getUrl());
        }
        return card;
    }

    private List<ShowcaseHomeRespVO.GalleryItem> buildGallery(List<MemberDO> members, int limit) {
        List<ShowcaseHomeRespVO.GalleryItem> list = new ArrayList<>();
        for (MemberDO m : members) {
            if (StrUtil.isNotBlank(m.getAvatar())) {
                ShowcaseHomeRespVO.GalleryItem item = new ShowcaseHomeRespVO.GalleryItem();
                item.setUrl(m.getAvatar());
                item.setMemberId(m.getId());
                item.setMemberName(m.getName());
                list.add(item);
            }
            if (CollUtil.isEmpty(m.getPhotoUrls())) {
                continue;
            }
            for (String url : m.getPhotoUrls()) {
                if (StrUtil.isBlank(url)) {
                    continue;
                }
                ShowcaseHomeRespVO.GalleryItem item = new ShowcaseHomeRespVO.GalleryItem();
                item.setUrl(url);
                item.setMemberId(m.getId());
                item.setMemberName(m.getName());
                list.add(item);
                if (list.size() >= limit) {
                    return list;
                }
            }
        }
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    private long countPhotos(List<MemberDO> members) {
        long n = 0;
        for (MemberDO m : members) {
            if (StrUtil.isNotBlank(m.getAvatar())) {
                n++;
            }
            if (CollUtil.isNotEmpty(m.getPhotoUrls())) {
                n += m.getPhotoUrls().size();
            }
        }
        return n;
    }

    private List<ShowcaseHomeRespVO.DeedCard> buildDeeds(ShowcaseConfigDO cfg, int limit) {
        List<ShowcaseHomeRespVO.DeedCard> list = new ArrayList<>();
        if (cfg.getFeaturedDeedId() != null) {
            MemberDeedDO d = memberDeedMapper.selectById(cfg.getFeaturedDeedId());
            if (d != null) {
                list.add(toDeedCard(d, true));
            }
        }
        if (cfg.getFeaturedAncestorDeedId() != null) {
            AncestorDeedDO d = ancestorDeedMapper.selectById(cfg.getFeaturedAncestorDeedId());
            if (d != null) {
                list.add(toAncestorCard(d, true));
            }
        }
        List<MemberDeedDO> deeds = memberDeedMapper.selectList(new LambdaQueryWrapperX<MemberDeedDO>()
                .orderByDesc(MemberDeedDO::getId)
                .last("LIMIT 20"));
        for (MemberDeedDO d : deeds) {
            if (list.stream().anyMatch(x -> "member".equals(x.getKind()) && d.getId().equals(x.getId()))) {
                continue;
            }
            list.add(toDeedCard(d, false));
            if (list.size() >= limit) {
                return list;
            }
        }
        List<AncestorDeedDO> ancestors = ancestorDeedMapper.selectList(new LambdaQueryWrapperX<AncestorDeedDO>()
                .eq(AncestorDeedDO::getFamilyId, FAMILY_ID)
                .orderByDesc(AncestorDeedDO::getId)
                .last("LIMIT 20"));
        for (AncestorDeedDO d : ancestors) {
            if (list.stream().anyMatch(x -> "ancestor".equals(x.getKind()) && d.getId().equals(x.getId()))) {
                continue;
            }
            list.add(toAncestorCard(d, false));
            if (list.size() >= limit) {
                break;
            }
        }
        return list;
    }

    private ShowcaseHomeRespVO.DeedCard toDeedCard(MemberDeedDO d, boolean pinned) {
        ShowcaseHomeRespVO.DeedCard card = new ShowcaseHomeRespVO.DeedCard();
        card.setId(d.getId());
        card.setKind("member");
        card.setMemberId(d.getMemberId());
        card.setTitle(d.getTitle());
        card.setContent(StrUtil.maxLength(d.getContent(), 80));
        card.setOccurYear(d.getOccurYear());
        card.setPinned(pinned);
        if (d.getMemberId() != null) {
            MemberDO m = memberMapper.selectById(d.getMemberId());
            if (m != null) {
                card.setName(m.getName());
                if (CollUtil.isNotEmpty(m.getPhotoUrls())) {
                    card.setCoverUrl(m.getPhotoUrls().get(0));
                } else {
                    card.setCoverUrl(m.getAvatar());
                }
            }
        }
        return card;
    }

    private ShowcaseHomeRespVO.DeedCard toAncestorCard(AncestorDeedDO d, boolean pinned) {
        ShowcaseHomeRespVO.DeedCard card = new ShowcaseHomeRespVO.DeedCard();
        card.setId(d.getId());
        card.setKind("ancestor");
        card.setMemberId(d.getMemberId());
        card.setName(d.getName());
        card.setTitle(d.getTitle());
        card.setContent(StrUtil.maxLength(d.getContent(), 80));
        card.setCoverUrl(d.getCoverUrl());
        card.setPinned(pinned);
        return card;
    }

    private List<ShowcaseHomeRespVO.WallItem> buildWall(ShowcaseConfigDO cfg, int limit) {
        List<ScholarshipApplicationDO> apps;
        if (CollUtil.isNotEmpty(cfg.getWallApplicationIds())) {
            apps = applicationMapper.selectBatchIds(cfg.getWallApplicationIds());
        } else {
            apps = applicationMapper.selectList(new LambdaQueryWrapperX<ScholarshipApplicationDO>()
                    .eq(ScholarshipApplicationDO::getFamilyId, FAMILY_ID)
                    .in(ScholarshipApplicationDO::getStatus,
                            ScholarshipStatusEnum.PENDING_DISBURSE.getStatus(),
                            ScholarshipStatusEnum.DISBURSED.getStatus())
                    .orderByDesc(ScholarshipApplicationDO::getYear)
                    .orderByDesc(ScholarshipApplicationDO::getId)
                    .last("LIMIT " + limit));
        }
        List<ShowcaseHomeRespVO.WallItem> list = new ArrayList<>();
        for (ScholarshipApplicationDO a : apps) {
            ShowcaseHomeRespVO.WallItem item = new ShowcaseHomeRespVO.WallItem();
            item.setApplicationId(a.getId());
            item.setMemberId(a.getMemberId());
            item.setSchool(a.getSchool());
            item.setMajor(a.getMajor());
            item.setYear(a.getYear());
            item.setGrade(a.getGrade());
            if (a.getMemberId() != null) {
                MemberDO m = memberMapper.selectById(a.getMemberId());
                if (m != null) {
                    item.setName(m.getName());
                }
            }
            if (StrUtil.isBlank(item.getName())) {
                continue;
            }
            list.add(item);
            if (list.size() >= limit) {
                break;
            }
        }
        return list;
    }

    private List<ShowcaseHomeRespVO.FeedCard> buildFeeds(int limit) {
        List<FeedDO> rows = feedMapper.selectList(new LambdaQueryWrapperX<FeedDO>()
                .eq(FeedDO::getFamilyId, FAMILY_ID)
                .eq(FeedDO::getStatus, 1)
                .orderByDesc(FeedDO::getPinned)
                .orderByDesc(FeedDO::getId)
                .last("LIMIT " + limit));
        List<ShowcaseHomeRespVO.FeedCard> list = new ArrayList<>();
        for (FeedDO f : rows) {
            ShowcaseHomeRespVO.FeedCard c = new ShowcaseHomeRespVO.FeedCard();
            c.setId(f.getId());
            c.setType(f.getType());
            c.setTitle(f.getTitle());
            c.setContent(StrUtil.maxLength(f.getContent(), 80));
            c.setImages(f.getImages());
            c.setAuthorName(f.getAuthorName());
            c.setLikeCount(f.getLikeCount());
            c.setCommentCount(f.getCommentCount());
            c.setPinned(f.getPinned());
            list.add(c);
        }
        return list;
    }

    private List<ShowcaseHomeRespVO.CalendarItem> buildCalendar(ShowcaseConfigDO cfg, List<MemberDO> members, LocalDate from, int limit) {
        List<ActivityDO> acts = loadActivities();
        List<ScholarshipConfigDO> windows = loadScholarshipWindows();
        List<MemberDO> shown = calendarMembers(cfg, members);
        List<ShowcaseHomeRespVO.CalendarItem> all = new ArrayList<>();
        for (int i = 0; i < 366 && all.size() < 80; i++) {
            LocalDate day = from.plusDays(i);
            all.addAll(dayItems(shown, acts, windows, day));
        }
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    private List<ShowcaseHomeRespVO.CalendarItem> buildMonthCalendar(ShowcaseConfigDO cfg, List<MemberDO> members, int year, int month) {
        List<ActivityDO> acts = loadActivities();
        List<ScholarshipConfigDO> windows = loadScholarshipWindows();
        List<MemberDO> shown = calendarMembers(cfg, members);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        List<ShowcaseHomeRespVO.CalendarItem> list = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            list.addAll(dayItems(shown, acts, windows, d));
        }
        list.sort(Comparator.comparing(ShowcaseHomeRespVO.CalendarItem::getDate));
        return list;
    }

    private List<ActivityDO> loadActivities() {
        return activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                .eq(ActivityDO::getFamilyId, FAMILY_ID)
                .ne(ActivityDO::getStatus, 3));
    }

    private List<ScholarshipConfigDO> loadScholarshipWindows() {
        return scholarshipConfigMapper.selectList(new LambdaQueryWrapperX<ScholarshipConfigDO>()
                .eq(ScholarshipConfigDO::getFamilyId, FAMILY_ID));
    }

    private List<ShowcaseHomeRespVO.CalendarItem> dayItems(List<MemberDO> members, List<ActivityDO> acts,
                                                         List<ScholarshipConfigDO> windows, LocalDate day) {
        List<ShowcaseHomeRespVO.CalendarItem> list = new ArrayList<>();
        int md = day.getMonthValue() * 100 + day.getDayOfMonth();
        if (day.getMonthValue() == 4 && day.getDayOfMonth() == 5) {
            list.add(cal("festival", "清明", day, "慎终追远", null, "/portal/ancestor"));
        }
        for (MemberDO m : members) {
            if (usableCivilDate(m.getBirthDate()) && monthDay(m.getBirthDate().toLocalDate()) == md
                    && (m.getAlive() == null || Boolean.TRUE.equals(m.getAlive()))) {
                list.add(cal("birthday", m.getName() + " 寿辰", day, "家族同贺", m.getId(), "/portal/member?id=" + m.getId()));
            }
            if (usableCivilDate(m.getDeathDate()) && monthDay(m.getDeathDate().toLocalDate()) == md
                    && Boolean.FALSE.equals(m.getAlive())) {
                list.add(cal("memorial", m.getName() + " 忌日", day, "缅怀先人", m.getId(), "/portal/member?id=" + m.getId()));
            }
        }
        for (ActivityDO a : acts) {
            if (a.getStartTime() != null && a.getStartTime().toLocalDate().equals(day)) {
                list.add(cal("activity", a.getTitle(), day, a.getPlace(), a.getId(), "/portal/activity?id=" + a.getId()));
            }
        }
        for (ScholarshipConfigDO cfg : windows) {
            if (cfg.getWindowStart() != null && cfg.getWindowStart().toLocalDate().equals(day)) {
                list.add(cal("scholarship", cfg.getYear() + "年资助申请开放", day, "学海无涯", null, "/portal/scholarship"));
            }
        }
        return list;
    }

    private int monthDay(LocalDate d) {
        return d.getMonthValue() * 100 + d.getDayOfMonth();
    }

    private ShowcaseHomeRespVO.CalendarItem cal(String type, String title, LocalDate date, String remark, Long refId, String link) {
        ShowcaseHomeRespVO.CalendarItem item = new ShowcaseHomeRespVO.CalendarItem();
        item.setType(type);
        item.setTitle(title);
        item.setDate(date);
        item.setRemark(remark);
        item.setRefId(refId);
        item.setLink(link);
        return item;
    }

    private List<MemberDO> mergeChildren(Long memberId) {
        LinkedHashMap<Long, MemberDO> map = new LinkedHashMap<>();
        for (MemberDO c : memberMapper.selectByFatherId(memberId)) {
            map.put(c.getId(), c);
        }
        for (MemberDO c : memberMapper.selectByMotherId(memberId)) {
            map.putIfAbsent(c.getId(), c);
        }
        return new ArrayList<>(map.values());
    }

    private List<MemberDO> calendarMembers(ShowcaseConfigDO cfg, List<MemberDO> all) {
        if (cfg == null || CollUtil.isEmpty(cfg.getCalendarMemberIds())) {
            return Collections.emptyList();
        }
        Set<Long> ids = new HashSet<>(cfg.getCalendarMemberIds());
        List<MemberDO> list = new ArrayList<>();
        for (MemberDO m : all) {
            if (ids.contains(m.getId())) {
                list.add(m);
            }
        }
        return list;
    }

    /** 公历可映射月日。公元前无法用公历月日展示，即使勾选也不进入日历。 */
    private boolean usableCivilDate(java.time.LocalDateTime t) {
        return t != null && t.getYear() >= 100;
    }

    private MemberSimpleVO toSimple(MemberDO m) {
        MemberSimpleVO vo = new MemberSimpleVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setGender(m.getGender());
        vo.setGenerationNo(m.getGenerationNo());
        vo.setAvatar(m.getAvatar());
        vo.setAlive(m.getAlive());
        if (m.getGenerationId() != null) {
            GenerationDO gen = generationMapper.selectById(m.getGenerationId());
            if (gen != null) {
                vo.setGenerationWord(gen.getWord());
                vo.setGenerationHouse(gen.getHouse());
            }
        }
        return vo;
    }

    private Integer parseGenerationNo(String kw) {
        if (kw.endsWith("世")) {
            try {
                return Integer.parseInt(kw.substring(0, kw.length() - 1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String lifeSpan(MemberDO m) {
        String birth = m.getBirthDate() == null ? "?" : String.valueOf(m.getBirthDate().getYear());
        if (Boolean.FALSE.equals(m.getAlive())) {
            String death = m.getDeathDate() == null ? "?" : String.valueOf(m.getDeathDate().getYear());
            return birth + " — " + death;
        }
        return birth + " — 今";
    }
}
