package com.squirrel.communication.controller;

import com.squirrel.communication.model.ResponseModel;

public class AbstractController {
   protected ResponseModel convertToResponseModel(Object object){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setSuccess(Boolean.toString(true));
        responseModel.setResponse(object);
        return responseModel;
    }
}
