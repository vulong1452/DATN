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
            throw new NotFoundException("Danh m???c kh??ng t???n t???i!");
        }
        return category.get();
    }

    @Override
    public Category createCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = categoryRepository.findByName(createCategoryRequest.getName());
        if (category != null) {
            throw new BadRequestException("T??n danh m???c ???? t???n t???i trong h??? th???ng. Vui l??ng ch???n t??n kh??c!");
        }
        category = CategoryMapper.toCategory(createCategoryRequest);
        categoryRepository.save(category);
        return category;
    }

    @Override
    public void updateCategory(CreateCategoryRequest createCategoryRequest, Long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh m???c kh??ng t???n t???i!");
        }
        Category cg = categoryRepository.findByName(createCategoryRequest.getName());
        if (cg != null){
            if (createCategoryRequest.getName().equals(cg.getName())){
                throw new BadRequestException("T??n danh m???c " + createCategoryRequest.getName() + " ???? t???n t???i trong h??? th???ng, Vui l??ng ch???n t??n kh??c!");
            }
        }
        Category category = result.get();
        category.setName(createCategoryRequest.getName());
        category.setStatus(createCategoryRequest.isStatus());
        category.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new InternalServerException("L???i khi ch???nh s???a danh m???c");
        }
    }

    @Override
    public void deleteCategory(long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh m???c kh??ng t???n t???i!");
        }

        //Check product in category
        long count = categoryRepository.checkProductInCategory(id);
        if (count > 0) {
            throw new BadRequestException("C?? s???n ph???m thu???c danh m???c kh??ng th??? x??a!");
        }

        try {
            categoryRepository.deleteById(id);
        } catch (Exception ex) {
            throw new InternalServerException("L???i khi x??a danh m???c!");
        }
    }


}
