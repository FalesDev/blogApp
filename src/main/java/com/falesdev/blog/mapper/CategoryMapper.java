package com.falesdev.blog.mapper;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dto.CategoryDto;
import com.falesdev.blog.domain.dto.request.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dto.request.UpdateCategoryRequestDto;
import com.falesdev.blog.domain.entity.Category;
import com.falesdev.blog.domain.entity.Post;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCreateCategory(CreateCategoryRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "posts", ignore = true)
    void updateFromDto(UpdateCategoryRequestDto dto, @MappingTarget Category entity);

    @Named("calculatePostCount")
    default Integer calculatePostCount(List<Post> posts){
        if (posts == null){
            return 0;
        }

        return (int) posts.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
