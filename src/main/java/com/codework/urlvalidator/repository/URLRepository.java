package com.codework.urlvalidator.repository;

import com.codework.urlvalidator.model.URLClass;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface URLRepository extends CrudRepository<URLClass, Long> {


    Optional<URLClass> findByCheckURL(String url);
}
