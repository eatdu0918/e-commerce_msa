package com.ecommerce.productservice.util;

import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import org.springframework.util.StringUtils;

public final class CatalogLocaleHelper {

    private CatalogLocaleHelper() {
    }

    public static boolean preferKorean(String acceptLanguage) {
        if (!StringUtils.hasText(acceptLanguage)) {
            return false;
        }
        String first = acceptLanguage.split(",")[0].trim().split(";")[0].trim().toLowerCase();
        return first.startsWith("ko");
    }

    public static String productName(Product product, boolean preferKorean) {
        if (preferKorean && StringUtils.hasText(product.getNameKo())) {
            return product.getNameKo();
        }
        return product.getName();
    }

    public static String productDescription(Product product, boolean preferKorean) {
        if (preferKorean && StringUtils.hasText(product.getDescriptionKo())) {
            return product.getDescriptionKo();
        }
        return product.getDescription();
    }

    public static String categoryName(Category category, boolean preferKorean) {
        if (category == null) {
            return null;
        }
        if (preferKorean && StringUtils.hasText(category.getNameKo())) {
            return category.getNameKo();
        }
        return category.getName();
    }
}
