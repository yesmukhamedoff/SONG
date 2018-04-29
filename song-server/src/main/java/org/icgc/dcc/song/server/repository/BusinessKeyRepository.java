package org.icgc.dcc.song.server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class BusinessKeyRepository {

 @Autowired
 private EntityManager entityManager;



}
