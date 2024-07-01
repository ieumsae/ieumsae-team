    package com.ieumsae.project.repository;

    import com.ieumsae.project.domain.User;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;


    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByUserName(String user_name);
        Optional<User> findByUserNickName(String userNickName);
        Optional<User> findByUserPhone(String userPhone);
        Optional<User> findByUserEmail(String userEmail);
    }
