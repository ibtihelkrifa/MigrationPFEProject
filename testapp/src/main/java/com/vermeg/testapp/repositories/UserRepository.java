package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {



    User findUserByEmail(String mail);


}
