package chen.genealogy.module.genealogy.dal.mysql.member;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.member.vo.ArchiveApplyPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.member.ArchiveApplyDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArchiveApplyMapper extends BaseMapperX<ArchiveApplyDO> {

    default PageResult<ArchiveApplyDO> selectPage(ArchiveApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArchiveApplyDO>()
                .eqIfPresent(ArchiveApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ArchiveApplyDO::getMemberId, reqVO.getMemberId())
                .orderByDesc(ArchiveApplyDO::getId));
    }
}
