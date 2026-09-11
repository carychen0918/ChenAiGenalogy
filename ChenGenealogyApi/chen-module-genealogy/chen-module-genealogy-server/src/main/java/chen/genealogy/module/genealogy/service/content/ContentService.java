package chen.genealogy.module.genealogy.service.content;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.content.vo.*;
import chen.genealogy.module.genealogy.controller.admin.activity.vo.TombSiteSaveReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.activity.TombSiteDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.CultureGuideDO;
import chen.genealogy.module.genealogy.dal.dataobject.content.MigrationNodeDO;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.mysql.activity.TombSiteMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.AncestorDeedMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.CultureGuideMapper;
import chen.genealogy.module.genealogy.dal.mysql.content.MigrationNodeMapper;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.mysql.family.FamilyMapper;
import chen.genealogy.module.genealogy.dal.mysql.generation.GenerationMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.enums.GenerationDisplay;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.module.genealogy.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ContentService {

    private static final Long FAMILY_ID = 1L;

    @Resource
    private FamilyMapper familyMapper;
    @Resource
    private GenerationMapper generationMapper;
    @Resource
    private MemberMapper memberMapper;
    @Resource
    private MigrationNodeMapper migrationNodeMapper;
    @Resource
    private AncestorDeedMapper ancestorDeedMapper;
    @Resource
    private CultureGuideMapper cultureGuideMapper;
    @Resource
    private TombSiteMapper tombSiteMapper;

    public FamilyDO getFamily() {
        FamilyDO family = familyMapper.selectById(FAMILY_ID);
        if (family == null) {
            throw exception(FAMILY_NOT_EXISTS);
        }
        return family;
    }

    public void updateFamily(FamilySaveReqVO reqVO) {
        FamilyDO family = getFamily();
        FamilyDO update = BeanUtils.toBean(reqVO, FamilyDO.class);
        update.setId(family.getId());
        familyMapper.updateById(update);
    }

    public Long createGeneration(GenerationSaveReqVO reqVO) {
        normalizeWord(reqVO);
        validateGenerationWordUnique(null, reqVO.getGenerationNo(), reqVO.getWord(), reqVO.getHouse());
        GenerationDO gen = BeanUtils.toBean(reqVO, GenerationDO.class);
        gen.setFamilyId(FAMILY_ID);
        if (gen.getSort() == null) {
            gen.setSort(reqVO.getGenerationNo() * 10);
        }
        generationMapper.insert(gen);
        syncPoemFields(gen.getGenerationNo(), gen.getNationalSource(), gen.getJiaofangSource());
        return gen.getId();
    }

    public void updateGeneration(GenerationSaveReqVO reqVO) {
        GenerationDO exists = generationMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw exception(GENERATION_NOT_EXISTS);
        }
        normalizeWord(reqVO);
        validateGenerationWordUnique(reqVO.getId(), reqVO.getGenerationNo(), reqVO.getWord(), reqVO.getHouse());
        GenerationDO update = BeanUtils.toBean(reqVO, GenerationDO.class);
        update.setFamilyId(FAMILY_ID);
        generationMapper.updateById(update);
        syncPoemFields(update.getGenerationNo(), update.getNationalSource(), update.getJiaofangSource());
    }

    public void deleteGeneration(Long id) {
        if (generationMapper.selectById(id) == null) {
            throw exception(GENERATION_NOT_EXISTS);
        }
        long used = memberMapper.selectCount(MemberDO::getGenerationId, id);
        if (used > 0) {
            throw exception(GENERATION_IN_USE, used);
        }
        generationMapper.deleteById(id);
    }

    public List<GenerationDO> getGenerationList() {
        return generationMapper.selectListByFamilyId(FAMILY_ID);
    }

    public List<GenerationPoemRowVO> getPoemTable() {
        Map<Integer, List<GenerationDO>> map = new TreeMap<>();
        for (GenerationDO g : getGenerationList()) {
            if (g.getGenerationNo() == null) {
                continue;
            }
            map.computeIfAbsent(g.getGenerationNo(), k -> new ArrayList<>()).add(g);
        }
        List<GenerationPoemRowVO> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<GenerationDO>> e : map.entrySet()) {
            List<GenerationDO> items = e.getValue();
            GenerationPoemRowVO row = new GenerationPoemRowVO();
            row.setGenerationNo(e.getKey());
            row.setChituOrder(GenerationDisplay.chituOrder(e.getKey()));
            row.setNationalSource(firstText(items, GenerationDO::getNationalSource));
            row.setJiaofangSource(firstText(items, GenerationDO::getJiaofangSource));
            row.setHouse1(houseWords(items, "1"));
            row.setHouse2(houseWords(items, "2"));
            row.setHouse3(houseWords(items, "3"));
            row.setHouse3Zhijin(houseWords(items, "4"));
            row.setHouse45(houseWords(items, "5"));
            rows.add(row);
        }
        return rows;
    }

    public String recommendName(Integer fatherGenerationNo) {
        List<GenerationDO> next = generationMapper.selectListByFamilyAndNo(FAMILY_ID, fatherGenerationNo + 1);
        if (next == null || next.isEmpty()) {
            return "未定";
        }
        return next.stream()
                .map(g -> GenerationDisplay.wordWithHouse(g.getWord(), g.getHouse()))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .reduce((a, b) -> a + "、" + b)
                .orElse("未定");
    }

    private void normalizeWord(GenerationSaveReqVO reqVO) {
        if (reqVO.getWord() != null) {
            reqVO.setWord(reqVO.getWord().trim());
        }
        if (StrUtil.isBlank(reqVO.getHouse())) {
            reqVO.setHouse(null);
        } else {
            reqVO.setHouse(reqVO.getHouse().trim());
        }
        if (StrUtil.isBlank(reqVO.getNationalSource())) {
            reqVO.setNationalSource(null);
        } else {
            reqVO.setNationalSource(reqVO.getNationalSource().trim());
        }
        if (StrUtil.isBlank(reqVO.getJiaofangSource())) {
            reqVO.setJiaofangSource(null);
        } else {
            reqVO.setJiaofangSource(reqVO.getJiaofangSource().trim());
        }
    }

    private void syncPoemFields(Integer generationNo, String nationalSource, String jiaofangSource) {
        if (generationNo == null) {
            return;
        }
        List<GenerationDO> same = generationMapper.selectListByFamilyAndNo(FAMILY_ID, generationNo);
        if (same == null) {
            return;
        }
        for (GenerationDO g : same) {
            GenerationDO patch = new GenerationDO();
            patch.setId(g.getId());
            patch.setNationalSource(nationalSource);
            patch.setJiaofangSource(jiaofangSource);
            generationMapper.updateById(patch);
        }
    }

    private String firstText(List<GenerationDO> items, Function<GenerationDO, String> getter) {
        return items.stream().map(getter).filter(StrUtil::isNotBlank).findFirst().orElse("");
    }

    private String houseWords(List<GenerationDO> items, String house) {
        return items.stream()
                .filter(g -> house.equals(g.getHouse()))
                .map(GenerationDO::getWord)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));
    }

    private void validateGenerationWordUnique(Long id, Integer generationNo, String word, String house) {
        GenerationDO exists = generationMapper.selectByFamilyNoWordAndHouse(FAMILY_ID, generationNo, word, house);
        if (exists != null && (id == null || !exists.getId().equals(id))) {
            throw exception(GENERATION_DUPLICATE, generationNo, word);
        }
    }

    public Long createMigration(MigrationNodeSaveReqVO reqVO) {
        MigrationNodeDO node = BeanUtils.toBean(reqVO, MigrationNodeDO.class);
        node.setFamilyId(FAMILY_ID);
        if (node.getSort() == null) {
            node.setSort(migrationNodeMapper.selectMaxSort(FAMILY_ID) + 1);
        }
        migrationNodeMapper.insert(node);
        return node.getId();
    }

    public void updateMigration(MigrationNodeSaveReqVO reqVO) {
        if (migrationNodeMapper.selectById(reqVO.getId()) == null) {
            throw exception(MIGRATION_NOT_EXISTS);
        }
        migrationNodeMapper.updateById(BeanUtils.toBean(reqVO, MigrationNodeDO.class));
    }

    public void deleteMigration(Long id) {
        migrationNodeMapper.deleteById(id);
    }

    public List<MigrationNodeDO> getMigrationList() {
        return migrationNodeMapper.selectListByFamilyId(FAMILY_ID);
    }

    public Long createAncestorDeed(AncestorDeedSaveReqVO reqVO) {
        AncestorDeedDO deed = BeanUtils.toBean(reqVO, AncestorDeedDO.class);
        deed.setFamilyId(FAMILY_ID);
        ancestorDeedMapper.insert(deed);
        return deed.getId();
    }

    public void updateAncestorDeed(AncestorDeedSaveReqVO reqVO) {
        if (ancestorDeedMapper.selectById(reqVO.getId()) == null) {
            throw exception(ANCESTOR_DEED_NOT_EXISTS);
        }
        ancestorDeedMapper.updateById(BeanUtils.toBean(reqVO, AncestorDeedDO.class));
    }

    public void deleteAncestorDeed(Long id) {
        ancestorDeedMapper.deleteById(id);
    }

    public AncestorDeedDO getAncestorDeed(Long id) {
        AncestorDeedDO deed = ancestorDeedMapper.selectById(id);
        if (deed == null) {
            throw exception(ANCESTOR_DEED_NOT_EXISTS);
        }
        return deed;
    }

    public PageResult<AncestorDeedDO> getAncestorDeedPage(AncestorDeedPageReqVO reqVO) {
        reqVO.setFamilyId(FAMILY_ID);
        return ancestorDeedMapper.selectPage(reqVO);
    }

    public Long createCulture(CultureGuideSaveReqVO reqVO) {
        CultureGuideDO guide = BeanUtils.toBean(reqVO, CultureGuideDO.class);
        guide.setFamilyId(FAMILY_ID);
        cultureGuideMapper.insert(guide);
        return guide.getId();
    }

    public void updateCulture(CultureGuideSaveReqVO reqVO) {
        cultureGuideMapper.updateById(BeanUtils.toBean(reqVO, CultureGuideDO.class));
    }

    public void deleteCulture(Long id) {
        cultureGuideMapper.deleteById(id);
    }

    public List<CultureGuideDO> getCultureList() {
        return cultureGuideMapper.selectListByFamilyId(FAMILY_ID);
    }

    public TombSiteDO getTomb() {
        return tombSiteMapper.selectByFamilyId(FAMILY_ID);
    }

    public void saveTomb(TombSiteSaveReqVO reqVO) {
        TombSiteDO exists = tombSiteMapper.selectByFamilyId(FAMILY_ID);
        TombSiteDO data = BeanUtils.toBean(reqVO, TombSiteDO.class);
        data.setFamilyId(FAMILY_ID);
        if (exists == null) {
            tombSiteMapper.insert(data);
        } else {
            data.setId(exists.getId());
            tombSiteMapper.updateById(data);
        }
    }
}
