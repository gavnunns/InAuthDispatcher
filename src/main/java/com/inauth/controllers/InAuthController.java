package com.inauth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inauth.PoJos.*;
import com.inauth.domain.InAuthRequestDomain;
import com.inauth.domain.InAuthResponseDomain;
import com.inauth.repository.InAuthRequestRepository;
import com.inauth.repository.InAuthResponseRepository;
import com.inauth.repository.RepositoryConfiguration;
import com.inauth.workers.InAuthRequestProcessor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
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
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
public class InAuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String template = "Hello, %s!";
    private static final String PAYLOAD = "payload";
    private final AtomicLong counter = new AtomicLong();
    private InAuthRequestRepository requestRepository;
    private InAuthResponseRepository responseRepository;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<String> log(HttpServletRequest request) throws IOException {
        logger.info("<----Log submission Request Received---->");
        InAuthMobileRequestInfo mobileRequestInfo = getInAuthMobileRequestInfo((MultipartHttpServletRequest) request);

        InAuthRequestProcessor processor = new InAuthRequestProcessor();
        String requestResponse = processor.submitPayloadToInAuth(new InAuthPayload(mobileRequestInfo),
                InAuthRequestProcessor.MOBILE_LOG);

        ObjectMapper mapper = new ObjectMapper();
        LogResponse logResponse = mapper.readValue(requestResponse, LogResponse.class);

        logger.info(logResponse.deviceInfo.toString());

        String responseMessage;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (logResponse.deviceResponse == null) {
            //no response object to send back to the device
            //most likely a response to a log collection submission
            responseMessage = "Device log collection submitted successfully! Smile your on camera\n";
        } else {
            //logResponse was populated
            logger.info("Device Response Received From Server");
            logger.info("Base64 Decoding Then Launching Back at supper high velocity");
            responseMessage = decodeResponse(logResponse);
        }

        return new ResponseEntity<>(responseMessage, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<String> register(HttpServletRequest request) throws IOException {
        logger.info("<----Device Register Request Received---->");
        InAuthMobileRequestInfo mobileRequestInfo = getInAuthMobileRequestInfo((MultipartHttpServletRequest) request);

        recordRequest(mobileRequestInfo);
        InAuthRequestProcessor processor = new InAuthRequestProcessor();
        String requestResponse = processor.submitPayloadToInAuth(new InAuthPayload(mobileRequestInfo),
                InAuthRequestProcessor.MOBILE_REGISTER);

        ObjectMapper mapper = new ObjectMapper();
        RegistrationResponse registrationResponse = mapper.readValue(requestResponse, RegistrationResponse.class);

        logger.info("Device Info response from the InAuth API is :" + registrationResponse.deviceInfo);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        String decodedStringReponse = decodeResponse(registrationResponse);

        saveRegistrationResponse(registrationResponse);
        return new ResponseEntity<>(decodedStringReponse, responseHeaders, HttpStatus.OK);
    }

    private void saveRegistrationResponse(RegistrationResponse registrationResponse) {
        InAuthResponseDomain response = new InAuthResponseDomain();
        response.setMessage(registrationResponse.getDeviceResponseInBytes().toString());
        response.setRequestType(InAuthRequestProcessor.MOBILE_REGISTER);
        response.setPermanentId(registrationResponse.deviceInfo.permanentId);
        responseRepository.save(response);
    }

    private void recordRequest(InAuthMobileRequestInfo mobileRequestInfo) {
        try {
            InAuthRequestDomain request = new InAuthRequestDomain();
            // decoding the just encoded message for readability
            // the persistance is just intended for testing and transparancy
            byte[] decodedResponse = Base64.decodeBase64(mobileRequestInfo.message);
            request.setMessage(new String(decodedResponse, "UTF-8"));
            request.setRequestType(InAuthRequestProcessor.MOBILE_REGISTER);
            requestRepository.save(request);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String decodeResponse(InAuthResponse response) throws UnsupportedEncodingException {
        byte[] decodedResponse = Base64.decodeBase64(response.getDeviceResponseInBytes());
        return new String(decodedResponse, "UTF-8");
    }

    private InAuthMobileRequestInfo getInAuthMobileRequestInfo(MultipartHttpServletRequest request) throws IOException {
        MultipartFile multipartFile = request.getFile(PAYLOAD);
        logger.info("local address :" + request.getLocalAddr());
        String remoteAddr = getRemoteAddr();
        return new InAuthMobileRequestInfo(
                new String(Base64.encodeBase64(multipartFile.getBytes()), "UTF-8"),
                remoteAddr
        );
    }

    @RequestMapping(value = "/browser-request", method = RequestMethod.POST)
    public ResponseEntity<String> browserRequest(@RequestParam("request") MultipartFile file) {
        InAuthRequestProcessor processor = new InAuthRequestProcessor();

        try {
            String browserPayload = null;
            Long time = new Date().getTime();
            if (file != null) {
                if (file.getSize() > 0)
                    browserPayload = new String(file.getBytes(), "UTF-8");
            } else {
                logger.info("NO FILE FOUND");
            }

            //create a unique transactionID - this should be mapped to your user login session somehow
            String transactionID = UUID.randomUUID().toString();

            String remoteAddr = getRemoteAddr();

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

            //Do something really clever with the response, for now just print to sysout
            logger.info(requestResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("You are a browser request submitting master!\n", HttpStatus.OK);
    }

    private String getRemoteAddr() {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null)
            remoteAddr = "1.1.1.1";
        logger.info("remote address :" + remoteAddr);
        return remoteAddr;
    }

    //get request httpHeaders
    //more the better nom nom nom nom
    //black list ones that cause internal secuirty or privacy concerns
    private Map<String, String> getHeadersInfo() {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }

    @Autowired
    public void setRequestRepository(InAuthRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Autowired
    public void setResponseRepository(InAuthResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }
}
