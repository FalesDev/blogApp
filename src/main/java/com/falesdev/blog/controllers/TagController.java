package com.falesdev.blog.controllers;

import com.falesdev.blog.domain.dtos.CategoryDto;
import com.falesdev.blog.domain.dtos.TagDto;
import com.falesdev.blog.domain.dtos.requests.CreateCategoryRequest;
import com.falesdev.blog.domain.dtos.requests.CreateTagRequest;
import com.falesdev.blog.domain.entities.Category;
import com.falesdev.blog.domain.entities.Tag;
import com.falesdev.blog.mappers.TagMapper;
import com.falesdev.blog.services.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagDto>> listTags(){
        List<TagDto> tags = tagService.listTags()
                .stream().map(tagMapper::toDto)
                .toList();
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(
            @Valid @RequestBody CreateTagRequest createTagRequest){
        List<Tag> savedTag = tagService.createTags(createTagRequest.getNames());
        List<TagDto> createdTagDtos = savedTag.stream().map(tagMapper::toDto).toList();
        return new ResponseEntity<>(
                createdTagDtos,
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id){
        tagService.deleteTag(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
