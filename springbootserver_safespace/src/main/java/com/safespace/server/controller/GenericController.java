package com.safespace.server.controller;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generic")
public class GenericController {
    @GetMapping("/message")
    public void sendDistressMessage(
            @RequestParam(required = true) String to,
            @RequestParam(required = true) String lat,
            @RequestParam(required = true) String lng
    ) {
        // Find your Account SID and Auth Token at twilio.com/console
        // and set the environment variables. See http://twil.io/secure
        

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber("whatsapp:+" + to),
                        new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
                        "Hello! I'm in distress. Here's my location:\nhttps://maps.google.com/?q=" + lat + "," + lng)
                .create();

        System.out.println(message.getSid());
    }
}
