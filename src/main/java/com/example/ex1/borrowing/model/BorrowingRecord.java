package com.example.ex1.borrowing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingRecord {
    private Long id;
    private Long memberId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private BorrowStatus status;
}
