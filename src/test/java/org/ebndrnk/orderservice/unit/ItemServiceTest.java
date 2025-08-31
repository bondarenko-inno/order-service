package org.ebndrnk.orderservice.unit;

import org.ebndrnk.orderservice.exception.ItemNotFoundException;
import org.ebndrnk.orderservice.exception.NotEnoughStock;
import org.ebndrnk.orderservice.model.entity.Item;
import org.ebndrnk.orderservice.repository.ItemRepository;
import org.ebndrnk.orderservice.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    private ItemRepository itemRepository;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemRepository = Mockito.mock(ItemRepository.class);
        itemService = new ItemService(itemRepository);
    }

    @Test
    void getItemById_shouldReturnItem_whenExists() {
        // given
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(10L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // when
        Item result = itemService.getItemById(1L);

        // then
        assertThat(result).isEqualTo(item);
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getItemById_shouldThrow_whenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(1L));
    }

    @Test
    void reserveItem_shouldDecreaseQuantity_whenEnoughStock() {
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(10L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.reserveItem(1L, 3L);

        assertThat(result.getQuantity()).isEqualTo(7L);
    }

    @Test
    void reserveItem_shouldThrow_whenNotEnoughStock() {
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(2L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotEnoughStock.class, () -> itemService.reserveItem(1L, 5L));
    }

    @Test
    void returnItem_shouldIncreaseQuantity() {
        Item item = new Item();
        item.setId(1L);
        item.setQuantity(5L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.returnItem(1L, 3L);

        assertThat(item.getQuantity()).isEqualTo(8L);
    }
}
