package com.falesdev.blog.service.impl;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.entity.Tag;
import com.falesdev.blog.mapper.TagMapper;
import com.falesdev.blog.repository.TagRepository;
import com.falesdev.blog.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> listTags() {
        return tagRepository.findAllWithPostCount().stream()
                .map(tagMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with ID " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> getTagByIds(Set<UUID> ids) {
        List<Tag> foundTags = tagRepository.findAllById(ids);
        if(foundTags.size() != ids.size()) {
            throw new EntityNotFoundException("Not all specified tag IDs exist");
        }
        return foundTags;
    }

    @Override
    @Transactional
    public List<TagDto> createTags(Set<String> tagNames) {
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTags = tagNames.stream()
                .filter(name ->!existingTagNames.contains(name))
                .map(name -> Tag.builder()
                        .name(name)
                        .posts(new HashSet<>())
                        .build())
                .toList();

        List<Tag> savedTags = new ArrayList<>();
        if(!newTags.isEmpty()){
            savedTags.addAll(tagRepository.saveAll(newTags));
        }

        savedTags.addAll(existingTags);
        return savedTags.stream().map(tagMapper::toDto).toList();
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));

        if (!tag.getPosts().isEmpty()) {
            throw new IllegalStateException("Cannot delete tag with posts");
        }

        tagRepository.deleteById(id);
    }
}
