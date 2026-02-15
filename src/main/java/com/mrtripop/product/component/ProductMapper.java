package com.mrtripop.product.component;

import com.mrtripop.product.models.db.Product;
import com.mrtripop.product.models.db.ProductHistory;
import com.mrtripop.product.models.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductHistory toProductHistory(Product product);

  ProductHistory toProductHistory(ProductDTO productDTO);
  
  Product toProduct(ProductDTO productDTO);
  
  ProductDTO toProductDTO(Product product);
  
  ProductDTO toProductDTO(ProductHistory productHistory);
  
}
