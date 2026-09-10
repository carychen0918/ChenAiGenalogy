package chen.genealogy.module.promotion.convert.banner;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.promotion.controller.admin.banner.vo.BannerCreateReqVO;
import chen.genealogy.module.promotion.controller.admin.banner.vo.BannerRespVO;
import chen.genealogy.module.promotion.controller.admin.banner.vo.BannerUpdateReqVO;
import chen.genealogy.module.promotion.controller.app.banner.vo.AppBannerRespVO;
import chen.genealogy.module.promotion.dal.dataobject.banner.BannerDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);

    List<BannerRespVO> convertList(List<BannerDO> list);

    PageResult<BannerRespVO> convertPage(PageResult<BannerDO> pageResult);

    BannerRespVO convert(BannerDO banner);

    BannerDO convert(BannerCreateReqVO createReqVO);

    BannerDO convert(BannerUpdateReqVO updateReqVO);

    List<AppBannerRespVO> convertList01(List<BannerDO> bannerList);

}
