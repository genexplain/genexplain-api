package com.genexplain.api.app;

public interface ApplicationCommand {
	public static final String NO_ARGS_MESSAGE = "Please provide an input file in JSON format as first argument";
	
    public void run(String[] args) throws Exception;
}