package com.example.ex1.book.service;

import com.example.ex1.book.model.Book;
import com.example.ex1.book.repository.BookRepository;
import com.example.ex1.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book addBook(Book book) {
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    public void updateBookAvailability(Long bookId, boolean available) {
        Book book = getBookById(bookId);
        book.setAvailable(available);
        bookRepository.save(book);
    }
}
