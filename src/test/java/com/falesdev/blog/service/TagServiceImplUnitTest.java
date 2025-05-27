package com.falesdev.blog.service;

import com.falesdev.blog.domain.dto.TagDto;
import com.falesdev.blog.domain.entity.Post;
import com.falesdev.blog.domain.entity.Tag;
import com.falesdev.blog.mapper.TagMapper;
import com.falesdev.blog.repository.TagRepository;
import com.falesdev.blog.service.impl.TagServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class TagServiceImplUnitTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag1;
    private Tag tag2;
    private TagDto tagDto1;
    private TagDto tagDto2;

    @BeforeEach
    void setUp() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        tag1 = Tag.builder()
                .id(id1)
                .name("Java")
                .posts(new HashSet<>())
                .build();

        tag2 = Tag.builder()
                .id(id2)
                .name("Spring")
                .posts(new HashSet<>())
                .build();

        tagDto1 = TagDto.builder().id(id1).name("Java").postCount(0).build();
        tagDto2 = TagDto.builder().id(id2).name("Spring").postCount(0).build();
    }

    @Test
    @DisplayName("List all tags - Success")
    void listTags_ShouldReturnAllTags() {
        List<Tag> tags = List.of(tag1, tag2);
        when(tagRepository.findAllWithPostCount()).thenReturn(tags);
        when(tagMapper.toDto(tag1)).thenReturn(tagDto1);
        when(tagMapper.toDto(tag2)).thenReturn(tagDto2);

        // Act
        List<TagDto> result = tagService.listTags();

        // Assert
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(tagDto1, tagDto2);
        verify(tagRepository).findAllWithPostCount();
        verify(tagMapper, times(2)).toDto(any(Tag.class));
    }

    @Test
    @DisplayName("Get tags by valid IDs - Success")
    void getTagsByIds_ValidIds_ReturnsTags() {
        Set<UUID> ids = Set.of(tag1.getId(), tag2.getId());
        when(tagRepository.findAllById(eq(ids))).thenReturn(List.of(tag1, tag2));

        List<Tag> result = tagService.getTagByIds(ids);

        assertThat(result)
                .hasSize(2)
                .containsExactly(tag1, tag2);
        verify(tagRepository).findAllById(eq(ids));
    }

    @Test
    @DisplayName("Get tags with missing IDs - Throws Exception")
    void getTagsByIds_MissingIds_ThrowsException() {
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        when(tagRepository.findAllById(eq(ids))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> tagService.getTagByIds(ids))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Not all specified tag IDs exist");
        verify(tagRepository).findAllById(eq(ids));
    }

    @Test
    @DisplayName("Create new tags - Success")
    void createTags_NewTags_ReturnsCreatedTags() {
        Set<String> tagNames = Set.of("Java", "Kotlin");
        List<Tag> existingTags = List.of(tag1);
        List<Tag> newTags = List.of(
                Tag.builder().name("Kotlin").posts(new HashSet<>()).build()
        );

        when(tagRepository.findByNameIn(tagNames)).thenReturn(existingTags);
        when(tagRepository.saveAll(eq(newTags))).thenReturn(new ArrayList<>(newTags));
        when(tagMapper.toDto(any(Tag.class)))
                .thenAnswer(inv -> TagDto.builder()
                        .name(((Tag) inv.getArgument(0)).getName())
                        .build());

        List<TagDto> result = tagService.createTags(tagNames);

        assertThat(result)
                .hasSize(2)
                .extracting(TagDto::getName)
                .containsExactlyInAnyOrder("Java", "Kotlin");
        verify(tagRepository).saveAll(eq(newTags));
        verify(tagMapper, times(2)).toDto(any(Tag.class));
    }

    @Test
    @DisplayName("Create tags with empty input - Returns Empty List")
    void createTags_EmptyInput_ReturnsEmptyList() {
        List<TagDto> result = tagService.createTags(Collections.emptySet());

        assertThat(result).isEmpty();
        verify(tagRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Delete tag without posts - Success")
    void deleteTag_WithoutPosts_DeletesTag() {
        when(tagRepository.findById(eq(tag1.getId()))).thenReturn(Optional.of(tag1));

        tagService.deleteTag(tag1.getId());

        verify(tagRepository, times(1)).deleteById(eq(tag1.getId()));
    }

    @Test
    @DisplayName("Delete tag with posts - Throws Exception")
    void deleteTag_WithPosts_ThrowsException() {
        tag1.getPosts().add(Post.builder().build());
        when(tagRepository.findById(eq(tag1.getId()))).thenReturn(Optional.of(tag1));

        assertThatThrownBy(() -> tagService.deleteTag(tag1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete tag with posts");
        verify(tagRepository).findById(eq(tag1.getId()));
        verify(tagRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete nonexistent tag - Throws Exception")
    void deleteTag_NonExistentTag_ThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(tagRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTag(invalidId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tag not found with id");
        verify(tagRepository).findById(eq(invalidId));
        verify(tagRepository, never()).delete(any());
    }
}
