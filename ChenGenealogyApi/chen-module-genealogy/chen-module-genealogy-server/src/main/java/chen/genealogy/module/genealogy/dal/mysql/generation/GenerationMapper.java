package chen.genealogy.module.genealogy.dal.mysql.generation;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.generation.GenerationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GenerationMapper extends BaseMapperX<GenerationDO> {

    default List<GenerationDO> selectListByFamilyId(Long familyId) {
        return selectList(new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .orderByAsc(GenerationDO::getGenerationNo)
                .orderByAsc(GenerationDO::getSort)
                .orderByAsc(GenerationDO::getId));
    }

    default List<GenerationDO> selectListByFamilyAndNo(Long familyId, Integer generationNo) {
        return selectList(new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .eq(GenerationDO::getGenerationNo, generationNo)
                .orderByAsc(GenerationDO::getSort)
                .orderByAsc(GenerationDO::getId));
    }

    default GenerationDO selectByFamilyAndNo(Long familyId, Integer generationNo) {
        return selectOne(new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .eq(GenerationDO::getGenerationNo, generationNo)
                .orderByAsc(GenerationDO::getSort)
                .orderByAsc(GenerationDO::getId)
                .last("LIMIT 1"));
    }

    default GenerationDO selectByFamilyNoAndWord(Long familyId, Integer generationNo, String word) {
        return selectOne(new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .eq(GenerationDO::getGenerationNo, generationNo)
                .eq(GenerationDO::getWord, word)
                .last("LIMIT 1"));
    }

    default GenerationDO selectByFamilyNoWordAndHouse(Long familyId, Integer generationNo, String word, String house) {
        LambdaQueryWrapperX<GenerationDO> q = new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .eq(GenerationDO::getGenerationNo, generationNo)
                .eq(GenerationDO::getWord, word);
        if (house == null || house.isBlank()) {
            q.and(w -> w.isNull(GenerationDO::getHouse).or().eq(GenerationDO::getHouse, ""));
        } else {
            q.eq(GenerationDO::getHouse, house);
        }
        return selectOne(q.last("LIMIT 1"));
    }

    default List<GenerationDO> selectListByFamilyAndWord(Long familyId, String word) {
        return selectList(new LambdaQueryWrapperX<GenerationDO>()
                .eq(GenerationDO::getFamilyId, familyId)
                .eq(GenerationDO::getWord, word)
                .orderByAsc(GenerationDO::getGenerationNo)
                .orderByAsc(GenerationDO::getId));
    }
}
