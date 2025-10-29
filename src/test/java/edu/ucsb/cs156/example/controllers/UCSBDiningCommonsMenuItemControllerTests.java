package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockBean UserRepository userRepository;

  // GET /api/ucsbdiningcommonsmenuitem/all
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(403)); // not logged in
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_user_can_post_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Pancakes")
            .station("Breakfast")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(item))).thenReturn(item);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post?diningCommonsCode=ortega&name=Pancakes&station=Breakfast")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item);
    String expectedJson = mapper.writeValueAsString(item);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_user_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("carrillo")
            .name("Chicken Parmesan")
            .station("Main")
            .build();

    UCSBDiningCommonsMenuItem item2 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("portola")
            .name("Salad")
            .station("Greens")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems =
        new ArrayList<>(Arrays.asList(item1, item2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // POST /api/ucsbdiningcommonsmenuitem/post
  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_user_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_new_item() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("carrillo")
            .name("Chicken Parmesan")
            .station("Main")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(any(UCSBDiningCommonsMenuItem.class)))
        .thenReturn(item);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post?diningCommonsCode=carrillo&name=Chicken Parmesan&station=Main")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1))
        .save(any(UCSBDiningCommonsMenuItem.class));
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_getById_exists() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Pancakes")
            .station("Breakfast")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(1L)).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
    String expectedJson = mapper.writeValueAsString(item);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_getById_not_found() throws Exception {
    when(ucsbDiningCommonsMenuItemRepository.findById(999L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem?id=999"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(999L);
    String responseString = response.getResponse().getContentAsString();

    JsonNode expected =
        mapper.readTree(
            "{\"message\":\"UCSBDiningCommonsMenuItem with id 999 not found\",\"type\":\"EntityNotFoundException\"}");
    JsonNode actual = mapper.readTree(responseString);
    assertEquals(expected, actual);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_put_menu_item_success() throws Exception {
    UCSBDiningCommonsMenuItem originalItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Pancakes")
            .station("Breakfast")
            .build();

    UCSBDiningCommonsMenuItem editedItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("portola")
            .name("Vegan Pancakes")
            .station("Bakery")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L)))
        .thenReturn(Optional.of(originalItem));
    when(ucsbDiningCommonsMenuItemRepository.save(any(UCSBDiningCommonsMenuItem.class)))
        .thenReturn(editedItem);

    String requestBody = mapper.writeValueAsString(editedItem);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem?id=1")
                    .contentType("application/json")
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    ArgumentCaptor<UCSBDiningCommonsMenuItem> captor =
        ArgumentCaptor.forClass(UCSBDiningCommonsMenuItem.class);
    verify(ucsbDiningCommonsMenuItemRepository).save(captor.capture());
    UCSBDiningCommonsMenuItem savedItem = captor.getValue();

    assertEquals("Vegan Pancakes", savedItem.getName());
    assertEquals("Bakery", savedItem.getStation());
    assertEquals("portola", savedItem.getDiningCommonsCode());

    String expectedJson = mapper.writeValueAsString(editedItem);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_put_menu_item_not_found() throws Exception {
    UCSBDiningCommonsMenuItem editedItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("portola") // different from original
            .name("Vegan Pancakes")
            .station("Bakery")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(99L))).thenReturn(Optional.empty());

    String requestBody = mapper.writeValueAsString(editedItem);

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem?id=99")
                    .contentType("application/json")
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(99L);
    String responseString = response.getResponse().getContentAsString();
    JsonNode expected =
        mapper.readTree(
            "{\"message\":\"UCSBDiningCommonsMenuItem with id 99 not found\",\"type\":\"EntityNotFoundException\"}");
    JsonNode actual = mapper.readTree(responseString);
    assertEquals(expected, actual);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_delete_menu_item_exists() throws Exception {
    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .id(15L)
            .diningCommonsCode("ortega")
            .name("Waffles")
            .station("Breakfast")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.of(item));

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(item);

    String expectedResponse = "record 15 deleted";
    String actualResponse = response.getResponse().getContentAsString();
    assertEquals(expectedResponse, actualResponse);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_delete_menu_item_not_found() throws Exception {
    when(ucsbDiningCommonsMenuItemRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem?id=999").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(999L);
    verify(ucsbDiningCommonsMenuItemRepository, times(0)).delete(any());

    String responseString = response.getResponse().getContentAsString();
    JsonNode expected =
        mapper.readTree(
            "{\"message\":\"UCSBDiningCommonsMenuItem with id 999 not found\",\"type\":\"EntityNotFoundException\"}");
    JsonNode actual = mapper.readTree(responseString);
    assertEquals(expected, actual);
  }
}
