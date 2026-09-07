package com.example.ex1.borrowing.dto;

import com.example.ex1.borrowing.model.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowResponse {
    private Long borrowingId;
    private Long memberId;
    private String memberName;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowDate;
    private BorrowStatus status;
    private int currentBorrowedByMember;
    private String message;
}
