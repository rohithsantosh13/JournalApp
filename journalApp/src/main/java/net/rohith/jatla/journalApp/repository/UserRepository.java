package net.rohith.jatla.journalApp.repository;

import net.rohith.jatla.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,ObjectId> {

    User findByUserName(String username);
    User deleteByUserName(String username);
    User findByEmail(String email);
}
