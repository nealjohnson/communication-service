package com.squirrel.communication.model;

import lombok.Data;

import java.util.UUID;

@Data
public class CommunicationModel {
    private UUID messageUUID;
    private CommunicationTypeEnum communicationType;
    private EmailModel emailModel;

    private TextModel textModel;
}
