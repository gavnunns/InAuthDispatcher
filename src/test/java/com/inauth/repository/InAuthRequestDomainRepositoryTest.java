package com.inauth.repository;

import com.inauth.domain.InAuthRequestDomain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by gavnunns on 6/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
public class InAuthRequestDomainRepositoryTest {
    private InAuthRequestRepository requestRepository;

    @Autowired
    public void setRequestRepository(InAuthRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Test
    public void testSaveInAuthRequest() {
        //setup InAuthRequestDomain
        InAuthRequestDomain request = new InAuthRequestDomain();
        request.setMessage("test message");

        assertNull(request.getId());
        requestRepository.save(request);
        assertNotNull(request);

        InAuthRequestDomain fetchedInAuthRequestDomain = requestRepository.findOne(request.getId());

        assertNotNull(fetchedInAuthRequestDomain);

        assertEquals(request.getId(), fetchedInAuthRequestDomain.getId());
        assertEquals(request.getMessage(), fetchedInAuthRequestDomain.getMessage());

        fetchedInAuthRequestDomain.setMessage("new message");
        requestRepository.save(fetchedInAuthRequestDomain);

        InAuthRequestDomain fetchedUpdatedRquest = requestRepository.findOne(fetchedInAuthRequestDomain.getId());
        assertEquals(fetchedInAuthRequestDomain.getMessage(), fetchedUpdatedRquest.getMessage());

        long requestCount = requestRepository.count();
        assertEquals(requestCount, 1);
    }

}

