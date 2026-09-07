package com.example.ex1.book.repository;

import com.example.ex1.book.model.Book;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    public void initData() {
        save(Book.builder().title("Clean Code").author("Robert C. Martin").isbn("978-0132350884").available(true).build());
        save(Book.builder().title("Design Patterns").author("Erich Gamma et al.").isbn("978-0201633610").available(true).build());
        save(Book.builder().title("Domain-Driven Design").author("Eric Evans").isbn("978-0321125217").available(true).build());
        save(Book.builder().title("Refactoring").author("Martin Fowler").isbn("978-0201485677").available(true).build());
        save(Book.builder().title("Effective Java").author("Joshua Bloch").isbn("978-0134685991").available(true).build());
        save(Book.builder().title("Spring in Action").author("Craig Walls").isbn("978-1617294945").available(true).build());
        save(Book.builder().title("Microservices Patterns").author("Chris Richardson").isbn("978-1617294549").available(true).build());
    }

    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idGenerator.incrementAndGet());
        }
        storage.put(book.getId(), book);
        return book;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
