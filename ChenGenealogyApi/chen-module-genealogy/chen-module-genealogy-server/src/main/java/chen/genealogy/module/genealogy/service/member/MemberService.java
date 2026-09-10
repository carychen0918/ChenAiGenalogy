package chen.genealogy.module.genealogy.service.member;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.genealogy.controller.admin.member.vo.*;
import chen.genealogy.module.genealogy.dal.dataobject.member.MemberDO;

import java.util.List;

public interface MemberService {

    MemberCreateRespVO createMember(MemberSaveReqVO reqVO);

    MemberCreateRespVO updateMember(MemberSaveReqVO reqVO);

    void deleteMember(Long id, Boolean confirm);

    void restoreMember(Long id);

    List<MemberDO> getRecycleList();

    MemberRespVO getMember(Long id);

    PageResult<MemberRespVO> getMemberPage(MemberPageReqVO pageReqVO);

    List<MemberSimpleVO> getSimpleList();

    List<MemberRespVO> getTree(Long rootId, Integer up, Integer down);

    MemberImportRespVO importMemberList(List<MemberImportExcelVO> list, Boolean updateSupport);

    Long createArchiveApply(ArchiveApplySaveReqVO reqVO);

    void auditArchiveApply(Long id, Integer status, String reason);

    PageResult<ArchiveApplyRespVO> getArchiveApplyPage(ArchiveApplyPageReqVO reqVO);

    MemberDO getCurrentMember();

    void updateMyPhotos(List<String> photoUrls);

    Long saveMyDeed(MemberDeedSaveReqVO reqVO);

    void deleteMyDeed(Long id);

    Long saveDeed(MemberDeedSaveReqVO reqVO);

    void deleteDeed(Long id);

    void updateArchive(MemberArchiveSaveReqVO reqVO);
}
