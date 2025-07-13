package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceImplTest {

    private BookingServiceImpl bookingService;

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private BookingMapper bookingMapper;

    private BookingResponseDto bookingResponseDto;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        bookingService = new BookingServiceImpl(userRepository, itemRepository, bookingRepository);

        user = new User(1L, "Booker", "booker@example.com");
        owner = new User(2L, "Owner", "owner@example.com");
        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);


        booking = new Booking(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                user,
                Status.WAITING
        );
    }

    @Test
    void create_shouldCreateBookingSuccessfully() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        booking.setId(1L);
        booking.setStatus(Status.WAITING);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.create(user.getId(), bookingDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(Status.WAITING, result.getStatus());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void create_shouldThrowValidationException_whenBookerIsOwner() {
        item.setOwner(user); // пользователь — владелец вещи

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowValidationException_whenItemUnavailable() {
        item.setAvailable(false);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowValidationException_whenInvalidDates() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1)); // start > end

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void update_shouldApproveBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDto result = bookingService.update(owner.getId(), 1L, true);

        assertEquals(Status.APPROVED, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void update_shouldThrowConflict_whenAlreadyApproved() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class,
                () -> bookingService.update(owner.getId(), 1L, false));
    }

    @Test
    void getById_shouldReturnBooking_whenBooker() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(1L, user.getId());

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_shouldThrow_whenUserNotOwnerOrBooker() {
        User anotherUser = new User(99L, "Stranger", "stranger@example.com");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class,
                () -> bookingService.getById(1L, anotherUser.getId()));
    }


    @Test
    void getAllByUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByUser(user.getId(), "ALL", 0, 10));
    }


    @Test
    void getAllByOwner_shouldThrow_whenOwnerNotFound() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwner(owner.getId(), "ALL", 0, 10));
    }

    @Test
    void create_shouldThrowNotFoundException_whenUserNotFound() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowNotFoundException_whenItemNotFound() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowValidationException_whenStartInPast() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class,
                () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void update_shouldThrowValidationException_whenUserNotOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        User notOwner = new User(99L, "NotOwner", "notowner@example.com");

        assertThrows(ValidationException.class,
                () -> bookingService.update(notOwner.getId(), 1L, true));
    }

    @Test
    void update_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.update(owner.getId(), 1L, true));
    }

    @Test
    void getById_shouldThrowConflictException_whenBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ConflictException.class,
                () -> bookingService.getById(1L, user.getId()));
    }

    @Test
    void getAllByUser_shouldThrowIllegalArgumentException_onUnknownState() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByUser(user.getId(), "UNKNOWN", 0, 10));
    }

    @Test
    void getAllByOwner_shouldThrowIllegalArgumentException_onUnknownState() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByOwner(owner.getId(), "UNKNOWN", 0, 10));
    }

    @Test
    void getAllByUser_shouldReturnEmptyList_whenNoBookings() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(user.getId()), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingResponseDto> result = bookingService.getAllByUser(user.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByOwner_shouldReturnEmptyList_whenNoBookings() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(Page.empty());

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "ALL",
            "CURRENT",
            "PAST",
            "FUTURE",
            "WAITING",
            "REJECTED"
    })
    void getAllByUser_shouldReturnBookingsForDifferentStates(String state) {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Page<Booking> bookingPage = new PageImpl<>(List.of(booking));
        Pageable anyPageable = PageRequest.of(0, 10);

        switch (state) {
            case "ALL":
                when(bookingRepository.findByBookerIdOrderByStartDesc(eq(user.getId()), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            case "CURRENT":
                when(bookingRepository.findCurrentByBookerId(
                        eq(user.getId()), any(LocalDateTime.class), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            case "PAST":
                when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                        eq(user.getId()), any(LocalDateTime.class), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            case "FUTURE":
                when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                        eq(user.getId()), any(LocalDateTime.class), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            case "WAITING":
                when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        eq(user.getId()), eq(Status.WAITING), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            case "REJECTED":
                when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        eq(user.getId()), eq(Status.REJECTED), any(Pageable.class)))
                        .thenReturn(bookingPage);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        List<BookingResponseDto> result = bookingService.getAllByUser(user.getId(), state, 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userRepository).findById(user.getId());
    }
}
