package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends BaseServiceException {
    public ItemNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND");
    }
}
