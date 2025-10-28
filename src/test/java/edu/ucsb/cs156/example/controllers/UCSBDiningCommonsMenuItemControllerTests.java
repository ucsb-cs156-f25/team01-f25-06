package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
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
    assertEquals(
        "{\"message\":\"UCSBDiningCommonsMenuItem with id 999 not found\",\"type\":\"EntityNotFoundException\"}",
        responseString);
  }
}
