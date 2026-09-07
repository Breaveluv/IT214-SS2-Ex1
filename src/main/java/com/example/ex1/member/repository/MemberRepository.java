package com.example.ex1.member.repository;

import com.example.ex1.member.model.Member;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemberRepository {

    private final Map<Long, Member> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    public void initData() {
        save(Member.builder().name("Nguyen Van A").email("a.nguyen@example.com").phone("0901234567").build());
        save(Member.builder().name("Tran Thi B").email("b.tran@example.com").phone("0912345678").build());
        save(Member.builder().name("Le Van C").email("c.le@example.com").phone("0923456789").build());
    }

    public List<Member> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Member save(Member member) {
        if (member.getId() == null) {
            member.setId(idGenerator.incrementAndGet());
        }
        storage.put(member.getId(), member);
        return member;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}
