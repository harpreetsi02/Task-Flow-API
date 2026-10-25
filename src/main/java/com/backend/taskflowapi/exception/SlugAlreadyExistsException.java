package com.backend.taskflowapi.exception;

public class SlugAlreadyExistsException extends RuntimeException{

    public SlugAlreadyExistsException(String slug){
        super("An organization with slug '" + slug + "' already exists!");
    }
}
