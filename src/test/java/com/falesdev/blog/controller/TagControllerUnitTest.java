package com.falesdev.blog.controller;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.dto.request.CreateTagRequestDto;
import com.falesdev.blog.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class TagControllerUnitTest {

    @InjectMocks
    private TagController tagController;

    @Mock
    private TagService tagService;

    private TagDto tagDto1,tagDto2,expectedCreatedTagDto1,expectedCreatedTagDto2;
    private CreateTagRequestDto createTagRequestDto;

    @BeforeEach
    void setUp() {
        // DTO to list/get
        tagDto1 = TagDto.builder().id(UUID.randomUUID()).name("Development").postCount(2).build();
        tagDto2 = TagDto.builder().id(UUID.randomUUID()).name("Git").postCount(5).build();

        // DTOs for the creation test
        createTagRequestDto = CreateTagRequestDto.builder().names(Set.of("IntelliJ IDEA","Visual Studio")).build();
        expectedCreatedTagDto1 = TagDto.builder()
                .id(UUID.randomUUID())
                .name("IntelliJ IDEA")
                .postCount(9)
                .build();
        expectedCreatedTagDto2 = TagDto.builder()
                .id(UUID.randomUUID())
                .name("Visual Studio")
                .postCount(5).build();
    }

    @Test
    @DisplayName("Success Get Tags")
    void listTags_ShouldReturnListOfTags() {
        List<TagDto> tagList = List.of(tagDto1,tagDto2);
        when(tagService.listTags()).thenReturn(tagList);

        ResponseEntity<List<TagDto>> response = tagController.listTags();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tagList).hasSize(2);

        verify(tagService).listTags();
    }

    @Test
    @DisplayName("Success Create Tags")
    void createTags_ShouldReturnCreatedTags() {
        List<TagDto> expectedCreatedTagList = List.of(expectedCreatedTagDto1,expectedCreatedTagDto2);
        when(tagService.createTags(eq(createTagRequestDto.getNames())))
                .thenReturn(expectedCreatedTagList);

        ResponseEntity<List<TagDto>> response = tagController.createTags(createTagRequestDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedCreatedTagList);

        verify(tagService).createTags(eq(createTagRequestDto.getNames()));
    }

    @Test
    @DisplayName("Success Delete Tag")
    void deleteTag_ShouldReturnNoContent() {
        UUID tagId = UUID.randomUUID();
        doNothing().when(tagService).deleteTag(eq(tagId));

        ResponseEntity<Void> response = tagController.deleteTag(tagId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(tagService, times(1)).deleteTag(eq(tagId));
    }
}
