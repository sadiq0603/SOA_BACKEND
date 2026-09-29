package com.bibliotech.book.service;

import com.bibliotech.book.dto.BranchDto;
import com.bibliotech.book.dto.BranchRequest;
import com.bibliotech.book.entity.Branch;
import com.bibliotech.book.exception.BookNotFoundException;
import com.bibliotech.book.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchDto> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BranchDto createBranch(BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .location(request.getLocation())
                .active(true)
                .build();
        return mapToDto(branchRepository.save(branch));
    }

    @Transactional
    public BranchDto updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Branch not found with id: " + id));
        branch.setName(request.getName());
        branch.setLocation(request.getLocation());
        return mapToDto(branchRepository.save(branch));
    }

    @Transactional
    public BranchDto toggleBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Branch not found with id: " + id));
        branch.setActive(!branch.isActive());
        return mapToDto(branchRepository.save(branch));
    }

    @Transactional
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new BookNotFoundException("Branch not found with id: " + id);
        }
        branchRepository.deleteById(id);
    }

    private BranchDto mapToDto(Branch branch) {
        return BranchDto.builder()
                .id(branch.getId())
                .name(branch.getName())
                .location(branch.getLocation())
                .active(branch.isActive())
                .build();
    }
}
