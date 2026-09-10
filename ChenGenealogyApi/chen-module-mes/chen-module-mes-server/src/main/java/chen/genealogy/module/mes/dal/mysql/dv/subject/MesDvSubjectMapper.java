package chen.genealogy.module.mes.dal.mysql.dv.subject;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.dv.subject.vo.MesDvSubjectPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MesDvSubjectMapper extends BaseMapperX<MesDvSubjectDO> {

    default PageResult<MesDvSubjectDO> selectPage(MesDvSubjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvSubjectDO>()
                .likeIfPresent(MesDvSubjectDO::getCode, reqVO.getCode())
                .likeIfPresent(MesDvSubjectDO::getName, reqVO.getName())
                .eqIfPresent(MesDvSubjectDO::getType, reqVO.getType())
                .eqIfPresent(MesDvSubjectDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesDvSubjectDO::getId));
    }

    default MesDvSubjectDO selectByCode(String code) {
        return selectOne(MesDvSubjectDO::getCode, code);
    }

}
