package chen.genealogy.module.mp.dal.mysql.message;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.module.mp.controller.admin.message.vo.template.MpMessageTemplateListReqVO;
import chen.genealogy.module.mp.dal.dataobject.message.MpMessageTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MpMessageTemplateMapper extends BaseMapperX<MpMessageTemplateDO> {

    default List<MpMessageTemplateDO> selectList(MpMessageTemplateListReqVO listReqVO) {
        return selectList(MpMessageTemplateDO::getAccountId, listReqVO.getAccountId());
    }

}