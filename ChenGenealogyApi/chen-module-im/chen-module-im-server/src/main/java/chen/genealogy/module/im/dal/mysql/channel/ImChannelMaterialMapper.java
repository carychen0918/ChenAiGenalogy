package chen.genealogy.module.im.dal.mysql.channel;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialPageReqVO;
import chen.genealogy.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 频道素材 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImChannelMaterialMapper extends BaseMapperX<ImChannelMaterialDO> {

    default Long selectCountByChannelId(Long channelId) {
        return selectCount(ImChannelMaterialDO::getChannelId, channelId);
    }

    default List<ImChannelMaterialDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapperX<ImChannelMaterialDO>()
                .eq(ImChannelMaterialDO::getChannelId, channelId)
                .orderByDesc(ImChannelMaterialDO::getId));
    }

    default PageResult<ImChannelMaterialDO> selectPage(ImChannelMaterialPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImChannelMaterialDO>()
                .eqIfPresent(ImChannelMaterialDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(ImChannelMaterialDO::getType, reqVO.getType())
                .likeIfPresent(ImChannelMaterialDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(ImChannelMaterialDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImChannelMaterialDO::getId));
    }

}
