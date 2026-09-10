package chen.genealogy.module.genealogy.service.book;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.book.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDeedDO;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.service.content.ContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.BOOK_PAGE_NOT_EXISTS;

/**
 * 将已有家族、字辈、成员数据编排为可翻页的谱书，不另存正文。
 */
@Service
@Validated
public class BookComposeService {

    public static final String COVER = "COVER";
    public static final String PREFACE = "PREFACE";
    public static final String GENERATION = "GENERATION";
    public static final String LINEAGE = "LINEAGE";

    private static final Long FAMILY_ID = 1L;
    private static final int PAGE_SIZE = 3;
    private static final Pattern GEN_NO = Pattern.compile("^(\\d+)\\s*世?$");

    @Resource
    private ContentService contentService;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    private MemberDeedMapper memberDeedMapper;

    public BookMetaRespVO getMeta() {
        ComposedBook book = compose();
        FamilyDO family = book.family;
        BookMetaRespVO vo = new BookMetaRespVO();
        vo.setBookTitle(resolveTitle(family));
        vo.setFamilyName(family.getName());
        vo.setSurname(family.getSurname());
        vo.setAncestorName(family.getAncestorName());
        vo.setRegion(family.getRegion());
        vo.setLogo(family.getLogo());
        vo.setBookRevision(family.getBookRevision());
        vo.setPreface(family.getBookPreface());
        vo.setTotalPages(book.pages.size());
        vo.setFirstLineagePage(book.firstLineagePage);
        vo.setToc(book.toc);
        vo.setGenerations(book.generationIndex);
        return vo;
    }

    public BookPageRespVO getPage(Integer pageNo, boolean loggedIn) {
        ComposedBook book = compose();
        if (pageNo == null || pageNo < 1 || pageNo > book.pages.size()) {
            throw exception(BOOK_PAGE_NOT_EXISTS);
        }
        BookPageRespVO page = copyPage(book.pages.get(pageNo - 1));
        page.setTotalPages(book.pages.size());
        if (LINEAGE.equals(page.getType()) && !loggedIn) {
            page.setLoginRequired(true);
            page.setBranches(Collections.emptyList());
        } else {
            page.setLoginRequired(false);
        }
        return page;
    }

    public BookSearchRespVO search(String keyword, boolean loggedIn) {
        BookSearchRespVO resp = new BookSearchRespVO();
        if (!loggedIn) {
            resp.setLoginRequired(true);
            return resp;
        }
        resp.setLoginRequired(false);
        String kw = StrUtil.trim(keyword);
        if (StrUtil.isBlank(kw)) {
            return resp;
        }
        ComposedBook book = compose();
        Matcher genMatcher = GEN_NO.matcher(kw);
        if (genMatcher.matches()) {
            int no = Integer.parseInt(genMatcher.group(1));
            addGenerationHit(resp, book, no);
            addMembersOfGeneration(resp, book, no);
            return resp;
        }
        for (BookGenerationIndexVO g : book.generationIndex) {
            if (StrUtil.splitTrim(g.getWords(), '、').contains(kw) || StrUtil.equals(g.getWords(), kw)) {
                addGenerationHit(resp, book, g.getGenerationNo());
                addMembersOfGeneration(resp, book, g.getGenerationNo());
                addMembersByWord(resp, book, kw);
                return resp;
            }
        }
        for (MemberDO m : book.lineageMembers) {
            if (m.getName() != null && m.getName().contains(kw)) {
                resp.getList().add(toHit(book, m));
            }
        }
        return resp;
    }

    private void addGenerationHit(BookSearchRespVO resp, ComposedBook book, int generationNo) {
        BookGenerationIndexVO g = book.generationIndex.stream()
                .filter(i -> Objects.equals(i.getGenerationNo(), generationNo))
                .findFirst().orElse(null);
        if (g == null) {
            return;
        }
        BookSearchHitVO hit = new BookSearchHitVO();
        hit.setType(LINEAGE);
        hit.setGenerationNo(generationNo);
        hit.setGenerationWord(g.getWords());
        hit.setPageNo(g.getPageNo());
        hit.setTitle(chineseGen(generationNo) + "（" + StrUtil.blankToDefault(g.getWords(), "未定") + "字辈）");
        resp.getList().add(hit);
    }

