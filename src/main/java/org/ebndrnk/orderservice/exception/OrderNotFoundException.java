package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseServiceException {
    public OrderNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
    }
}
