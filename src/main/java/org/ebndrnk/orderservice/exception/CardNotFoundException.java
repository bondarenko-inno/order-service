package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class CardNotFoundException extends BaseServiceException {
    public CardNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "CARD_NOT_FOUND");
    }
}
