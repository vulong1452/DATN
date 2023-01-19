package com.datn.application.service.impl;

import com.datn.application.config.Contant;
import com.datn.application.entity.Category;
import com.datn.application.model.mapper.CategoryMapper;
import com.datn.application.model.request.CreateCategoryRequest;
import com.datn.application.service.CategoryService;
import com.datn.application.exception.BadRequestException;
import com.datn.application.exception.InternalServerException;
import com.datn.application.exception.NotFoundException;
import com.datn.application.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@Component
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<Category> adminGetListCategory(String id, String name, String status, int page) {
        page--;
        if (page <= 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, Contant.LIMIT_CATEGORY, Sort.by("created_at").descending());
        return categoryRepository.adminGetListCategory(id, name, status, pageable);
    }

    @Override
    public void updateOrderCategory(int[] ids) {

        for (int id: ids){
            Optional<Category> category = categoryRepository.findById((long) id);
            category.get().setOrder(0);
            categoryRepository.save(category.get());
        }
    }

    @Override
    public long getCountCategories() {
        return categoryRepository.count();
    }

    @Override
    public List<Category> getListCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }
        return category.get();
    }

    @Override
    public Category createCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = categoryRepository.findByName(createCategoryRequest.getName());
        if (category != null) {
            throw new BadRequestException("Tên danh mục đã tồn tại trong hệ thống. Vui lòng chọn tên khác!");
        }
        category = CategoryMapper.toCategory(createCategoryRequest);
        categoryRepository.save(category);
        return category;
    }

    @Override
    public void updateCategory(CreateCategoryRequest createCategoryRequest, Long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }
        Category cg = categoryRepository.findByName(createCategoryRequest.getName());
        Category category = result.get();
        if (Objects.equals(category.getName(), createCategoryRequest.getName())){
            category.setName(createCategoryRequest.getName());
            category.setStatus(createCategoryRequest.isStatus());
            category.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        }else {
            category.setName(createCategoryRequest.getName());
            category.setStatus(createCategoryRequest.isStatus());
            category.setModifiedAt(new Timestamp(System.currentTimeMillis()));
            if (cg != null){
                if (createCategoryRequest.getName().equals(cg.getName())){
                    throw new BadRequestException("Tên danh mục " + createCategoryRequest.getName() + " đã tồn tại trong hệ thống, Vui lòng chọn tên khác!");
                }
            }
        }
        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi chỉnh sửa danh mục");
        }
    }

    @Override
    public void deleteCategory(long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }

        //Check product in category
        long count = categoryRepository.checkProductInCategory(id);
        if (count > 0) {
            throw new BadRequestException("Có sản phẩm thuộc danh mục không thể xóa!");
        }

        try {
            categoryRepository.deleteById(id);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi xóa danh mục!");
        }
    }


}
