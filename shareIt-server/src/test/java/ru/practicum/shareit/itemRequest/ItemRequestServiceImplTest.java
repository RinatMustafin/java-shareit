package ru.practicum.shareit.itemRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@mail.com");
        request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need drill");
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        requestDto = new ItemRequestDto(null, "Need drill", null);
    }

    @Test
    void create_whenUserExists_thenReturnCreatedRequest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(requestRepository.save(Mockito.any(ItemRequest.class)))
                .thenAnswer(invocation -> {
                    ItemRequest input = invocation.getArgument(0);
                    input.setId(1L);
                    return input;
                });

        ItemRequestDto created = service.create(user.getId(), requestDto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getDescription()).isEqualTo(requestDto.getDescription());

        verify(requestRepository).save(Mockito.any(ItemRequest.class));
    }

    @Test
    void create_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(user.getId(), requestDto));
    }

    @Test
    void getOwnRequests_whenUserExists_thenReturnRequestsWithItems() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(user.getId()))
                .thenReturn(List.of(request));
        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setOwner(user);
        item.setRequest(request);
        when(itemRepository.findByRequestIdIn(List.of(request.getId())))
                .thenReturn(List.of(item));

        List<ItemRequestWithResponsesDto> requests = service.getOwnRequests(user.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getItems()).hasSize(1);
        assertThat(requests.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getAllRequests_withPagination_thenReturnRequestsWithItems() {
        long anotherUserId = 2L;
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ItemRequest otherRequest = new ItemRequest();
        otherRequest.setId(2L);
        otherRequest.setDescription("Need hammer");
        otherRequest.setRequestor(new User(anotherUserId, "Other", "other@mail.com"));
        otherRequest.setCreated(LocalDateTime.now());

        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(user.getId(), PageRequest.of(0, 10)))
                .thenReturn(List.of(otherRequest));

        when(itemRepository.findByRequestIdIn(List.of(otherRequest.getId()))).thenReturn(List.of());

        List<ItemRequestWithResponsesDto> allRequests = service.getAllRequests(user.getId(), 0, 10);

        assertThat(allRequests).hasSize(1);
        assertThat(allRequests.get(0).getDescription()).isEqualTo("Need hammer");
        assertThat(allRequests.get(0).getItems()).isEmpty();
    }

    @Test
    void getByRequestId_whenRequestExists_thenReturnRequestWithItems() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        Item item = new Item();
        item.setId(5L);
        item.setName("Drill");
        item.setOwner(user);
        item.setRequest(request);

        when(itemRepository.findByRequestId(request.getId())).thenReturn(List.of(item));

        ItemRequestWithResponsesDto dto = service.getByRequestId(user.getId(), request.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getByRequestId_whenRequestNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByRequestId(user.getId(), 999L));
    }
}