    private void addMembersOfGeneration(BookSearchRespVO resp, ComposedBook book, int generationNo) {
        for (MemberDO m : book.lineageMembers) {
            if (Objects.equals(m.getGenerationNo(), generationNo)) {
                resp.getList().add(toHit(book, m));
            }
        }
    }

    private void addMembersByWord(BookSearchRespVO resp, ComposedBook book, String word) {
        Set<Long> seen = resp.getList().stream()
                .map(BookSearchHitVO::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (MemberDO m : book.lineageMembers) {
            if (seen.contains(m.getId())) {
                continue;
            }
            String w = wordOf(book, m);
            if (word.equals(w)) {
                resp.getList().add(toHit(book, m));
            }
        }
    }

    private BookSearchHitVO toHit(ComposedBook book, MemberDO m) {
        BookSearchHitVO hit = new BookSearchHitVO();
        hit.setMemberId(m.getId());
        hit.setName(m.getName());
        hit.setGenerationNo(m.getGenerationNo());
        hit.setGenerationWord(wordOf(book, m));
        MemberDO father = m.getFatherId() == null ? null : book.byId.get(m.getFatherId());
        hit.setFatherName(father == null ? null : father.getName());
        hit.setPageNo(book.memberPage.getOrDefault(m.getId(), book.firstLineagePage));
        hit.setType(LINEAGE);
        hit.setTitle(m.getName());
        return hit;
    }

    private ComposedBook compose() {
        FamilyDO family = contentService.getFamily();
        List<GenerationDO> gens = contentService.getGenerationList();
        List<MemberDO> all = memberMapper.selectListByFamilyId(FAMILY_ID);
        ComposedBook book = new ComposedBook();
        book.family = family;
        book.byId = all.stream().collect(Collectors.toMap(MemberDO::getId, m -> m, (a, b) -> a));
        book.wordByGenId = gens.stream()
                .filter(g -> g.getId() != null && StrUtil.isNotBlank(g.getWord()))
                .collect(Collectors.toMap(GenerationDO::getId, GenerationDO::getWord, (a, b) -> a));
        Map<Integer, List<String>> wordsByNo = new TreeMap<>();
        for (GenerationDO g : gens) {
            if (g.getGenerationNo() == null || StrUtil.isBlank(g.getWord())) {
                continue;
            }
            wordsByNo.computeIfAbsent(g.getGenerationNo(), k -> new ArrayList<>()).add(g.getWord());
        }
        wordsByNo.forEach((no, words) -> book.genWords.put(no, words.stream().distinct().collect(Collectors.joining("、"))));

        Set<Long> parentIds = all.stream().map(MemberDO::getFatherId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> listedAsSpouse = new HashSet<>();
        for (MemberDO m : all) {
            if (CollUtil.isNotEmpty(m.getSpouseIds())) {
                listedAsSpouse.addAll(m.getSpouseIds());
            }
        }
        book.lineageMembers = all.stream()
                .filter(m -> isLineage(m, family, parentIds, listedAsSpouse))
                .collect(Collectors.toList());
        for (GenerationDO g : gens) {
            if (g.getId() != null) {
                int order = g.getSort() != null ? g.getSort() : (g.getId().intValue());
                book.wordOrder.put(g.getId(), order);
            }
        }
        Set<Long> lineageIds = book.lineageMembers.stream().map(MemberDO::getId).collect(Collectors.toSet());
        fillDeeds(book, lineageIds);

        Map<Long, List<MemberDO>> children = new HashMap<>();
        for (MemberDO m : book.lineageMembers) {
            if (m.getFatherId() != null && lineageIds.contains(m.getFatherId())) {
                children.computeIfAbsent(m.getFatherId(), k -> new ArrayList<>()).add(m);
            }
        }
        children.values().forEach(list -> list.sort(this::compareSibling));

        List<MemberDO> roots = book.lineageMembers.stream()
                .filter(m -> m.getFatherId() == null || !lineageIds.contains(m.getFatherId()))
                .sorted(this::compareSibling)
                .collect(Collectors.toList());
        List<MemberDO> dfs = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        for (MemberDO root : roots) {
            walk(root, children, visited, dfs);
        }
        for (MemberDO m : book.lineageMembers) {
            if (!visited.contains(m.getId())) {
                dfs.add(m);
            }
        }
        Map<Long, Integer> dfsIndex = new HashMap<>();
        for (int i = 0; i < dfs.size(); i++) {
            dfsIndex.put(dfs.get(i).getId(), i);
        }
        book.lineageMembers.sort(Comparator
                .comparing((MemberDO m) -> m.getGenerationNo() == null ? Integer.MAX_VALUE : m.getGenerationNo())
                .thenComparing(m -> book.wordOrder.getOrDefault(m.getGenerationId(), Integer.MAX_VALUE))
                .thenComparing(m -> dfsIndex.getOrDefault(m.getId(), Integer.MAX_VALUE)));

        Map<Integer, List<MemberDO>> byGen = new LinkedHashMap<>();
        for (MemberDO m : book.lineageMembers) {
            int no = m.getGenerationNo() == null ? 0 : m.getGenerationNo();
            byGen.computeIfAbsent(no, k -> new ArrayList<>()).add(m);
        }

        List<BookGenerationRowVO> genRows = buildGenerationRows(gens);
        addFrontMatter(book, family, genRows);
        paginateLineage(book, byGen);
        fillGenerationIndex(book, gens, byGen);
        return book;
    }

    private void addFrontMatter(ComposedBook book, FamilyDO family, List<BookGenerationRowVO> genRows) {
        BookPageRespVO cover = basePage(COVER, "封面", 1);
        cover.setBookTitle(resolveTitle(family));
        cover.setFamilyName(family.getName());
        cover.setSurname(family.getSurname());
        cover.setAncestorName(family.getAncestorName());
        cover.setRegion(family.getRegion());
        cover.setLogo(family.getLogo());
        cover.setBookRevision(family.getBookRevision());
        book.pages.add(cover);
        book.toc.add(toc(1, COVER, "封面", null));

        BookPageRespVO preface = basePage(PREFACE, "前言", 2);
        preface.setHtml(StrUtil.blankToDefault(family.getBookPreface(), "<p>谱序待修。</p>"));
        preface.setBookTitle(resolveTitle(family));
        book.pages.add(preface);
        book.toc.add(toc(2, PREFACE, "前言", null));

        BookPageRespVO gen = basePage(GENERATION, "字辈派语", 3);
        gen.setGenerationRows(genRows);
        book.pages.add(gen);
        book.toc.add(toc(3, GENERATION, "字辈派语", null));
        book.generationTablePage = 3;
    }

    private void paginateLineage(ComposedBook book, Map<Integer, List<MemberDO>> byGen) {
        for (Map.Entry<Integer, List<MemberDO>> e : byGen.entrySet()) {
            int genNo = e.getKey();
            List<List<MemberDO>> branches = groupBranches(book, e.getValue());
            boolean started = false;
            List<BookBranchVO> buf = new ArrayList<>();
            int count = 0;
            for (List<MemberDO> branch : branches) {
                int offset = 0;
                while (offset < branch.size()) {
                    int room = PAGE_SIZE - count;
                    if (room <= 0) {
                        flushLineage(book, genNo, buf, started);
                        buf = new ArrayList<>();
                        count = 0;
                        started = true;
                        room = PAGE_SIZE;
                    }
                    int take = Math.min(room, branch.size() - offset);
                    BookBranchVO vo = toBranch(book, branch.subList(offset, offset + take), offset > 0);
                    buf.add(vo);
                    for (MemberDO m : branch.subList(offset, offset + take)) {
                        book.memberPage.put(m.getId(), book.pages.size() + 1);
                    }
                    count += take;
                    offset += take;
                    if (count >= PAGE_SIZE) {
                        flushLineage(book, genNo, buf, started);
                        buf = new ArrayList<>();
                        count = 0;
                        started = true;
                    }
                }
            }
            if (!buf.isEmpty() || !started) {
                if (!buf.isEmpty()) {
                    flushLineage(book, genNo, buf, started);
                }
            }
        }
        book.firstLineagePage = book.pages.stream()
                .filter(p -> LINEAGE.equals(p.getType()))
                .map(BookPageRespVO::getPageNo)
                .findFirst().orElse(book.pages.size());
    }

    private void flushLineage(ComposedBook book, int genNo, List<BookBranchVO> branches, boolean continued) {
        int pageNo = book.pages.size() + 1;
        String title = chineseGen(genNo) + (continued ? "（续）" : "");
        BookPageRespVO page = basePage(LINEAGE, title, pageNo);
        page.setGenerationNo(genNo == 0 ? null : genNo);
        page.setGenerationWords(wordsOfGen(book, genNo));
        page.setBranches(new ArrayList<>(branches));
        book.pages.add(page);
        if (!continued) {
            book.toc.add(toc(pageNo, LINEAGE, chineseGen(genNo), genNo == 0 ? null : genNo));
            book.genFirstPage.putIfAbsent(genNo, pageNo);
        }
    }

    private List<List<MemberDO>> groupBranches(ComposedBook book, List<MemberDO> genMembers) {
        Map<String, List<MemberDO>> map = new LinkedHashMap<>();
        for (MemberDO m : genMembers) {
            String word = StrUtil.blankToDefault(wordOf(book, m), "");
            String fid = m.getFatherId() == null ? "_" : String.valueOf(m.getFatherId());
            map.computeIfAbsent(word + "\0" + fid, k -> new ArrayList<>()).add(m);
        }
        return new ArrayList<>(map.values());
    }

    private BookBranchVO toBranch(ComposedBook book, List<MemberDO> members, boolean continued) {
        MemberDO first = members.get(0);
        MemberDO father = first.getFatherId() == null ? null : book.byId.get(first.getFatherId());
        BookBranchVO vo = new BookBranchVO();
        vo.setFatherId(first.getFatherId());
        vo.setFatherName(father == null ? null : father.getName());
        String word = wordOf(book, first);
        vo.setGenerationWord(word);
        String prefix = StrUtil.isNotBlank(word) ? word + "字辈 · " : "";
        if (father == null) {
            vo.setTitle(prefix + (continued ? "本支始迁（续）" : "本支始迁"));
        } else {
            vo.setTitle(prefix + father.getName() + (continued ? "之子（续）" : "之子"));
        }
        vo.setMembers(members.stream().map(m -> toMember(book, m)).collect(Collectors.toList()));
        return vo;
    }

    private BookMemberVO toMember(ComposedBook book, MemberDO m) {
        BookMemberVO vo = new BookMemberVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setGender(m.getGender());
        vo.setGenerationNo(m.getGenerationNo());
        vo.setGenerationWord(wordOf(book, m));
        MemberDO father = m.getFatherId() == null ? null : book.byId.get(m.getFatherId());
        MemberDO mother = m.getMotherId() == null ? null : book.byId.get(m.getMotherId());
        vo.setFatherName(father == null ? null : father.getName());
        vo.setMotherName(mother == null ? null : mother.getName());
        vo.setSpouseNames(spouseNames(book, m));
        vo.setLifeSpan(lifeSpan(m));
        vo.setIntro(m.getIntro());
        vo.setAlive(m.getAlive());
        vo.setTags(m.getTags());
        vo.setPhotoUrls(photosOf(m));
        vo.setDeeds(book.deedsByMember.getOrDefault(m.getId(), Collections.emptyList()));
        return vo;
    }

    private List<String> photosOf(MemberDO m) {
        if (CollUtil.isEmpty(m.getPhotoUrls())) {
            return Collections.emptyList();
        }
        return m.getPhotoUrls().stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }

    private void fillDeeds(ComposedBook book, Set<Long> lineageIds) {
        if (CollUtil.isEmpty(lineageIds)) {
            return;
        }
        List<MemberDeedDO> deeds = memberDeedMapper.selectList(new LambdaQueryWrapperX<MemberDeedDO>()
                .in(MemberDeedDO::getMemberId, lineageIds)
                .orderByAsc(MemberDeedDO::getId));
        if (CollUtil.isEmpty(deeds)) {
            return;
        }
        for (MemberDeedDO d : deeds) {
            if (d.getMemberId() == null) {
                continue;
            }
            BookDeedVO vo = new BookDeedVO();
            vo.setId(d.getId());
            vo.setTitle(d.getTitle());
            vo.setContent(d.getContent());
            vo.setSource(d.getSource());
            vo.setOccurYear(d.getOccurYear());
            book.deedsByMember.computeIfAbsent(d.getMemberId(), k -> new ArrayList<>()).add(vo);
        }
    }

    private String spouseNames(ComposedBook book, MemberDO m) {
        if (CollUtil.isEmpty(m.getSpouseIds())) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Long id : m.getSpouseIds()) {
            MemberDO s = book.byId.get(id);
            if (s != null && StrUtil.isNotBlank(s.getName())) {
                names.add(s.getName());
            }
        }
        return names.isEmpty() ? null : String.join("、", names);
    }

    private String lifeSpan(MemberDO m) {
        String birth = yearOf(m.getBirthDate());
        boolean dead = Boolean.FALSE.equals(m.getAlive());
        String end = dead ? yearOf(m.getDeathDate()) : "今";
        if ("?".equals(birth) && "今".equals(end) && !dead) {
            return null;
        }
        return birth + "—" + end;
    }

    private String yearOf(LocalDateTime t) {
        return t == null ? "?" : String.valueOf(t.getYear());
    }

    private List<BookGenerationRowVO> buildGenerationRows(List<GenerationDO> gens) {
        Map<Integer, List<GenerationDO>> map = new TreeMap<>();
        for (GenerationDO g : gens) {
            if (g.getGenerationNo() == null) {
                continue;
            }
            map.computeIfAbsent(g.getGenerationNo(), k -> new ArrayList<>()).add(g);
        }
        List<BookGenerationRowVO> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<GenerationDO>> e : map.entrySet()) {
            BookGenerationRowVO row = new BookGenerationRowVO();
            row.setGenerationNo(e.getKey());
            row.setWords(e.getValue().stream().map(GenerationDO::getWord).filter(StrUtil::isNotBlank)
                    .distinct().collect(Collectors.joining("、")));
            row.setRemark(e.getValue().stream().map(GenerationDO::getRemark).filter(StrUtil::isNotBlank)
                    .collect(Collectors.joining("；")));
            rows.add(row);
        }
        return rows;
    }

