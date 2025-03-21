package com.falesdev.blog.mapper;

import com.falesdev.blog.domain.PostStatus;
import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.entity.Post;
import com.falesdev.blog.domain.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
    TagDto toDto(Tag tag);

    @Named("calculatePostCount")
    default Integer calculatePostCount(Set<Post> posts){
        if (posts == null){
            return 0;
        }

        return (int) posts.stream()
                .filter(post -> PostStatus.PUBLISHED.equals(post.getStatus()))
                .count();
    }
}
