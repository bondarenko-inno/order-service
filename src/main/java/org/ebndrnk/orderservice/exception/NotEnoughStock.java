package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class NotEnoughStock extends BaseServiceException {
    public NotEnoughStock(String message) {
        super(message, HttpStatus.BAD_REQUEST, "NOT_ENOUGH_STOCK");
    }
}
