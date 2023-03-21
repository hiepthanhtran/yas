package com.yas.product.service;

import com.yas.product.exception.DuplicatedNameException;
import com.yas.product.exception.NotFoundException;
import com.yas.product.utils.Constants;
import com.yas.product.viewmodel.productoption.ProductOptionGetVm;
import com.yas.product.model.ProductOption;
import com.yas.product.viewmodel.productoption.ProductOptionListGetVm;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.viewmodel.productoption.ProductOptionPostVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductOptionService {
    private final ProductOptionRepository productOptionRepository;

    public ProductOptionService(ProductOptionRepository productOptionRepository) {
        this.productOptionRepository = productOptionRepository;
    }

    public ProductOptionListGetVm getPageableProductOptions(int pageNo, int pageSize) {
        List<ProductOptionGetVm> productOptionGetVms = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ProductOption> productOptionPage = productOptionRepository.findAll(pageable);
        List<ProductOption> productOptions = productOptionPage.getContent();
        for (ProductOption productOption : productOptions) {
            productOptionGetVms.add(ProductOptionGetVm.fromModel(productOption));
        }

        return new ProductOptionListGetVm(
            productOptionGetVms,
            productOptionPage.getNumber(),
            productOptionPage.getSize(),
            (int) productOptionPage.getTotalElements(),
            productOptionPage.getTotalPages(),
            productOptionPage.isLast()
        );
    }

    public ProductOption create(ProductOptionPostVm productOptionPostVm) {
        if (checkExistedName(productOptionPostVm.name(), null)) {
            throw new DuplicatedNameException(Constants.ERROR_CODE.NAME_ALREADY_EXITED, productOptionPostVm.name());
        }
        ProductOption productOption = new ProductOption();
        productOption.setName(productOptionPostVm.name());

        return productOptionRepository.saveAndFlush(productOption);
    }

    private boolean checkExistedName(String name, Long id) {
        return productOptionRepository.findExistedName(name, id) != null;
    }

    public ProductOption update(ProductOptionPostVm productOptionPostVm, Long id) {
        ProductOption productOption = productOptionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(Constants.ERROR_CODE.PRODUCT_OPTION_NOT_FOUND, id)));

        if (checkExistedName(productOptionPostVm.name(), id)) {
            throw new DuplicatedNameException(Constants.ERROR_CODE.NAME_ALREADY_EXITED, productOptionPostVm.name());
        }
        productOption.setName(productOptionPostVm.name());

        return productOptionRepository.saveAndFlush(productOption);
    }
}
