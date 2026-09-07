package com.example.ex1;

import com.example.ex1.book.controller.BookController;
import com.example.ex1.book.repository.BookRepository;
import com.example.ex1.book.service.BookService;
import com.example.ex1.borrowing.controller.BorrowingController;
import com.example.ex1.borrowing.repository.BorrowingRepository;
import com.example.ex1.borrowing.service.BorrowingService;
import com.example.ex1.member.controller.MemberController;
import com.example.ex1.member.repository.MemberRepository;
import com.example.ex1.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LibraryEndpointsIntegrationTest {

    private MockMvc bookMockMvc;
    private MockMvc memberMockMvc;
    private MockMvc borrowingMockMvc;

    @BeforeEach
    void setUp() {
        BookRepository bookRepository = new BookRepository();
        bookRepository.initData();
        BookService bookService = new BookService(bookRepository);
        BookController bookController = new BookController(bookService);
        bookMockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        MemberRepository memberRepository = new MemberRepository();
        memberRepository.initData();
        MemberService memberService = new MemberService(memberRepository);
        MemberController memberController = new MemberController(memberService);
        memberMockMvc = MockMvcBuilders.standaloneSetup(memberController).build();

        BorrowingRepository borrowingRepository = new BorrowingRepository();
        BorrowingService borrowingService = new BorrowingService(borrowingRepository, bookService, memberService);
        BorrowingController borrowingController = new BorrowingController(borrowingService);
        borrowingMockMvc = MockMvcBuilders.standaloneSetup(borrowingController).build();
    }

    @Test
    @DisplayName("Test REST endpoint Book: GET /api/books/1")
    void testGetBookById() throws Exception {
        bookMockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @DisplayName("Test REST endpoint Member: GET /api/members/1")
    void testGetMemberById() throws Exception {
        memberMockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nguyen Van A")));
    }

    @Test
    @DisplayName("Test REST endpoint Borrowing: POST /api/borrowings")
    void testBorrowBookEndpoint() throws Exception {
        String json = "{\"memberId\": 2, \"bookId\": 2}";

        borrowingMockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId", is(2)))
                .andExpect(jsonPath("$.bookId", is(2)))
                .andExpect(jsonPath("$.status", is("BORROWED")));
    }
}
