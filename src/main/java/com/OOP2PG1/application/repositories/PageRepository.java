package com.OOP2PG1.application.repositories;

import com.OOP2PG1.application.entities.Page;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends MongoRepository<Page, String> {

   // Optional<Page> findBytitlePage(String titlePage);
    Optional<Page> findByTitlePage(String titlePage);
    Optional<Page> findByUrlTitlePage(String UrlTitlePage);
    Boolean existsByTitlePage(String titlePage);
    Boolean existsByUrlTitlePage(String titlePage);

//Optional<Page> findByurlHeader(String urlHeader);
//    Optional<Page> findBy<?>(String <?>);
//    Optional<Page> findBy<?>Or<?>(String <?>);
//    Optional<Page> findBy<urlHeader>Or<AdminId>(String <?>);
//    Optional<Page> find<?>By<?>(String <?>);
//    Optional<Page> find<?>By<?><?>(String <?>);

    List<Page> findByAdminId(String urlHeader);
}
