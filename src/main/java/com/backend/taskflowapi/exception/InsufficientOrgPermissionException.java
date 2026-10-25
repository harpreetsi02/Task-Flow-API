package com.backend.taskflowapi.exception;

public class InsufficientOrgPermissionException extends RuntimeException{

    public InsufficientOrgPermissionException(){
        super("You do not have permission to perform this action in this organization");
    }
}
