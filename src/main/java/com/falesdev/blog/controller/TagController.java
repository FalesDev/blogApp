package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.dto.request.CreateTagRequestDto;
import com.falesdev.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tag", description = "Controller for Tags")
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "Get all tags",
            description = "Returns a list of all tags"
    )
    @GetMapping
    public ResponseEntity<List<TagDto>> listTags(){
        return ResponseEntity.ok(tagService.listTags());
    }

    @Operation(
            summary = "Create new tags",
            description = "Creates multiple tags from a list of names and returns the created entities"
    )
    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(
            @Valid @RequestBody CreateTagRequestDto createTagRequestDto){
        return new ResponseEntity<>(
                tagService.createTags(createTagRequestDto.getNames()),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Delete tag by ID",
            description = "Delete a tag by its identifier"
    )
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id){
        tagService.deleteTag(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
