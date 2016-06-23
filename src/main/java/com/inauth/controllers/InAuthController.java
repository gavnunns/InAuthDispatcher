package com.inauth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inauth.PoJos.*;
import com.inauth.workers.InAuthRequestProcessor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class InAuthController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }


    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<String> log(HttpServletRequest request) throws IOException {
        InAuthMobileRequestInfo mobileRequestInfo = getInAuthMobileRequestInfo((MultipartHttpServletRequest) request);

        InAuthRequestProcessor processor = new InAuthRequestProcessor();
        String requestResponse = processor.submitPayloadToInAuth(new InAuthPayload(mobileRequestInfo),
                InAuthRequestProcessor.MOBILE_LOG);

        System.out.println(requestResponse);
        ObjectMapper mapper = new ObjectMapper();
        LogResponse logResponse = mapper.readValue(requestResponse, LogResponse.class);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        String decodedStringReponse = decodeResponse(logResponse);
        System.out.println(decodedStringReponse);
        return new ResponseEntity<String>(decodedStringReponse, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<String> register(HttpServletRequest request) throws IOException {
        InAuthMobileRequestInfo mobileRequestInfo = getInAuthMobileRequestInfo((MultipartHttpServletRequest) request);

        InAuthRequestProcessor processor = new InAuthRequestProcessor();
        String requestResponse = processor.submitPayloadToInAuth(new InAuthPayload(mobileRequestInfo),
                InAuthRequestProcessor.MOBILE_REGISTER);

        ObjectMapper mapper = new ObjectMapper();
        RegistrationResponse registrationResponse = mapper.readValue(requestResponse, RegistrationResponse.class);

        System.out.println("And your response is :" + registrationResponse.deviceInfo);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        String decodedStringReponse = decodeResponse(registrationResponse);
        System.out.println(decodedStringReponse);
        return new ResponseEntity<String>(decodedStringReponse, responseHeaders, HttpStatus.OK);
    }

    private String decodeResponse(InAuthResponse response) throws UnsupportedEncodingException {
        byte[] decodedResponse = Base64.decodeBase64(response.getDeviceResponseInBytes());
        return new String(decodedResponse, "UTF-8");
    }

    private InAuthMobileRequestInfo getInAuthMobileRequestInfo(MultipartHttpServletRequest request) throws IOException {
        MultipartHttpServletRequest r = request;
        MultipartFile multipartFile = r.getFile("payload");

        String mobilePayload = new String(multipartFile.getBytes(), "UTF-8");
        System.out.println(mobilePayload);

        return new InAuthMobileRequestInfo(
                new String(Base64.encodeBase64(multipartFile.getBytes()), "UTF-8"),
                "1.1.1.1"
        );
    }

    @RequestMapping(value = "/browser-request", method = RequestMethod.POST)
    public ResponseEntity<String> browserRequest(@RequestParam("request") MultipartFile file,
                                                 @RequestParam("remoteAddr") String remoteAddr) {
        InAuthRequestProcessor processor = new InAuthRequestProcessor();

        if (remoteAddr.length() < 7)
            return new ResponseEntity<String>("Remote Address is not valid\n", HttpStatus.BAD_REQUEST);

        try {
            String browserPayload = null;
            Long time = new Date().getTime();
            if (file != null) {
                if (file.getSize() > 0)
                    browserPayload = new String(file.getBytes(), "UTF-8");
            } else {
                System.out.println("NO FILE FOUND");
            }

            //create a unique transactionID - this should be mapped to your user login session somehow
            String transactionID = UUID.randomUUID().toString();

            //constructing a pojo with everything a good browserRequest will need!
            InAuthBrowserRequestInfo browserRequestInfo = new InAuthBrowserRequestInfo(getHeadersInfo(),
                    remoteAddr,
                    transactionID,
                    "POST",
                    time.toString(),
                    browserPayload
            );

            String requestResponse = processor.submitPayloadToInAuth(new InAuthPayload(browserRequestInfo),
                    InAuthRequestProcessor.BROWSER_REQUEST);

            //Do something realy realy clever with the response, for now just print to sysout
            System.out.println(requestResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>("You are a browser request submitting master!\n", HttpStatus.OK);
    }

    //get request httpHeaders
    private Map<String, String> getHeadersInfo() {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}
