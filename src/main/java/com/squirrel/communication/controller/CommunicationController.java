package com.squirrel.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squirrel.communication.model.CommunicationModel;
import com.squirrel.communication.model.EmailModel;
import com.squirrel.communication.model.ResponseModel;
import com.squirrel.communication.model.TextModel;
import com.squirrel.communication.service.EmailService;
import com.squirrel.communication.service.impl.AccountCreationEmailService;
import com.squirrel.communication.service.impl.AppointmentCreationEmailService;
import com.squirrel.communication.service.impl.MessageCreationEmailService;
import com.squirrel.communication.service.impl.PrescriptionCreationEmailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.thymeleaf.TemplateEngine;


@RestController(value = "communication-service")
public class CommunicationController extends AbstractController {

    private JavaMailSender sender;
    private TemplateEngine templateEngine;
    private static final Log logger = LogFactory.getLog(CommunicationController.class);


    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "channel1")
    public void listen(ConsumerRecord<String, String> record) throws JsonProcessingException {
        CommunicationModel inputCommunication = new ObjectMapper().readValue(record.value(), CommunicationModel.class);
        switch (inputCommunication.getCommunicationType()){
            case EMAIL:
                email(inputCommunication.getEmailModel());
                break;
            case TEXT:
                appointmentText(inputCommunication.getTextModel());
                break;
        }
    }


    @RequestMapping(value = "/text", method = RequestMethod.POST)
    public void appointmentText(TextModel textModel) {

    }


    @RequestMapping(value = "/email", method = RequestMethod.POST)
    public ResponseModel email(@RequestBody EmailModel emailModel) {
        try {
            EmailService emailService;
            switch (emailModel.getEmailType()) {
                case ACCOUNT:
                    emailService = new AccountCreationEmailService(sender, templateEngine);
                    break;
                case APPOINTMENT:
                    emailService = new AppointmentCreationEmailService(sender, templateEngine);
                    break;
                case PRESCRIPTION:
                    emailService = new PrescriptionCreationEmailService(sender, templateEngine);
                    break;
                case MESSAGE:
                    emailService = new MessageCreationEmailService(sender, templateEngine);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + emailModel.getEmailType());
            }
            logger.info(String.format("Email model contents %s",emailModel));
            emailService.sendEmail(emailModel);
            return convertToResponseModel(Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            return convertToResponseModel(Boolean.FALSE);
        }
    }


    @Autowired
    public CommunicationController(JavaMailSender sender, TemplateEngine templateEngine) {
        this.sender = sender;
        this.templateEngine = templateEngine;
    }
}
