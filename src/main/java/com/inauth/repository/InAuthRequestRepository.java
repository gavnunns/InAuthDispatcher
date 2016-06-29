package com.inauth.repository;

import com.inauth.domain.InAuthRequestDomain;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by gavnunns on 6/27/16.
 *
 */
public interface InAuthRequestRepository extends CrudRepository<InAuthRequestDomain, Integer> {
}
