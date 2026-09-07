package com.example.ex1.member.service;

import com.example.ex1.common.exception.ResourceNotFoundException;
import com.example.ex1.member.model.Member;
import com.example.ex1.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    public Member addMember(Member member) {
        return memberRepository.save(member);
    }
}
