package net.rohith.jatla.journalApp.scheduler;

import net.rohith.jatla.journalApp.cache.AppCache;
import net.rohith.jatla.journalApp.entity.JournalEntry;
import net.rohith.jatla.journalApp.entity.User;
import net.rohith.jatla.journalApp.enums.Sentiment;
import net.rohith.jatla.journalApp.model.SentimentData;
import net.rohith.jatla.journalApp.repository.UserRepositoryImpl;
import net.rohith.jatla.journalApp.service.EmailService;
import net.rohith.jatla.journalApp.service.SentimentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private AppCache appCache;

    @Autowired
    private KafkaTemplate<String, SentimentData> kafkaTemplate;

//    @Scheduled(cron ="0 0 9 ? * SUN")
//    @Scheduled(cron ="0 * * ? * *")
    public void fetchUsersAndSendSaMail(){
        List<User> users = userRepository.getUsersForSA();
        for(User user : users){
            List<JournalEntry> journalEntries = user.getJournalEntries();
            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS))).map(x -> x.getSentiment()).collect(Collectors.toList());
            Map<Sentiment, Integer> sentimentCounts = new HashMap<>();
            for (Sentiment sentiment : sentiments) {
                if (sentiment != null)
                    sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
            }
            Sentiment mostFrequentSentiment = null;
            int maxCount = 0;
            for (Map.Entry<Sentiment, Integer> entry : sentimentCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostFrequentSentiment = entry.getKey();
                }
            }
            if (mostFrequentSentiment != null){
                SentimentData sentimentData = SentimentData.builder().email(user.getEmail()).sentiment( String.format("Hello %s Your mood for the last week looks like your %s",user.getUserName(),mostFrequentSentiment.toString())).build();
                try{
                    kafkaTemplate.send("weekly-sentiments",sentimentData.getEmail(),sentimentData);
                }
                catch(Exception e){
                    emailService.sendEmail(sentimentData.getEmail(),"Sentiment for previous week", sentimentData.getSentiment());
                }
            }
        }
    }


    @Scheduled(cron = "0 * * ? * *")
    public void clearAppCache(){
        appCache.init();
    }


}
