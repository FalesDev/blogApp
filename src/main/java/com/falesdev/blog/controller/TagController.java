package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.dto.request.CreateTagRequestDto;
import com.falesdev.blog.service.TagService;
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

    @GetMapping
    public ResponseEntity<List<TagDto>> listTags(){
        return ResponseEntity.ok(tagService.listTags());
    }

    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(
            @Valid @RequestBody CreateTagRequestDto createTagRequestDto){
        return new ResponseEntity<>(
                tagService.createTags(createTagRequestDto.getNames()),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id){
        tagService.deleteTag(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
