package chen.genealogy.module.genealogy.service.member;

import cn.hutool.core.collection.CollUtil;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.module.genealogy.controller.admin.member.vo.MemberRespVO;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import chen.genealogy.module.genealogy.dal.mysql.generation.GenerationMapper;
import chen.genealogy.module.genealogy.dal.mysql.member.MemberMapper;
import chen.genealogy.module.genealogy.dal.redis.RedisKeyConstants;
import chen.genealogy.module.genealogy.util.RegionAreaUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 族谱世系全量缓存：一次查出全族成员并在内存拼装，避免树接口的 N+1。
 * 成员或字辈写入后 {@link #evictFamilyMembers()} 清空。
 */
@Service
public class PedigreeCacheService {

    public static final Long FAMILY_ID = 1L;

    @Resource
    private MemberMapper memberMapper;
    @Resource
    private GenerationMapper generationMapper;

    @Cacheable(value = RedisKeyConstants.MEMBER_PEDIGREE, key = "#familyId", unless = "#result == null")
    public List<MemberRespVO> getFamilyMembers(Long familyId) {
        List<MemberDO> all = memberMapper.selectListByFamilyId(familyId);
        if (CollUtil.isEmpty(all)) {
            return Collections.emptyList();
        }
        Map<Long, MemberDO> byId = all.stream().collect(Collectors.toMap(MemberDO::getId, m -> m, (a, b) -> a));
        Map<Long, GenerationDO> gens = generationMapper.selectListByFamilyId(familyId).stream()
                .collect(Collectors.toMap(GenerationDO::getId, g -> g, (a, b) -> a));
        Map<Long, Integer> childCount = new HashMap<>();
        for (MemberDO member : all) {
            if (member.getFatherId() != null) {
                childCount.merge(member.getFatherId(), 1, Integer::sum);
            }
        }
        List<MemberRespVO> list = new ArrayList<>(all.size());
        for (MemberDO member : all) {
            list.add(toTreeVo(member, byId, gens, childCount));
        }
        return list;
    }

    @CacheEvict(cacheNames = RedisKeyConstants.MEMBER_PEDIGREE, allEntries = true)
    public void evictFamilyMembers() {
    }

    private MemberRespVO toTreeVo(MemberDO member, Map<Long, MemberDO> byId,
                                  Map<Long, GenerationDO> gens, Map<Long, Integer> childCount) {
        MemberRespVO vo = BeanUtils.toBean(member, MemberRespVO.class);
        if (member.getGenerationId() != null) {
            GenerationDO gen = gens.get(member.getGenerationId());
            if (gen != null) {
                vo.setGenerationWord(gen.getWord());
                vo.setGenerationHouse(gen.getHouse());
                vo.setGenerationNationalSource(gen.getNationalSource());
            }
        }
        if (member.getFatherId() != null) {
            MemberDO father = byId.get(member.getFatherId());
            if (father != null) {
                vo.setFatherName(father.getName());
            }
        }
        if (member.getMotherId() != null) {
            MemberDO mother = byId.get(member.getMotherId());
            if (mother != null) {
                vo.setMotherName(mother.getName());
            }
        }
        if (CollUtil.isNotEmpty(member.getSpouseIds())) {
            vo.setSpouseNames(member.getSpouseIds().stream()
                    .map(byId::get)
                    .filter(Objects::nonNull)
                    .map(MemberDO::getName)
                    .collect(Collectors.toList()));
        }
        vo.setDescendantCount(childCount.getOrDefault(member.getId(), 0));
        RegionAreaUtils.Region region = RegionAreaUtils.of(member.getProvinceId(), member.getCityId(), member.getCountyId());
        vo.setRegionName(region.getRegionName());
        return vo;
    }
}
