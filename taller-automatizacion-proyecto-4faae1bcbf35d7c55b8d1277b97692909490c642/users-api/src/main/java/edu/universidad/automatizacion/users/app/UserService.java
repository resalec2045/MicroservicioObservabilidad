package edu.universidad.automatizacion.users.app;

import edu.universidad.automatizacion.users.domain.InMemoryUserRepository;
import edu.universidad.automatizacion.users.domain.UserDTO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final InMemoryUserRepository repo = new InMemoryUserRepository();

    public UserDTO create(UserDTO u) {
        logger.debug("Saving new user: {}", u);
        return repo.save(u);
    }

    public UserDTO update(String id, UserDTO u) {
        logger.debug("Updating user: {} with data: {}", id, u);
        return repo.save(new UserDTO(id, u.name(), u.email()));
    }

    public UserDTO get(String id) {
        logger.debug("Getting user by id: {}", id);
        return repo.findById(id).orElse(null);
    }

    public Optional<UserDTO> findOptional(String id) {
        logger.debug("Finding user by id (optional): {}", id);
        return repo.findById(id);
    }

    public List<UserDTO> list() {
        logger.debug("Listing all users");
        return repo.findAll();
    }

    public boolean delete(String id) {
        logger.debug("Deleting user by id: {}", id);
        return repo.delete(id);
    }
}
