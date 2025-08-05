package net.rohith.jatla.journalApp.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "config_journal_app")
/*

 This is a pojo class (plain old java object its contains no business logic only used for carrying data between ORM layers )
 This annotation is used to specify the collection names in mongodb.
 If no collection attribute is specified, the default collection name is the
 class name in camel case (e.g., JournalEntry -> journalEntry).
 **/
@Setter
@Getter
@NoArgsConstructor
public class ConfigJournalAppEntity {
    private String key;
    private String value;
}
