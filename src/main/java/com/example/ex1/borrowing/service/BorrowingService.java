package com.example.ex1.borrowing.service;

import com.example.ex1.book.model.Book;
import com.example.ex1.book.service.BookService;
import com.example.ex1.borrowing.dto.BorrowRequest;
import com.example.ex1.borrowing.dto.BorrowResponse;
import com.example.ex1.borrowing.model.BorrowStatus;
import com.example.ex1.borrowing.model.BorrowingRecord;
import com.example.ex1.borrowing.repository.BorrowingRepository;
import com.example.ex1.common.exception.BorrowLimitExceededException;
import com.example.ex1.common.exception.ResourceNotFoundException;
import com.example.ex1.member.model.Member;
import com.example.ex1.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private static final int MAX_BORROW_LIMIT = 5;

    private final BorrowingRepository borrowingRepository;
    private final BookService bookService;
    private final MemberService memberService;

    /**
     * Sửa lỗi logic: Mỗi độc giả chỉ được mượn tối đa 5 cuốn cùng lúc.
     * Khi đã mượn >= 5 cuốn thì KHÔNG cho mượn tiếp.
     */
    public boolean canBorrowBook(int currentBorrowedByMember) {
        return currentBorrowedByMember < MAX_BORROW_LIMIT;
    }

    /**
     * Mượn sách: Đã loại bỏ biến static 'totalBorrowedBooks' dùng chung.
     * Trạng thái được kiểm soát theo từng độc giả qua Repository.
     */
    public synchronized BorrowResponse borrowBook(BorrowRequest request) {
        if (request.getMemberId() == null || request.getBookId() == null) {
            throw new IllegalArgumentException("Member ID and Book ID must not be null");
        }

        Member member = memberService.getMemberById(request.getMemberId());
        Book book = bookService.getBookById(request.getBookId());

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book '" + book.getTitle() + "' is currently unavailable");
        }

        int currentBorrowed = borrowingRepository.countByMemberIdAndStatus(member.getId(), BorrowStatus.BORROWED);

        if (!canBorrowBook(currentBorrowed)) {
            throw new BorrowLimitExceededException(
                    String.format("Member '%s' (ID: %d) has already borrowed %d books. Maximum allowed is %d.",
                            member.getName(), member.getId(), currentBorrowed, MAX_BORROW_LIMIT)
            );
        }

        bookService.updateBookAvailability(book.getId(), false);

        BorrowingRecord record = BorrowingRecord.builder()
                .memberId(member.getId())
                .bookId(book.getId())
                .borrowDate(LocalDateTime.now())
                .status(BorrowStatus.BORROWED)
                .build();
        BorrowingRecord savedRecord = borrowingRepository.save(record);

        return BorrowResponse.builder()
                .borrowingId(savedRecord.getId())
                .memberId(member.getId())
                .memberName(member.getName())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .borrowDate(savedRecord.getBorrowDate())
                .status(savedRecord.getStatus())
                .currentBorrowedByMember(currentBorrowed + 1)
                .message("Book borrowed successfully")
                .build();
    }

    public synchronized BorrowResponse returnBook(Long borrowingId) {
        BorrowingRecord record = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with id: " + borrowingId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new IllegalStateException("This book has already been returned");
        }

        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnDate(LocalDateTime.now());
        borrowingRepository.save(record);

        bookService.updateBookAvailability(record.getBookId(), true);

        Member member = memberService.getMemberById(record.getMemberId());
        Book book = bookService.getBookById(record.getBookId());
        int currentBorrowed = borrowingRepository.countByMemberIdAndStatus(member.getId(), BorrowStatus.BORROWED);

        return BorrowResponse.builder()
                .borrowingId(record.getId())
                .memberId(member.getId())
                .memberName(member.getName())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .borrowDate(record.getBorrowDate())
                .status(record.getStatus())
                .currentBorrowedByMember(currentBorrowed)
                .message("Book returned successfully")
                .build();
    }

    public List<BorrowingRecord> getBorrowingsByMember(Long memberId) {
        memberService.getMemberById(memberId);
        return borrowingRepository.findByMemberId(memberId);
    }

    public List<BorrowingRecord> getAllBorrowings() {
        return borrowingRepository.findAll();
    }
}
