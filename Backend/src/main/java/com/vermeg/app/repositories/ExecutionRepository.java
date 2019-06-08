package com.vermeg.app.repositories;


import com.vermeg.app.models.Execution;
import com.vermeg.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ExecutionRepository extends JpaRepository<Execution,Long> {


    List<Execution> findByOwner(User user);
}
