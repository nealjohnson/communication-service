package com.squirrel.communication.model;

import com.squirrel.communication.service.impl.EmailTypeEnum;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.UUID;

@Data
public class EmailModel {
    private UUID messageUUID;
    private EmailTypeEnum emailType;
    private String addresseName;
    private String addresseEmailAddress;
    private String subject;
    private String message;

    //will be providing service
    private String senderName;

    private String emailTemplateFile;

    private String imageResource;

    //For appointment Email
    private String linkToBeShown;
    private Long appointmentDateTime;


    //for prescription email

    private String expertise;
    private int age;
    private String senderAddress;
    private String senderPhoneNumber;
    private String  prescription;

    private boolean sendAttachment;
}
