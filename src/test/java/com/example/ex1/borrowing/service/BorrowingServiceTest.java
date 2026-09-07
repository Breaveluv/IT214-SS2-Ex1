package com.example.ex1.borrowing.service;

import com.example.ex1.book.repository.BookRepository;
import com.example.ex1.book.service.BookService;
import com.example.ex1.borrowing.dto.BorrowRequest;
import com.example.ex1.borrowing.dto.BorrowResponse;
import com.example.ex1.borrowing.model.BorrowStatus;
import com.example.ex1.borrowing.repository.BorrowingRepository;
import com.example.ex1.common.exception.BorrowLimitExceededException;
import com.example.ex1.member.model.Member;
import com.example.ex1.member.repository.MemberRepository;
import com.example.ex1.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BorrowingServiceTest {

    private BorrowingService borrowingService;
    private BookService bookService;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        BookRepository bookRepository = new BookRepository();
        bookRepository.initData();
        bookService = new BookService(bookRepository);

        MemberRepository memberRepository = new MemberRepository();
        memberRepository.initData();
        memberService = new MemberService(memberRepository);

        BorrowingRepository borrowingRepository = new BorrowingRepository();
        borrowingService = new BorrowingService(borrowingRepository, bookService, memberService);
    }

    @ParameterizedTest(name = "Khi số sách đang mượn là {0} thì canBorrowBook trả về {1}")
    @CsvSource({
            "0, true",
            "1, true",
            "4, true",
            "5, false",
            "6, false"
    })
    @DisplayName("Kiểm tra logic canBorrowBook - Giới hạn tối đa 5 cuốn")
    void testCanBorrowBook(int currentBorrowed, boolean expected) {
        assertEquals(expected, borrowingService.canBorrowBook(currentBorrowed));
    }

    @Test
    @DisplayName("Độc giả mượn đến 5 cuốn thành công và bị từ chối ở cuốn thứ 6")
    void testBorrowLimitEnforcement() {
        Member member = memberService.getMemberById(1L);

        for (long bookId = 1; bookId <= 5; bookId++) {
            BorrowResponse response = borrowingService.borrowBook(
                    BorrowRequest.builder().memberId(member.getId()).bookId(bookId).build()
            );
            assertNotNull(response);
            assertEquals(bookId, response.getCurrentBorrowedByMember());
            assertEquals(BorrowStatus.BORROWED, response.getStatus());
        }

        BorrowLimitExceededException exception = assertThrows(
                BorrowLimitExceededException.class,
                () -> borrowingService.borrowBook(
                        BorrowRequest.builder().memberId(member.getId()).bookId(6L).build()
                )
        );

        assertTrue(exception.getMessage().contains("already borrowed 5 books"));
    }

    @Test
    @DisplayName("Không xảy ra shared mutable state: Độc giả A mượn 5 cuốn không ảnh hưởng tới độc giả B")
    void testIndependentMemberBorrowingState() {
        Member memberA = memberService.getMemberById(1L);
        Member memberB = memberService.getMemberById(2L);

        for (long bookId = 1; bookId <= 5; bookId++) {
            borrowingService.borrowBook(
                    BorrowRequest.builder().memberId(memberA.getId()).bookId(bookId).build()
            );
        }

        assertThrows(BorrowLimitExceededException.class, () ->
                borrowingService.borrowBook(
                        BorrowRequest.builder().memberId(memberA.getId()).bookId(6L).build()
                )
        );

        BorrowResponse responseB = borrowingService.borrowBook(
                BorrowRequest.builder().memberId(memberB.getId()).bookId(6L).build()
        );
        assertNotNull(responseB);
        assertEquals(1, responseB.getCurrentBorrowedByMember());
    }
}
