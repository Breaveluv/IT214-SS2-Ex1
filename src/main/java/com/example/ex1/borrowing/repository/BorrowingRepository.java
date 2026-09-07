package com.example.ex1.borrowing.repository;

import com.example.ex1.borrowing.model.BorrowStatus;
import com.example.ex1.borrowing.model.BorrowingRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BorrowingRepository {

    private final Map<Long, BorrowingRecord> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public BorrowingRecord save(BorrowingRecord record) {
        if (record.getId() == null) {
            record.setId(idGenerator.incrementAndGet());
        }
        storage.put(record.getId(), record);
        return record;
    }

    public Optional<BorrowingRecord> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<BorrowingRecord> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<BorrowingRecord> findByMemberId(Long memberId) {
        return storage.values().stream()
                .filter(record -> record.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public int countByMemberIdAndStatus(Long memberId, BorrowStatus status) {
        return (int) storage.values().stream()
                .filter(record -> record.getMemberId().equals(memberId) && record.getStatus() == status)
                .count();
    }
}
