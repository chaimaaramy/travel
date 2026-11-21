
package com.travelbook.serviceutilisateurs.service;

import com.travelbook.serviceutilisateurs.dto.UserRequestDTO;
import com.travelbook.serviceutilisateurs.dto.UserResponseDTO;
import com.travelbook.serviceutilisateurs.entity.User;
import com.travelbook.serviceutilisateurs.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long id) {
        User user = findUser(id);
        return toDTO(user);
    }

    public UserResponseDTO createUser(UserRequestDTO dto) {
        User user = new User();

        user.setNom(dto.nom());
        user.setEmail(dto.email());
        user.setRole(dto.role());
        user.setActif(dto.actif());

        return toDTO(userRepository.save(user));
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = findUser(id);

        user.setNom(dto.nom());
        user.setEmail(dto.email());
        user.setRole(dto.role());
        user.setActif(dto.actif());

        return toDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable : " + id
                ));
    }

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNom(),
                user.getEmail(),
                user.getRole(),
                user.getActif()
        );
    }
}
