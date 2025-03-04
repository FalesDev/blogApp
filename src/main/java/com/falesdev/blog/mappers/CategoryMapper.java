package com.falesdev.blog.mappers;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dtos.CategoryDto;
import com.falesdev.blog.domain.dtos.requests.CreateCategoryRequestDto;
import com.falesdev.blog.domain.dtos.requests.UpdateCategoryRequestDto;
import com.falesdev.blog.domain.entities.Category;
import com.falesdev.blog.domain.entities.Post;
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
    default long calculatePostCount(List<Post> posts){
        if (posts == null){
            return 0;
        }

        return posts.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
