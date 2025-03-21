package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.entity.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TagService {

    List<TagDto> listTags();
    Tag getTagById(UUID id);
    List<Tag> getTagByIds(Set<UUID> ids);
    List<TagDto> createTags(Set<String> tagNames);
    void deleteTag(UUID id);

}
