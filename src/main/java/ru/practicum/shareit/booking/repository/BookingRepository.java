package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    Page<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now, Pageable pageable);

    Page<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND :now BETWEEN b.start AND b.end ORDER BY b.start DESC")
    Page<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable pageable);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId,
            Long itemId,
            Status status,
            LocalDateTime endBefore
    );

    Booking findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, Status status);

    Booking findTopByItemIdAndStartBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime now, Status status);
}
