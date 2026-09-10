package chen.genealogy.module.genealogy.dal.mysql.content;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.content.CultureGuideDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CultureGuideMapper extends BaseMapperX<CultureGuideDO> {

    default List<CultureGuideDO> selectListByFamilyId(Long familyId) {
        return selectList(new LambdaQueryWrapperX<CultureGuideDO>()
                .eq(CultureGuideDO::getFamilyId, familyId)
                .orderByAsc(CultureGuideDO::getSort));
    }
}
