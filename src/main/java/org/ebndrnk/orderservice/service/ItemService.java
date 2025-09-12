package org.ebndrnk.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.ebndrnk.orderservice.exception.ItemNotFoundException;
import org.ebndrnk.orderservice.exception.NotEnoughStock;
import org.ebndrnk.orderservice.mapper.ItemMapper;
import org.ebndrnk.orderservice.model.dto.ItemDto;
import org.ebndrnk.orderservice.model.dto.ItemResponse;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.repository.ItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;


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


    public List<ItemResponse> getAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::entityToResponse)
                .toList();
    }

    public ItemResponse getItemDtoById(Long id) {
        return itemMapper.entityToResponse(itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id)));
    }
}
