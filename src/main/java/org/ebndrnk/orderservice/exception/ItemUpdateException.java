package org.ebndrnk.orderservice.exception;

import org.ebndrnk.common.exception.BaseServiceException;
import org.springframework.http.HttpStatus;

public class ItemUpdateException extends BaseServiceException {
    public ItemUpdateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "ERROR_ITEM_UPDATE");
    }
}