    private void fillGenerationIndex(ComposedBook book, List<GenerationDO> gens, Map<Integer, List<MemberDO>> byGen) {
        Map<Integer, List<GenerationDO>> map = new TreeMap<>();
        for (GenerationDO g : gens) {
            if (g.getGenerationNo() == null) {
                continue;
            }
            map.computeIfAbsent(g.getGenerationNo(), k -> new ArrayList<>()).add(g);
        }
        Set<Integer> allNos = new TreeSet<>();
        allNos.addAll(map.keySet());
        allNos.addAll(byGen.keySet());
        for (Integer no : allNos) {
            BookGenerationIndexVO vo = new BookGenerationIndexVO();
            vo.setGenerationNo(no);
            List<GenerationDO> list = map.getOrDefault(no, Collections.emptyList());
            vo.setWords(list.stream().map(GenerationDO::getWord).filter(StrUtil::isNotBlank)
                    .distinct().collect(Collectors.joining("、")));
            vo.setMemberCount(byGen.getOrDefault(no, Collections.emptyList()).size());
            vo.setPageNo(book.genFirstPage.getOrDefault(no, book.generationTablePage));
            book.generationIndex.add(vo);
        }
    }

    private boolean isLineage(MemberDO m, FamilyDO family, Set<Long> parentIds, Set<Long> listedAsSpouse) {
        if (family.getAncestorId() != null && family.getAncestorId().equals(m.getId())) {
            return true;
        }
        if (m.getFatherId() != null) {
            return true;
        }
        if (parentIds.contains(m.getId())) {
            return true;
        }
        if (StrUtil.contains(m.getTags(), "始祖")) {
            return true;
        }
        // 并行字辈/旁支始迁祖：无父子关系，但不是他人配偶
        return !listedAsSpouse.contains(m.getId());
    }

