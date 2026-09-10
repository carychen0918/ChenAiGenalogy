package chen.genealogy.module.genealogy.dal.mysql.member;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDeedDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberDeedMapper extends BaseMapperX<MemberDeedDO> {

    default List<MemberDeedDO> selectListByMemberId(Long memberId) {
        return selectList(MemberDeedDO::getMemberId, memberId);
    }
}
