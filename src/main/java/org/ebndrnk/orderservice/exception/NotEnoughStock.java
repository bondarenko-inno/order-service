package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class NotEnoughStock extends BaseServiceException {
    public NotEnoughStock(String message) {
        super(message, HttpStatus.CONFLICT, "NOT_ENOUGH_STOCK");
    }
}
