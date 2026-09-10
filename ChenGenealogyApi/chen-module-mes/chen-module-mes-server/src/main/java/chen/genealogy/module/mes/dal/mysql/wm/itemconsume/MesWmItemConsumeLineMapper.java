package chen.genealogy.module.mes.dal.mysql.wm.itemconsume;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.itemconsume.vo.MesWmItemConsumeLinePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 物料消耗记录行 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmItemConsumeLineMapper extends BaseMapperX<MesWmItemConsumeLineDO> {

    default PageResult<MesWmItemConsumeLineDO> selectPage(MesWmItemConsumeLinePageReqVO reqVO,
                                                          Long consumeId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmItemConsumeLineDO>()
                .eq(MesWmItemConsumeLineDO::getConsumeId, consumeId)
                .orderByDesc(MesWmItemConsumeLineDO::getId));
    }

    default List<MesWmItemConsumeLineDO> selectListByConsumeId(Long consumeId) {
        return selectList(MesWmItemConsumeLineDO::getConsumeId, consumeId);
    }

}
