package com.siti.orderitem.Services;

import com.siti.orderitem.Models.Items;
import com.siti.orderitem.Repositories.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Items> getAllItems(int page, int size, String sortBy, String direction, String itemsName) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Items> itemsPage;
        if (itemsName == null || itemsName.isEmpty()) {
            itemsPage = itemRepository.findAll(pageable);
        } else {
            itemsPage = itemRepository.findByItemsNameContainingIgnoreCase(itemsName, pageable);
        }

        return itemsPage;
    }

    public Page<Items> getAllAvailableItems(int page, int size, String sortBy, String direction, String itemsName) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Items> availableItemsPage;
        if (itemsName == null || itemsName.isEmpty()) {
            availableItemsPage = itemRepository.findByIsAvailable(true, pageable);
        } else {
            availableItemsPage = itemRepository.findByItemsNameContainingIgnoreCaseAndIsAvailable(itemsName, true, pageable);
        }

        return availableItemsPage;
    }

    public Optional<Items> getItemById(Long itemsId) {
        return itemRepository.findByItemsId(itemsId);
    }

    public Items addItem(Items newItem) {
        if (newItem.getIsAvailable() == null) {
            newItem.setIsAvailable(true);
        }

        if (newItem.getLastReStock() == null) {
            newItem.setLastReStock(new Date());
        }

        Items savedItem = itemRepository.save(newItem);
        savedItem.setItemsCode();
        return itemRepository.save(savedItem);
    }

    public Items updateItemById(Long itemsId, Items updatedItem) {
        Optional<Items> itemOptional = itemRepository.findById(itemsId);
        if (itemOptional.isPresent()) {
            Items item = itemOptional.get();

            // Perbarui properti item berdasarkan updatedItem
            if (updatedItem.getItemsName() != null) {
                item.setItemsName(updatedItem.getItemsName());
            }
            if (updatedItem.getStock() != null) {
                item.setStock(updatedItem.getStock());
            }
            if (updatedItem.getPrice() != null) {
                item.setPrice(updatedItem.getPrice());
            }
            if (updatedItem.getLastReStock() != null) {
                item.setLastReStock(updatedItem.getLastReStock());
            }

            // Cek apakah stock menjadi 0, maka set isAvailable menjadi false
            if (item.getStock() != null && item.getStock() == 0) {
                item.setIsAvailable(false);
            }

            return itemRepository.save(item);
        } else {
            return null; // Atau throw exception jika item tidak ditemukan
        }
    }

    public void deleteItemById(Long itemsId) {
        Optional<Items> itemOptional = itemRepository.findById(itemsId);
        if (itemOptional.isPresent()) {
            Items item = itemOptional.get();
            if (!item.getIsAvailable()) {
                throw new IllegalArgumentException("Data dengan ID tersebut sudah dihapus.");
            }

            itemRepository.deleteById(itemsId);
        } else {
            throw new IllegalArgumentException("ID tidak ditemukan.");
        }
    }
}