package com.mattseq.authservice.service;

import com.mattseq.authservice.domain.Role;
import com.mattseq.authservice.domain.User;
import com.mattseq.authservice.dto.UpdateUserRequest;
import com.mattseq.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public Optional<User> createUser(User user) {
        if (repo.findByUsername(user.getUsername()).isPresent()) {
            return Optional.empty();
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return Optional.of(repo.save(user));
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = repo.findByUsername(username);
        if (userOpt.isPresent() && BCrypt.checkpw(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public Optional<User> initializeAdmin(User user) {
        if (repo.existsByRole(Role.ADMIN)) {
            return Optional.empty();
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return Optional.of(repo.save(user));
    }

    public Optional<User> updateUser(Long id, UpdateUserRequest request) {
        return repo.findById(id).map(existingUser -> {
            if (request.getUsername() != null) existingUser.setUsername(request.getUsername());
            if (request.getEmail() != null) existingUser.setEmail(request.getEmail());
            if (request.getPassword() != null) {
                existingUser.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            }
            return repo.save(existingUser);
        });
    }

    public Optional<User> updateRole(Long id, Role role) {
        return repo.findById(id).map(existingUser -> {
            if (role != null) existingUser.setRole(role);
            return repo.save(existingUser);
        });
    }
}
