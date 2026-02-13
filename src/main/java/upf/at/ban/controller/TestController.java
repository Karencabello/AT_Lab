package main.java.upf.at.ban.controller;
/*Creem un endpoint de prova */

import javax.ws.rs.*;

@Path("/test")
public class TestController {
    
    @Get
    @Produces(mediaType.TEXT_PLAIN)
    public String test(){
        return "OK";
    }
}
