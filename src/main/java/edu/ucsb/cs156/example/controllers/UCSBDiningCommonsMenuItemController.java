package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSBDiningCommonsMenuItems
   *
   * @return all menu items
   */
  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();
    return items;
  }

  /**
   * Create a new UCSBDiningCommonsMenuItem
   *
   * @param diningCommonsCode the dining commons code (e.g. 'ortega')
   * @param name the name of the menu item (e.g. 'Pancakes')
   * @param station the station name (e.g. 'Breakfast')
   * @return the saved menu item
   */
  @Operation(summary = "Create a new UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    log.info("Creating menu item: {}, {}, {}", diningCommonsCode, name, station);

    UCSBDiningCommonsMenuItem menuItem = new UCSBDiningCommonsMenuItem();
    menuItem.setDiningCommonsCode(diningCommonsCode);
    menuItem.setName(name);
    menuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return savedItem;
  }

  /**
   * Get a single UCSBDiningCommonsMenuItem by id
   *
   * @param id the id of the menu item
   * @return the menu item with that id, or 404 if not found
   */
  @Operation(summary = "Get a single UCSB Dining Commons Menu Item by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {

    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return menuItem;
  }

  /**
   * Update a UCSBDiningCommonsMenuItem by id
   *
   * @param id the id of the menu item to update
   * @param incoming the updated menu item data
   * @return the updated menu item
   */
  @Operation(summary = "Update a UCSB Dining Commons Menu Item by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem existing =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    existing.setDiningCommonsCode(incoming.getDiningCommonsCode());
    existing.setName(incoming.getName());
    existing.setStation(incoming.getStation());

    UCSBDiningCommonsMenuItem updated = ucsbDiningCommonsMenuItemRepository.save(existing);
    return updated;
  }

  /**
   * Delete a UCSBDiningCommonsMenuItem by id
   *
   * @param id the id of the menu item to delete
   * @return a message indicating success or not found
   */
  @Operation(summary = "Delete a UCSB Dining Commons Menu Item by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @DeleteMapping("")
  public ResponseEntity<String> deleteMenuItem(@Parameter(name = "id") @RequestParam Long id) {

    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(item);
    return ResponseEntity.ok("record " + id + " deleted");
  }
}
