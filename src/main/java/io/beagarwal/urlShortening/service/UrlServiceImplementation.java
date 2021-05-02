package io.beagarwal.urlShortening.service;

import com.google.common.hash.Hashing;
import io.beagarwal.urlShortening.model.Url;
import io.beagarwal.urlShortening.model.UrlDto;
import io.beagarwal.urlShortening.repository.UrlRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class UrlServiceImplementation implements UrlService{

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public Url generateShortLink(UrlDto urlDto) {
        if(StringUtils.isNotEmpty(urlDto.getUrl())) {

            String encodedUrl = encodeUrl(urlDto.getUrl());

            Url urlToPersist = new Url();
            urlToPersist.setShortLink(encodedUrl);
            urlToPersist.setOriginalUrl(urlDto.getUrl());
            urlToPersist.setCreationDate(LocalDateTime.now());
            urlToPersist.setExpirationData(getExpirationDate(urlDto.getExpirationDate(), urlToPersist.getCreationDate()));

            return persistShortLink(urlToPersist);
        }
        return null;
    }
    private String encodeUrl(String url) {
        String encodedUrl = "";
        LocalDateTime time = LocalDateTime.now();
        encodedUrl = Hashing.murmur3_32()
                    .hashString(url.concat(time.toString()), StandardCharsets.UTF_8)
                    .toString();
        return encodedUrl;
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if(StringUtils.isBlank(expirationDate)) {
            return creationDate.plusSeconds(60);    //after 60 seconds, link will expire
        }
        return LocalDateTime.parse(expirationDate);
    }


    @Override
    public Url persistShortLink(Url url) {
        return urlRepository.save(url);
    }

    @Override
    public Url getEncodedUrl(String url) {
        return urlRepository.findByShortLink(url);
    }

    @Override
    public void deleteShortLink(Url url) {
        urlRepository.delete(url);
    }
}
