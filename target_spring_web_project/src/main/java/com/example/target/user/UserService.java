package com.example.target.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> list() {
        return repo.findAll();
    }

    public Optional<User> get(Long id) {
        return repo.findById(id);
    }

    public User create(User user) {
        return repo.save(user);
    }

    public Optional<User> update(Long id, User user) {
        return repo.findById(id).map(existing -> {
            existing.setName(user.getName());
            existing.setEmail(user.getEmail());
            return repo.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repo.delete(id);
    }
}

