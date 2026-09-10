package chen.genealogy.module.genealogy.dal.mysql.aimatch;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.aimatch.AiMatchMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiMatchMessageMapper extends BaseMapperX<AiMatchMessageDO> {

    default List<AiMatchMessageDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<AiMatchMessageDO>()
                .eq(AiMatchMessageDO::getConversationId, conversationId)
                .orderByAsc(AiMatchMessageDO::getId));
    }
}
