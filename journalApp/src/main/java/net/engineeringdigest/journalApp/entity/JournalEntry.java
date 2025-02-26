package net.engineeringdigest.journalApp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.engineeringdigest.journalApp.enums.Sentiment;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "journal_entries")
/*

 This is a pojo class (plain old java object its contains no business logic only used for carrying data between ORM layers )
 This annotation is used to specify the collection names in mongodb.
 If no collection attribute is specified, the default collection name is the
 class name in camel case (e.g., JournalEntry -> journalEntry).
 **/
@NoArgsConstructor
@Data
@AllArgsConstructor
public class JournalEntry {

    @Id
    private ObjectId id;
    @NonNull
    private String title;
    private String content;
    private LocalDateTime date;
    private Sentiment sentiment;
}
