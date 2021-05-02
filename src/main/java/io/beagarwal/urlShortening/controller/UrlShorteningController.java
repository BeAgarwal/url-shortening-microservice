package io.beagarwal.urlShortening.controller;

import io.beagarwal.urlShortening.model.Url;
import io.beagarwal.urlShortening.model.UrlDto;
import io.beagarwal.urlShortening.model.UrlErrorResponseDto;
import io.beagarwal.urlShortening.model.UrlResponseDto;
import io.beagarwal.urlShortening.service.UrlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
public class UrlShorteningController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateShortLink(@RequestBody UrlDto urlDto) {

        Url urlToReturn = urlService.generateShortLink(urlDto);

        if(urlToReturn != null) {
            UrlResponseDto urlResponseDto = new UrlResponseDto();
            urlResponseDto.setOriginalUrl(urlToReturn.getOriginalUrl());
            urlResponseDto.setExpirationDate(urlToReturn.getExpirationData());
            urlResponseDto.setShortLink(urlToReturn.getShortLink());

            return new ResponseEntity<UrlResponseDto>(urlResponseDto, HttpStatus.OK);
        }

        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
        urlErrorResponseDto.setStatus(HttpStatus.NOT_FOUND.toString());
        urlErrorResponseDto.setError("There is an error while processing your request. Please try again!");

        return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto, HttpStatus.OK);
    }

    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {

        if (StringUtils.isEmpty(shortLink)) {
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setStatus(HttpStatus.BAD_REQUEST.toString());
            urlErrorResponseDto.setError("Invalid Url!");

            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto, HttpStatus.OK);
        }

        Url urlToReturn = urlService.getEncodedUrl(shortLink);
        if(urlToReturn == null) {
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setStatus(HttpStatus.BAD_REQUEST.toString());
            urlErrorResponseDto.setError("URL doesn't exist or it may be expired!!");

            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto, HttpStatus.OK);
        }

        if(urlToReturn.getExpirationData().isBefore(LocalDateTime.now())) {
            urlService.deleteShortLink(urlToReturn); // delete from database
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setStatus(HttpStatus.OK.toString());
            urlErrorResponseDto.setError("URL expired, Please try generating a fresh one!");

            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto, HttpStatus.OK);
        }
        response.sendRedirect(urlToReturn.getOriginalUrl());
        return null;
    }
}
