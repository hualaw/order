package com.interview.order.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationProperties {

    @Value("${notification.types:}")
    private String typesRaw;

    @Value("${notification.emails:}")
    private String emailsRaw;

    @Value("${notification.phones:}")
    private String phonesRaw;

    public List<String> getTypes() {
        if (typesRaw == null || typesRaw.isBlank()) return Collections.emptyList();
        return Arrays.stream(typesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getEmails() {
        if (emailsRaw == null || emailsRaw.isBlank()) return Collections.emptyList();
        return Arrays.stream(emailsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getPhones() {
        if (phonesRaw == null || phonesRaw.isBlank()) return Collections.emptyList();
        return Arrays.stream(phonesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

