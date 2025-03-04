package com.falesdev.blog.services;

import com.falesdev.blog.domain.dtos.TagDto;
import com.falesdev.blog.domain.entities.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TagService {

    List<TagDto> listTags();
    List<TagDto> createTags(Set<String> tagNames);
    void deleteTag(UUID id);
    Tag getTagById(UUID id);
    List<Tag> getTagByIds(Set<UUID> ids);
}
