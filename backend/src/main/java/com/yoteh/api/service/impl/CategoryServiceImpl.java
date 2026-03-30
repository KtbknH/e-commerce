package com.yoteh.api.service.impl;

import com.yoteh.api.dto.request.CategoryRequest;
import com.yoteh.api.dto.response.CategoryResponse;
import com.yoteh.api.dto.response.common.PagedResponse;
import com.yoteh.api.entity.Category;
import com.yoteh.api.exception.DuplicateResourceException;
import com.yoteh.api.exception.ResourceNotFoundException;
import com.yoteh.api.mapper.CategoryMapper;
import com.yoteh.api.repository.CategoryRepository;
import com.yoteh.api.service.CategoryService;
import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS PUBLICS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        log.debug("Récupération de l'arbre de catégories");
        List<Category> roots = categoryRepository.findRootCategoriesWithChildren();
        return roots.stream().map(categoryMapper::toTreeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories =
                categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<Category> roots = categoryRepository.findRootCategories();
        return categoryMapper.toResponseList(roots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(UUID parentId) {
        // Vérifier que le parent existe
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category", "id", parentId.toString());
        }
        List<Category> children = categoryRepository.findByParentId(parentId);
        return categoryMapper.toResponseList(children);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(String type) {
        List<Category> categories =
                categoryRepository.findByTypeAndIsActiveTrueOrderBySortOrderAsc(type);
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category =
                categoryRepository
                        .findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        CategoryResponse response = categoryMapper.toTreeResponse(category);
        response.setProductCount(
                (int) categoryRepository.countProductsByCategoryId(category.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category", "id", id.toString()));
        CategoryResponse response = categoryMapper.toTreeResponse(category);
        response.setProductCount(
                (int) categoryRepository.countProductsByCategoryId(category.getId()));
        return response;
    }

    // ═══════════════════════════════════════════════════════════
    //  ENDPOINTS ADMIN
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAllCategories(
            String search, String type, Boolean isActive, UUID parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<Category> categoryPage =
                categoryRepository.findAllWithFilters(search, type, isActive, parentId, pageable);

        List<CategoryResponse> content =
                categoryPage.getContent().stream()
                        .map(
                                cat -> {
                                    CategoryResponse resp = categoryMapper.toResponse(cat);
                                    resp.setProductCount(
                                            (int)
                                                    categoryRepository.countProductsByCategoryId(
                                                            cat.getId()));
                                    return resp;
                                })
                        .collect(Collectors.toList());

        return PagedResponse.<CategoryResponse>builder()
                .content(content)
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Création de la catégorie : {}", request.getName());

        // Vérifier l'unicité du nom
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(generateUniqueSlug(request.getName()));

        // Assigner le parent si spécifié
        if (request.getParentId() != null) {
            Category parent =
                    categoryRepository
                            .findById(request.getParentId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Category",
                                                    "parentId",
                                                    request.getParentId().toString()));
            category.setParent(parent);
        }

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        Category saved = categoryRepository.save(category);
        log.info("Catégorie créée avec succès : {} ({})", saved.getName(), saved.getId());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Mise à jour de la catégorie : {}", id);

        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category", "id", id.toString()));

        // Vérifier l'unicité du nom si modifié
        if (request.getName() != null
                && !request.getName().equals(category.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        categoryMapper.updateEntityFromRequest(request, category);

        // Mettre à jour le slug si le nom change
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            category.setSlug(generateUniqueSlug(request.getName()));
        }

        // Mettre à jour le parent
        if (request.getParentId() != null) {
            // Empêcher une catégorie d'être son propre parent
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException(
                        "Une catégorie ne peut pas être son propre parent");
            }
            Category parent =
                    categoryRepository
                            .findById(request.getParentId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Category",
                                                    "parentId",
                                                    request.getParentId().toString()));
            category.setParent(parent);
        }

        Category updated = categoryRepository.save(category);
        log.info("Catégorie mise à jour : {}", updated.getName());
        return categoryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category", "id", id.toString()));

        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer la catégorie '"
                            + category.getName()
                            + "' : elle contient "
                            + productCount
                            + " produit(s)");
        }

        log.info("Suppression de la catégorie : {} ({})", category.getName(), id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryResponse toggleCategoryStatus(UUID id) {
        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Category", "id", id.toString()));

        category.setIsActive(!category.getIsActive());
        Category updated = categoryRepository.save(category);
        log.info(
                "Catégorie {} {} ",
                updated.getName(),
                updated.getIsActive() ? "activée" : "désactivée");
        return categoryMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    private String generateUniqueSlug(String name) {
        String baseSlug = slugify(name);
        String slug = baseSlug;
        int counter = 1;
        while (categoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
