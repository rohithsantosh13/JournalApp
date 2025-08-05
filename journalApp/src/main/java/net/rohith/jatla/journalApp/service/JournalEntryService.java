package net.rohith.jatla.journalApp.service;

import net.rohith.jatla.journalApp.entity.JournalEntry;
import net.rohith.jatla.journalApp.entity.User;
import net.rohith.jatla.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    // This annotation is used to have the rollback feature
    // if at all the method fails at any point all the operation done in the method will be rolled back to the
    // initial state (just before the method is called meaning it cancels all the operation done in here)
    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
        User user = userService.findByUserName(userName);
        journalEntry.setDate(LocalDateTime.now());
        JournalEntry saved =  journalEntryRepository.save(journalEntry);
        user.getJournalEntries().add(saved);
        userService.saveUser(user);
    }

    public void saveEntry(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> getJournalEntryById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteJournalEntryById(ObjectId Id,String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getJournalEntries().removeIf(x -> x.getId().equals(Id));
            if(removed){
                userService.saveUser(user);
                journalEntryRepository.deleteById(Id);
            }

        }
        catch (Exception e){
            System.out.println(e);
            throw new RuntimeException("An error occurred while deleting the entry ",e);
        }
        return removed;
    }

}
