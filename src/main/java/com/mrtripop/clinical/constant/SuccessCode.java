package com.mrtripop.clinical.constant;

import com.mrtripop.constant.BaseStatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseStatusCode {
  // Store operations
  CL2001_GET_ALL_STORES_IS_SUCCESS("CL2001", "Stores retrieved successfully"),
  CL2002_GET_STORE_BY_ID_IS_SUCCESS("CL2002", "Store retrieved successfully"),
  CL2003_CREATE_STORE_IS_SUCCESS("CL2003", "Store created successfully"),
  CL2004_UPDATE_STORE_IS_SUCCESS("CL2004", "Store updated successfully"),
  CL2005_DELETE_STORE_IS_SUCCESS("CL2005", "Store deleted successfully"),
  // Store-product operations
  CL2006_ACTIVATE_PRODUCT_SUCCESS("CL2006", "Product activated successfully"),
  CL2007_GET_ACTIVE_PRODUCTS_SUCCESS("CL2007", "Active products retrieved successfully"),
  CL2008_GET_STORE_PRODUCT_SUCCESS("CL2008", "Store product retrieved successfully"),
  CL2009_UPDATE_OVERRIDE_SUCCESS("CL2009", "Override updated successfully"),
  CL2010_DEACTIVATE_PRODUCT_SUCCESS("CL2010", "Product deactivated successfully"),
  // Catalog operations
  CL2011_CREATE_MOLECULE_SUCCESS("CL2011", "Molecule created successfully"),
  CL2012_CREATE_BRAND_SUCCESS("CL2012", "Brand created successfully"),
  CL2013_GET_MOLECULE_SUCCESS("CL2013", "Molecule retrieved successfully"),
  CL2014_UPDATE_MOLECULE_METADATA_SUCCESS("CL2014", "Molecule metadata updated successfully"),
  CL2015_SEARCH_MOLECULES_SUCCESS("CL2015", "Molecules retrieved successfully"),
  CL2016_GET_BRANDS_BY_MOLECULE_SUCCESS("CL2016", "Brands retrieved successfully");

  private final String code;
  private final String message;
}