    private void walk(MemberDO node, Map<Long, List<MemberDO>> children, Set<Long> visited, List<MemberDO> out) {
        if (node == null || !visited.add(node.getId())) {
            return;
        }
        out.add(node);
        for (MemberDO child : children.getOrDefault(node.getId(), Collections.emptyList())) {
            walk(child, children, visited, out);
        }
    }

    private int compareSibling(MemberDO a, MemberDO b) {
        LocalDateTime da = a.getBirthDate();
        LocalDateTime db = b.getBirthDate();
        if (da != null && db != null && !da.equals(db)) {
            return da.compareTo(db);
        }
        if (da != null && db == null) {
            return -1;
        }
        if (da == null && db != null) {
            return 1;
        }
        return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
    }

    private String wordOf(ComposedBook book, MemberDO m) {
        if (m.getGenerationId() == null) {
            return null;
        }
        return book.wordByGenId.get(m.getGenerationId());
    }

    private String wordsOfGen(ComposedBook book, int genNo) {
        return book.genWords.getOrDefault(genNo, book.lineageMembers.stream()
                        .filter(m -> Objects.equals(m.getGenerationNo(), genNo))
                        .map(m -> wordOf(book, m))
                        .filter(StrUtil::isNotBlank)
                        .distinct()
                        .collect(Collectors.joining("、")));
    }

