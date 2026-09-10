package chen.genealogy.module.product.convert.brand;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.product.controller.admin.brand.vo.ProductBrandCreateReqVO;
import chen.genealogy.module.product.controller.admin.brand.vo.ProductBrandRespVO;
import chen.genealogy.module.product.controller.admin.brand.vo.ProductBrandSimpleRespVO;
import chen.genealogy.module.product.controller.admin.brand.vo.ProductBrandUpdateReqVO;
import chen.genealogy.module.product.dal.dataobject.brand.ProductBrandDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 品牌 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductBrandConvert {

    ProductBrandConvert INSTANCE = Mappers.getMapper(ProductBrandConvert.class);

    ProductBrandDO convert(ProductBrandCreateReqVO bean);

    ProductBrandDO convert(ProductBrandUpdateReqVO bean);

    ProductBrandRespVO convert(ProductBrandDO bean);

    List<ProductBrandSimpleRespVO> convertList1(List<ProductBrandDO> list);

    List<ProductBrandRespVO> convertList(List<ProductBrandDO> list);

    PageResult<ProductBrandRespVO> convertPage(PageResult<ProductBrandDO> page);

}
