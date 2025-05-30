package com.falesdev.blog.mapper;

import com.falesdev.blog.domain.dto.PostDto;
import com.falesdev.blog.domain.dto.request.CreatePostRequestDto;
import com.falesdev.blog.domain.dto.request.UpdatePostRequestDto;
import com.falesdev.blog.domain.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "status", source = "status")
    PostDto toDto(Post post);

    @Mapping(target = "id", ignore = true)
    Post toCreatePost(CreatePostRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(UpdatePostRequestDto dto, @MappingTarget Post post);
}
