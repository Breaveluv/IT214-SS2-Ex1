package com.example.ex1.borrowing.controller;

import com.example.ex1.borrowing.dto.BorrowRequest;
import com.example.ex1.borrowing.dto.BorrowResponse;
import com.example.ex1.borrowing.model.BorrowingRecord;
import com.example.ex1.borrowing.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping
    public ResponseEntity<BorrowResponse> borrowBook(@RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowingService.borrowBook(request));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowingRecord>> getBorrowingsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByMember(memberId));
    }

    @GetMapping
    public ResponseEntity<List<BorrowingRecord>> getAllBorrowings() {
        return ResponseEntity.ok(borrowingService.getAllBorrowings());
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.returnBook(id));
    }
}
