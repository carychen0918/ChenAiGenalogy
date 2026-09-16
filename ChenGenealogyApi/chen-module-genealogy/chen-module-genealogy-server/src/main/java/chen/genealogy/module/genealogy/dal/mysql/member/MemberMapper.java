package chen.genealogy.module.genealogy.dal.mysql.member;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.member.vo.MemberPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberMapper extends BaseMapperX<MemberDO> {

    default PageResult<MemberDO> selectPage(MemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberDO>()
                .likeIfPresent(MemberDO::getName, reqVO.getName())
                .eqIfPresent(MemberDO::getGender, reqVO.getGender())
                .eqIfPresent(MemberDO::getGenerationNo, reqVO.getGenerationNo())
                .eqIfPresent(MemberDO::getFatherId, reqVO.getFatherId())
                .betweenIfPresent(MemberDO::getBirthDate, reqVO.getBirthDate())
                .orderByAsc(MemberDO::getGenerationNo)
                .orderByAsc(MemberDO::getId));
    }

    default List<MemberDO> selectListByFamilyId(Long familyId) {
        return selectList(new LambdaQueryWrapperX<MemberDO>()
                .eq(MemberDO::getFamilyId, familyId)
                .orderByAsc(MemberDO::getGenerationNo)
                .orderByAsc(MemberDO::getId));
    }

    default List<MemberDO> selectByFatherId(Long fatherId) {
        return selectList(MemberDO::getFatherId, fatherId);
    }

    default List<MemberDO> selectByMotherId(Long motherId) {
        return selectList(MemberDO::getMotherId, motherId);
    }

    default MemberDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<MemberDO>()
                .eq(MemberDO::getUserId, userId)
                .orderByDesc(MemberDO::getGenerationNo)
                .orderByDesc(MemberDO::getId)
                .last("LIMIT 1"));
    }

    default MemberDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapperX<MemberDO>()
                .eq(MemberDO::getName, name)
                .orderByAsc(MemberDO::getId)
                .last("LIMIT 1"));
    }

    default long selectDescendantCount(Long fatherId) {
        return selectCount(MemberDO::getFatherId, fatherId);
    }

    /** updateById 会忽略 null，改为配偶档案时必须显式清空字辈、世代、父母 */
    default int updateLineage(MemberDO member) {
        return update(null, new LambdaUpdateWrapper<MemberDO>()
                .eq(MemberDO::getId, member.getId())
                .set(MemberDO::getGenerationId, member.getGenerationId())
                .set(MemberDO::getGenerationNo, member.getGenerationNo())
                .set(MemberDO::getFatherId, member.getFatherId())
                .set(MemberDO::getMotherId, member.getMotherId()));
    }

    @Select("SELECT * FROM tb_member WHERE deleted = 1 ORDER BY deleted_time DESC")
    List<MemberDO> selectRecycleList();

    @Update("UPDATE tb_member SET deleted = 0, deleted_time = NULL WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
