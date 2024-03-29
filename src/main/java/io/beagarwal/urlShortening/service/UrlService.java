package io.beagarwal.urlShortening.service;

import io.beagarwal.urlShortening.model.Url;
import io.beagarwal.urlShortening.model.UrlDto;
import org.springframework.stereotype.Service;

@Service
public interface UrlService {
    //To generate the short link
    public Url generateShortLink(UrlDto urlDto);

    //To persist the short link
    public Url persistShortLink(Url url);

    //To get the encoded url
    public Url getEncodedUrl(String url);

    //To delete short link
    public void deleteShortLink(Url url);
}
