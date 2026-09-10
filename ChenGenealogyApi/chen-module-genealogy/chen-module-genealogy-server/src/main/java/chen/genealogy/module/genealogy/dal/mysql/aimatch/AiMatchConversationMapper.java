package chen.genealogy.module.genealogy.dal.mysql.aimatch;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.aimatch.AiMatchConversationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiMatchConversationMapper extends BaseMapperX<AiMatchConversationDO> {

    default List<AiMatchConversationDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<AiMatchConversationDO>()
                .eq(AiMatchConversationDO::getUserId, userId)
                .orderByDesc(AiMatchConversationDO::getUpdateTime)
                .orderByDesc(AiMatchConversationDO::getId));
    }

    default AiMatchConversationDO selectBySessionId(String sessionId) {
        return selectOne(AiMatchConversationDO::getSessionId, sessionId);
    }
}
