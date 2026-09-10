package chen.genealogy.module.mes.dal.mysql.pro.workrecord;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.dal.dataobject.pro.workrecord.MesProWorkRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 当前绑定状态（快照） Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesProWorkRecordMapper extends BaseMapperX<MesProWorkRecordDO> {

    default MesProWorkRecordDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<MesProWorkRecordDO>()
                .eq(MesProWorkRecordDO::getUserId, userId));
    }

}
