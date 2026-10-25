package com.backend.taskflowapi.exception;

public class AlreadyMemberException extends RuntimeException{

    public AlreadyMemberException(){
        super("This user is already a member of the organization");
    }
}