    private String resolveTitle(FamilyDO family) {
        if (StrUtil.isNotBlank(family.getBookTitle())) {
            return family.getBookTitle();
        }
        return StrUtil.blankToDefault(family.getName(), "族谱") + "谱书";
    }

    private String chineseGen(int no) {
        if (no <= 0) {
            return "未分世";
        }
        return "第" + no + "世";
    }

    private BookPageRespVO basePage(String type, String title, int pageNo) {
        BookPageRespVO p = new BookPageRespVO();
        p.setType(type);
        p.setTitle(title);
        p.setPageNo(pageNo);
        p.setLoginRequired(false);
        return p;
    }

    private BookTocItemVO toc(int pageNo, String type, String title, Integer generationNo) {
        BookTocItemVO item = new BookTocItemVO();
        item.setPageNo(pageNo);
        item.setType(type);
        item.setTitle(title);
        item.setGenerationNo(generationNo);
        return item;
    }

    private BookPageRespVO copyPage(BookPageRespVO src) {
        BookPageRespVO p = new BookPageRespVO();
        p.setPageNo(src.getPageNo());
        p.setType(src.getType());
        p.setTitle(src.getTitle());
        p.setGenerationNo(src.getGenerationNo());
        p.setGenerationWords(src.getGenerationWords());
        p.setLoginRequired(src.getLoginRequired());
        p.setBookTitle(src.getBookTitle());
        p.setFamilyName(src.getFamilyName());
        p.setSurname(src.getSurname());
        p.setAncestorName(src.getAncestorName());
        p.setRegion(src.getRegion());
        p.setLogo(src.getLogo());
        p.setBookRevision(src.getBookRevision());
        p.setHtml(src.getHtml());
        p.setGenerationRows(src.getGenerationRows() == null ? new ArrayList<>() : new ArrayList<>(src.getGenerationRows()));
        p.setBranches(src.getBranches() == null ? new ArrayList<>() : new ArrayList<>(src.getBranches()));
        return p;
    }

    private static class ComposedBook {
        private FamilyDO family;
        private Map<Long, MemberDO> byId = new HashMap<>();
        private Map<Long, String> wordByGenId = new HashMap<>();
        private List<MemberDO> lineageMembers = new ArrayList<>();
        private List<BookPageRespVO> pages = new ArrayList<>();
        private List<BookTocItemVO> toc = new ArrayList<>();
        private List<BookGenerationIndexVO> generationIndex = new ArrayList<>();
        private Map<Long, Integer> memberPage = new HashMap<>();
        private Map<Integer, Integer> genFirstPage = new HashMap<>();
        private Map<Integer, String> genWords = new HashMap<>();
        private Map<Long, Integer> wordOrder = new HashMap<>();
        private Map<Long, List<BookDeedVO>> deedsByMember = new HashMap<>();
        private int generationTablePage = 3;
        private int firstLineagePage = 4;
    }
}
