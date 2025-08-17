package org.ebndrnk.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.ebndrnk.orderservice.exception.ItemNotFoundException;
import org.ebndrnk.orderservice.exception.NotEnoughStock;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;


    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));
    }



    public Item reserveItem(Long itemId, Long requestedQty) {
        Item item = getItemById(itemId);

        if (item.getQuantity() < requestedQty) {
            throw new NotEnoughStock("Not enough stock for item ID " + itemId);
        }

        item.setQuantity(item.getQuantity() - requestedQty);
        return item;
    }

    @Transactional
    public void returnItem(Long itemId, Long quantity) {
        Item item = getItemById(itemId);
        item.setQuantity(item.getQuantity() + quantity);
    }


}
