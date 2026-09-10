package chen.genealogy.module.wms.dal.mysql.md.item;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.wms.controller.admin.md.item.vo.brand.WmsItemBrandPageReqVO;
import chen.genealogy.module.wms.dal.dataobject.md.item.WmsItemBrandDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS 商品品牌 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsItemBrandMapper extends BaseMapperX<WmsItemBrandDO> {

    default PageResult<WmsItemBrandDO> selectPage(WmsItemBrandPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsItemBrandDO>()
                .likeIfPresent(WmsItemBrandDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsItemBrandDO::getName, reqVO.getName())
                .orderByDesc(WmsItemBrandDO::getId));
    }

    default WmsItemBrandDO selectByCode(String code) {
        return selectOne(WmsItemBrandDO::getCode, code);
    }

    default WmsItemBrandDO selectByName(String name) {
        return selectOne(WmsItemBrandDO::getName, name);
    }

}
