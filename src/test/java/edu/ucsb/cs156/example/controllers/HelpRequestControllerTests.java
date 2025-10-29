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

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {
  @MockBean private HelpRequestRepository helpRequestRepository;
  @MockBean private UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequest/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/helprequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/helprequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {
    HelpRequest h1 =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("test1@example.com")
            .teamId("team06")
            .tableOrBreakoutRoom("Table 6")
            .requestTime(java.time.LocalDateTime.of(2024, 1, 1, 1, 0))
            .explanation("test")
            .solved(false)
            .build();

    HelpRequest h2 =
        HelpRequest.builder()
            .id(2L)
            .requesterEmail("test2@example.com")
            .teamId("team07")
            .tableOrBreakoutRoom("test room")
            .requestTime(java.time.LocalDateTime.of(2023, 5, 6, 7, 8))
            .explanation("explanation")
            .solved(true)
            .build();

    ArrayList<HelpRequest> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(h1, h2));

    when(helpRequestRepository.findAll()).thenReturn(expected);

    MvcResult response =
        mockMvc.perform(get("/api/helprequest/all")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_user_can_post_a_new_help_request() throws Exception {
    HelpRequest req =
        HelpRequest.builder()
            .requesterEmail("andrew@example.com")
            .teamId("team06")
            .tableOrBreakoutRoom("Table 6")
            .requestTime(java.time.LocalDateTime.of(2023, 2, 2, 14, 0))
            .explanation("Help")
            .solved(true)
            .build();

    HelpRequest saved =
        HelpRequest.builder()
            .id(10L)
            .requesterEmail(req.getRequesterEmail())
            .teamId(req.getTeamId())
            .tableOrBreakoutRoom(req.getTableOrBreakoutRoom())
            .requestTime(req.getRequestTime())
            .explanation(req.getExplanation())
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(req))).thenReturn(saved);

    String url =
        String.format(
            "/api/helprequest/post?requesterEmail=%s&teamId=%s&tableOrBreakoutRoom=%s&requestTime=%s&explanation=%s&solved=%s",
            "andrew@example.com", "team06", "Table 6", "2023-02-02T14:00:00", "Help", "true");

    MvcResult response =
        mockMvc.perform(post(url).with(csrf())).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).save(req);
    String expectedJson = mapper.writeValueAsString(saved);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/helprequest?id=1")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    HelpRequest helpRequest =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("test@example.com")
            .teamId("team06")
            .tableOrBreakoutRoom("Table 6")
            .requestTime(java.time.LocalDateTime.of(2024, 1, 1, 12, 0))
            .explanation("help please")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(1L))).thenReturn(Optional.of(helpRequest));

    MvcResult response =
        mockMvc.perform(get("/api/helprequest?id=1")).andExpect(status().isOk()).andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(1L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    when(helpRequestRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/helprequest?id=999"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(eq(999L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_help_request() throws Exception {
    HelpRequest helpRequest =
        HelpRequest.builder()
            .id(5L)
            .requesterEmail("delete@example.com")
            .teamId("team06")
            .tableOrBreakoutRoom("Table 6")
            .requestTime(java.time.LocalDateTime.of(2024, 3, 15, 10, 30))
            .explanation("Deleted")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(5L))).thenReturn(Optional.of(helpRequest));

    MvcResult response =
        mockMvc
            .perform(delete("/api/helprequest?id=5").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(5L);
    verify(helpRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 5 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_help_request_and_gets_right_error_message()
      throws Exception {
    when(helpRequestRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/helprequest?id=999").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(999L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 999 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_help_request() throws Exception {
    HelpRequest orig =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("orig@example.com")
            .teamId("team6")
            .tableOrBreakoutRoom("Table 6")
            .requestTime(java.time.LocalDateTime.of(2024, 4, 4, 10, 0))
            .explanation("original explanation")
            .solved(false)
            .build();

    HelpRequest edited =
        HelpRequest.builder()
            .id(7L)
            .requesterEmail("edited@example.com")
            .teamId("team6-edited")
            .tableOrBreakoutRoom("Table 6a")
            .requestTime(java.time.LocalDateTime.of(2024, 4, 5, 11, 30))
            .explanation("edited explanation")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(orig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequest?id=7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(7L);
    verify(helpRequestRepository, times(1)).save(edited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_help_request_that_does_not_exist() throws Exception {
    HelpRequest edited =
        HelpRequest.builder()
            .id(999L)
            .requesterEmail("noone@example.com")
            .teamId("team999")
            .tableOrBreakoutRoom("Table 999")
            .requestTime(java.time.LocalDateTime.of(2024, 6, 6, 9, 15))
            .explanation("lol")
            .solved(false)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(helpRequestRepository.findById(eq(999L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequest?id=999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(helpRequestRepository, times(1)).findById(999L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 999 not found", json.get("message"));
  }
}
