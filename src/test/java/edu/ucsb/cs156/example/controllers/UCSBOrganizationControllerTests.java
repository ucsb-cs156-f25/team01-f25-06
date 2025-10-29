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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(200)); // logged in users can get all
  }

  // Authorization tests for /api/ucsborganization/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    // arrange

    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgCode("OSLI")
            .orgTranslationShort("STUDENT LIFE")
            .orgTranslation("OFFICE OF STUDENT LIFE")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(org1, org2));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgCode=SKY&orgTranslationShort=SKYDIVING CLUB&orgTranslation=SKYDIVING CLUB AT UCSB&inactive=false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(org);
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization_with_inactive_true() throws Exception {
    // arrange

    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("OSLI")
            .orgTranslationShort("STUDENT LIFE")
            .orgTranslation("OFFICE OF STUDENT LIFE")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgCode=OSLI&orgTranslationShort=STUDENT LIFE&orgTranslation=OFFICE OF STUDENT LIFE&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(org);
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization?orgCode=SKY"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById("SKY")).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=SKY"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_with_inactive_true() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("OSLI")
            .orgTranslationShort("STUDENT LIFE")
            .orgTranslation("OFFICE OF STUDENT LIFE")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("OSLI")).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=OSLI"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("OSLI");
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_cannot_get_by_id_when_organization_not_found() throws Exception {
    // arrange
    when(ucsbOrganizationRepository.findById("DNE")).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgCode=DNE"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DNE");
    String responseString = response.getResponse().getContentAsString();
    assert responseString.contains("EntityNotFoundException");
    assert responseString.contains("DNE");
  }

  @Test
  public void logged_out_users_cannot_edit() throws Exception {
    mockMvc
        .perform(
            put(
                "/api/ucsborganization?orgCode=SKY&orgTranslationShort=SKY&orgTranslation=SKY&inactive=false"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_edit() throws Exception {
    mockMvc
        .perform(
            put(
                "/api/ucsborganization?orgCode=SKY&orgTranslationShort=SKY&orgTranslation=SKY&inactive=false"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_edit() throws Exception {
    // arrange
    UCSBOrganization existingOrg =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("OLD SHORT TRANSLATION")
            .orgTranslation("OLD TRANSLATION")
            .inactive(false)
            .build();

    UCSBOrganization updatedOrg =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("NEW SHORT TRANSLATION")
            .orgTranslation("NEW TRANSLATION")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(updatedOrg);

    when(ucsbOrganizationRepository.findById("SKY")).thenReturn(Optional.of(existingOrg));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=SKY")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1)).save(existingOrg);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_put_nonexistent_organization() throws Exception {
    // arrange
    UCSBOrganization updatedOrg =
        UCSBOrganization.builder()
            .orgCode("DNE")
            .orgTranslationShort("NEW SHORT TRANSLATION")
            .orgTranslation("NEW TRANSLATION")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(updatedOrg);

    when(ucsbOrganizationRepository.findById("DNE")).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgCode=DNE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DNE");
    verify(ucsbOrganizationRepository, times(0)).save(any());
    String responseString = response.getResponse().getContentAsString();
    assert responseString.contains("EntityNotFoundException");
    assert responseString.contains("DNE");
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc.perform(delete("/api/ucsborganization?orgCode=SKY")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsborganization?orgCode=SKY"))
        .andExpect(status().is(403)); // only admins can delete
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_delete_an_organization() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("SKYDIVING CLUB")
            .orgTranslation("SKYDIVING CLUB AT UCSB")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById("SKY")).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=SKY").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1)).delete(org);
    String responseString = response.getResponse().getContentAsString();
    assert responseString.contains("Organization with code SKY deleted");
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_delete_an_inactive_organization() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("OSLI")
            .orgTranslationShort("STUDENT LIFE")
            .orgTranslation("OFFICE OF STUDENT LIFE")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("OSLI")).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=OSLI").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("OSLI");
    verify(ucsbOrganizationRepository, times(1)).delete(org);
    String responseString = response.getResponse().getContentAsString();
    assert responseString.contains("Organization with code OSLI deleted");
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_cannot_delete_nonexistent_organization() throws Exception {
    // arrange
    when(ucsbOrganizationRepository.findById("DNE")).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgCode=DNE").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("DNE");
    verify(ucsbOrganizationRepository, times(0)).delete(any());
    String responseString = response.getResponse().getContentAsString();
    assert responseString.contains("EntityNotFoundException");
    assert responseString.contains("DNE");
  }
}
