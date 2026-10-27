package com.backend.taskflowapi.exception;

public class InvalidAssigneeException extends RuntimeException{

    public InvalidAssigneeException(){
        super("Assignee must be a member of the organization!");
    }
}
