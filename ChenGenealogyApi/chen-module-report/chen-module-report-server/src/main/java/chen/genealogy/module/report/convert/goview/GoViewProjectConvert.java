package chen.genealogy.module.report.convert.goview;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.report.controller.admin.goview.vo.project.GoViewProjectCreateReqVO;
import chen.genealogy.module.report.controller.admin.goview.vo.project.GoViewProjectRespVO;
import chen.genealogy.module.report.controller.admin.goview.vo.project.GoViewProjectUpdateReqVO;
import chen.genealogy.module.report.dal.dataobject.goview.GoViewProjectDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GoViewProjectConvert {

    GoViewProjectConvert INSTANCE = Mappers.getMapper(GoViewProjectConvert.class);

    GoViewProjectDO convert(GoViewProjectCreateReqVO bean);

    GoViewProjectDO convert(GoViewProjectUpdateReqVO bean);

    GoViewProjectRespVO convert(GoViewProjectDO bean);

    PageResult<GoViewProjectRespVO> convertPage(PageResult<GoViewProjectDO> page);

}
