package chen.genealogy.module.genealogy.dal.mysql.content;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.content.vo.AncestorDeedPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.content.AncestorDeedDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AncestorDeedMapper extends BaseMapperX<AncestorDeedDO> {

    default PageResult<AncestorDeedDO> selectPage(AncestorDeedPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AncestorDeedDO>()
                .eqIfPresent(AncestorDeedDO::getFamilyId, reqVO.getFamilyId())
                .eqIfPresent(AncestorDeedDO::getCategory, reqVO.getCategory())
                .likeIfPresent(AncestorDeedDO::getName, reqVO.getName())
                .orderByDesc(AncestorDeedDO::getId));
    }
}
