package com.backend.taskflowapi.exception;

public class NotOrganizationMemberException extends RuntimeException{

    public NotOrganizationMemberException(){
        super("You are not a member of this organization");
    }
}
