package chen.genealogy.module.genealogy.dal.mysql.family;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.module.genealogy.dal.dataobject.family.FamilyDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FamilyMapper extends BaseMapperX<FamilyDO> {
}
