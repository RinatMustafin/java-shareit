package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotFoundExceptionRequest;
import ru.practicum.shareit.exception.NotFoundOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);

        item = new Item();
        item.setId(1L);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
    }

    @Test
    void create_whenUserExistsAndRequestExists_thenCreateItem() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        itemDto.setRequestId(10L);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        ItemDto created = itemService.create(owner.getId(), itemDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(itemDto.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void create_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(1L, itemDto));
    }

    @Test
    void create_whenRequestNotFound_thenThrowNotFoundExceptionRequest() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        itemDto.setRequestId(99L);

        assertThrows(NotFoundExceptionRequest.class, () -> itemService.create(owner.getId(), itemDto));
    }

    @Test
    void update_whenOwnerMatches_thenUpdateItem() {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Drill");
        updateDto.setDescription("New description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("New Drill");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void update_whenItemNotFound_thenThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(owner.getId(), 1L, itemDto));
    }

    @Test
    void update_whenOwnerMismatch_thenThrowNotFoundOwnerException() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        item.setOwner(anotherUser);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundOwnerException.class, () -> itemService.update(owner.getId(), item.getId(), itemDto));
    }

    @Test
    void getById_whenItemExists_thenReturnItemDto() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(item.getId())).thenReturn(Collections.emptyList());
        when(bookingRepository.findTopByItemIdAndStartBeforeAndStatusOrderByEndDesc(
                anyLong(), any(LocalDateTime.class), any(Status.class))).thenReturn(null);
        when(bookingRepository.findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(
                anyLong(), any(LocalDateTime.class), any(Status.class))).thenReturn(null);

        ItemDto result = itemService.getById(item.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(item.getName());
    }

    @Test
    void getById_whenItemNotFound_thenThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(1L, owner.getId()));
    }

    @Test
    void search_whenTextIsBlank_thenReturnEmptyList() {
        List<ItemDto> results = itemService.search("  ");
        assertThat(results).isEmpty();
    }

    @Test
    void search_whenTextIsValid_thenReturnList() {
        when(itemRepository.findByAvailableTrueAndNameContainingIgnoreCaseOrAvailableTrueAndDescriptionContainingIgnoreCase(
                anyString(), anyString())).thenReturn(List.of(item));

        List<ItemDto> results = itemService.search("drill");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo(item.getName());
    }

    @Test
    void addComment_whenUserHasBooking_thenAddComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice item!");

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(owner.getId()), eq(item.getId()), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto result = itemService.addComment(owner.getId(), item.getId(), commentDto);

        assertThat(result.getText()).isEqualTo("Nice item!");
    }

    @Test
    void addComment_whenUserHasNoBooking_thenThrowValidationException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice item!");

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(owner.getId()), eq(item.getId()), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(owner.getId(), item.getId(), commentDto));
    }

    @Test
    void addComment_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.addComment(99L, item.getId(), new CommentDto()));
    }

    @Test
    void addComment_whenItemNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.addComment(owner.getId(), 99L, new CommentDto()));
    }

    @Test
    void addComment_whenNoBooking_thenThrowValidationException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(owner.getId()), eq(item.getId()), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () ->
                itemService.addComment(owner.getId(), item.getId(), new CommentDto()));
    }

    @Test
    void getAllByOwner_shouldReturnItemsList() {
        when(itemRepository.findAllByOwnerId(owner.getId()))
                .thenReturn(List.of(item));

        List<ItemDto> result = itemService.getAllByOwner(owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(item.getName());
    }
}
